package com.miueon.blog.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.elrepo.PostSearchDao
import com.miueon.blog.mpg.PostELDO
import com.miueon.blog.mpg.model.PostDO
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders.matchQuery
import org.elasticsearch.index.query.QueryBuilders.multiMatchQuery
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service

@Service
class PostEService {
    @Autowired
    private lateinit var postSearchDao: PostSearchDao

    @Autowired
    private lateinit var elasticsearchRestTemplate: ElasticsearchRestTemplate
    @Autowired
    lateinit var postService: PostService

    val logger = LoggerFactory.getLogger(this.javaClass)
    fun search(keyword: String): List<PostDO> {
        val searchQuery = NativeSearchQueryBuilder()
                .withQuery(multiMatchQuery(keyword, "title", "body")
                        .operator(Operator.OR)
                        .fuzziness(Fuzziness.ONE)
                        // the fuzziness will make the query tolerate one character miss
                        .prefixLength(3) // the first 3 character must exactly
                        // matches

                ).build()
        val hits = elasticsearchRestTemplate.search(searchQuery, PostELDO::class.java,
                IndexCoordinates.of("blog"))
        val list: MutableList<PostDO> = ArrayList()
        val listSearchHist = hits.searchHits
        logger.info(" search hits: {}", listSearchHist)
        listSearchHist.forEach { list.add(it.content.toDO()) }

        return postService.polishPostList(list)
    }



    fun deletePostE(ids: List<String>) {
        val pending = postSearchDao.findAllById(ids)
        logger.info(" pending: {}", pending)
        postSearchDao.deleteAll(pending)
    }


    fun updatePostE(pe: PostELDO) {
        val searchQuery = NativeSearchQueryBuilder()
                .withQuery(matchQuery("title", pe.title)
                        .minimumShouldMatch("75%"))
                .build()
        val postEHits = elasticsearchRestTemplate.search(searchQuery, PostELDO::class.java,
                IndexCoordinates.of("blog"))
        val result = postEHits.searchHits[0].content
        result.title = pe.title
        postSearchDao.save(result)
    }

    fun registerPostE(pe: PostELDO) {
        postSearchDao.save(pe)
    }

    fun init(ps: List<PostELDO>) {
        postSearchDao.deleteAll()
        postSearchDao.saveAll(ps)
    }

}
package com.miueon.blog.service
//
//import com.alibaba.fastjson.JSONObject
//import com.miueon.blog.elrepo.PostSearchDao
//import com.miueon.blog.pojo.post
//import com.miueon.blog.mpg.postE
//import org.elasticsearch.client.Client
//import org.elasticsearch.client.RestHighLevelClient
//import org.elasticsearch.common.unit.Fuzziness
//import org.elasticsearch.index.query.Operator
//import org.elasticsearch.index.query.QueryBuilders.matchQuery
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
//import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
//import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
//import org.springframework.stereotype.Service
//import javax.annotation.Resource
//
//@Service
//class PostEService {
//    @Autowired
//    private lateinit var postSearchDao: PostSearchDao
//
//    @Autowired
//    private lateinit var elasticsearchRestTemplate: ElasticsearchRestTemplate
//
//
//    fun search(keyword: String): List<postE> {
//        val searchQuery = NativeSearchQueryBuilder()
//                .withQuery(matchQuery("title", keyword)
//                        .operator(Operator.OR)
//                        .fuzziness(Fuzziness.ONE)
//                        // the fuzziness will make the query tolerate one character miss
//                        .prefixLength(3) // the first 3 character must exactly
//                        // matches
//                ).build()
//
//        val hits = elasticsearchRestTemplate.search(searchQuery, postE::class.java,
//                IndexCoordinates.of("blog"))
//        val list: MutableList<postE> = arrayListOf()
//        val listSearchHist = hits.searchHits
//        listSearchHist.forEach { list.add(it.content) }
//        return list
//    }
//
//    fun deletePostE(id: String) {
//        postSearchDao.deleteById(id)
//    }
//
//    fun updatePostE(pe: postE) {
//        val searchQuery = NativeSearchQueryBuilder()
//                .withQuery(matchQuery("title", pe.title)
//                        .minimumShouldMatch("75%"))
//                .build()
//        val postEHits = elasticsearchRestTemplate.search(searchQuery, postE::class.java,
//                IndexCoordinates.of("blog"))
//        val result = postEHits.searchHits[0].content
//        result.title = pe.title
//        postSearchDao.save(result)
//    }
//
//    fun registerPostE(pe: postE) {
//        postSearchDao.save(pe)
//    }
//
//}
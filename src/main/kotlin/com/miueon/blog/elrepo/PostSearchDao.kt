package com.miueon.blog.elrepo

import com.miueon.blog.mpg.PostELDO
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface PostSearchDao : ElasticsearchRepository<PostELDO, String> {

}
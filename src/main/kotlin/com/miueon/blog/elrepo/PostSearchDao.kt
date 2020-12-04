package com.miueon.blog.elrepo

import com.miueon.blog.mpg.postE
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface PostSearchDao : ElasticsearchRepository<postE, String> {

}
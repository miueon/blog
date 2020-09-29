package com.miueon.blog.elrepo

import com.miueon.blog.pojo.postE
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface PostSearchDao : ElasticsearchRepository<postE, String> {

}
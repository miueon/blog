package com.miueon.blog.controller

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.IdListDTO
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.mpg.model.TagsDO
import com.miueon.blog.service.BulkDelete
import com.miueon.blog.service.DELETEKEY
import com.miueon.blog.service.TagPostService
import com.miueon.blog.service.TagService
import com.miueon.blog.util.ApiException
import com.miueon.blog.util.Page4Navigator
import com.miueon.blog.util.Reply
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tag")
class TagController {
    @Autowired
    lateinit var tagService: TagService
    @Autowired
    lateinit var tagPostService: TagPostService
    @Autowired
    lateinit var bulkDelete: BulkDelete
    private var logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    fun getTags(
            @RequestParam(value = "start", defaultValue = "1") start: Int,
            @RequestParam(value = "size", defaultValue = "20") size: Int
    ): ResponseEntity<Reply<Page4Navigator<TagsDO>>> {
        val tags = tagService.getTags(Page(start.toLong(), size.toLong()), 5)
        tags.content?.forEach{
            it.postCount = tagPostService.getPostCountByTid(it.id!!)
        }
        return ResponseEntity(Reply.success(tags), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getTag(@PathVariable id: Int): ResponseEntity<Reply<TagsDO>> {
        val tag =  tagService.findForId(id)
        return ResponseEntity(Reply.success(tag), HttpStatus.OK)
    }

    data class TagName(val name:String?)
    @PostMapping
    fun addTag(@RequestBody tName: TagName): Reply<Int> {
        if (StringUtils.isBlank(tName.name) or (tName.name == null)) {
            throw ApiException("the tag's name should not be empty.",
                    HttpStatus.BAD_REQUEST)
        }
        val result = tagService.saveTag(tName.name!!)
        return Reply.success(result.id!!)
    }

    @PostMapping("/bulk_delete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun bulkDeletePrep(@RequestBody idList: IdListDTO): Reply<Unit> {
        bulkDelete.prepToDelete(idList.ids, DELETEKEY.TAG)
        return Reply.success()
    }


    @DeleteMapping("/bulk_delete")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    fun batchDelete(): Reply<Unit> {
        val ids = bulkDelete.getDeleteInfo(DELETEKEY.TAG)
        tagService.bulkDelete(ids.toList())
        return Reply.success()
    }

    @PutMapping("/{id}")
    fun editTag(@PathVariable id: Int, @RequestBody tName: TagName): Reply<Unit> {
        if (StringUtils.isNotBlank(tName.name)) {
            tagService.updateForId(id, tName.name!!)
        } else {
            throw ApiException("tag name should not be empty.", HttpStatus.BAD_REQUEST)
        }
        return Reply.success()
    }

    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: Int): Reply<Unit> {
        tagService.deleteForId(id)
        return Reply.success()
    }
}
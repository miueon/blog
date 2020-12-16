package com.miueon.blog.mpg

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.miueon.blog.mpg.model.CommentDO
import com.miueon.blog.mpg.model.PostDO
import com.miueon.blog.mpg.model.UserDO
import com.miueon.blog.validator.Insert
import com.miueon.blog.validator.Update
import org.hibernate.validator.constraints.Range
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class userDto(
        var name: String? = null,
        var isAdmin: Boolean = false
)

data class IdListDTO(val ids: IdList)

data class PostArchive(val year: Int, val month: Int, val postCount: Int) {
    @get:JsonIgnore
    val yearMonth: Int
        get() = this.year * 100 + this.month
}

/**
 * Author: Miueon
 *
 * Description: // transform the data from one to another.
 * the target class should has the desire constructor parameter
 *
 * Date:  3:06 p.m. 2020-12-05
 **/
open class Transformer<T : Any, R : Any>
protected constructor(inClass: KClass<T>, outClass: KClass<R>) {
    // get Constructor & filed names by reflection
    private val outConstructor = outClass.primaryConstructor!!
    private val inConstructor = inClass.primaryConstructor!!
    private val inPropertiesByName by lazy {
        inClass.memberProperties.associateBy { it.name }
    }
    private val outPropertiesByName by lazy {
        outClass.memberProperties.associateBy { it.name }
    }

    fun transform(data: T): R = with(outConstructor) {
        // callBy can call the method with it default value by give in map of arguments
        callBy(parameters.associateWith { parameter -> argFor(parameter, data) })
    }

    fun reverseTransForm(data: R): T = with(inConstructor) {
        callBy(parameters.associateWith { reverseArgFor(it, data) })
    }

    open fun argFor(parameter: KParameter, data: T): Any? {
        return inPropertiesByName[parameter.name]?.get(data)
    }

    open fun reverseArgFor(parameter: KParameter, data: R): Any? {
        return outPropertiesByName[parameter.name]?.get(data)
    }
}

//val personFormToPersonRecordTransformer = object
//    : Transformer<PersonForm, PersonRecord>(PersonForm::class, PersonRecord::class) {
//    override fun argFor(parameter: KParameter, data: PersonForm): Any? {
//        return when (parameter.name) {
//            "name" -> with(data) { "$firstName $lastName" }
//            else -> super.argFor(parameter, data)
//        }
//    }
//}
// @NotEmpty is for string,collection, map or array.
class PostTitle(var id: Int?, var title: String?)

class CommentDTO(
        @field:NotNull(message = "When you add a comment,it has to go somewhere.", groups = [Insert::class, Update::class])
        @field:Range(min = 1, groups = [Insert::class, Update::class])
        var pid: Int? = null,
        @field:NotEmpty(message = "you can't comment an empty", groups = [Insert::class, Update::class])
        @field:Size(min = 1, max = 300, groups = [Insert::class, Update::class])
        var content: String? = null,
        @field:Valid
        @field:NotEmpty(message = "comments should have identity.", groups = [Insert::class])
        var usr: String? = null
) {
    companion object {
        val transformer = object : Transformer<CommentDTO, CommentDO>(CommentDTO::class, CommentDO::class) {}
        fun fromDO(data: CommentDO): CommentDTO {
            return transformer.reverseTransForm(data)
        }
    }

    fun transToDO(): CommentDO {
        return transformer.transform(this)
    }
}

class CommentUserInfo(@NotEmpty(message = "the name shouldn't be empty")
                      var name: String? = null,
                      @NotEmpty(message = "the email shouldn't be empty")
                      var email: String? = null,
                      @NotEmpty(message = "the url is also critical to identify")
                      var url: String? = null) {
    companion object {
        val transformer = object : Transformer<CommentUserInfo, UserDO>(CommentUserInfo::class, UserDO::class) {}
        fun fromDO(data: UserDO): CommentUserInfo {
            return transformer.reverseTransForm(data)
        }
    }

    fun transToDO(): UserDO {
        return transformer.transform(this)
    }

}

@Document(indexName = "blog", type = "article")
data class PostELDO(
        @Id
        var id: Int? = null,
        var body: String? = null,
        var title: String? = null,
        @Field(type= FieldType.Date, format = DateFormat.basic_date_time)
        var createdDate: LocalDateTime? = null,
        @Field(type= FieldType.Date, format = DateFormat.basic_date_time)
        var modifiedDate: LocalDateTime? = null,
        var uid: Int? = null,
        var cid: Int? = null,
        var view: Int? = null
) {
    fun toDO(): PostDO {
        return PostDO(id, uid, title, body, createdDate, modifiedDate, cid, view)
    }

    companion object {
        fun fromDO(post: PostDO): PostELDO {
            return PostELDO(post.id, post.body, post.title, post.createdDate, post.modifiedDate,
                    post.uid, post.cid, post.view
            )
        }
    }
}

data class Search(val keyWord:String)


typealias IdList = List<Int>
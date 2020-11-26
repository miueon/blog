package com.miueon.blog.service


import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.miueon.blog.mpg.model.CategoryDO
import com.miueon.blog.util.ApiException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.transaction.annotation.Transactional
import kotlin.properties.Delegates

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CategoryServiceImplTest {
    @Autowired
    lateinit var categoryService: CategoryService

    var id by Delegates.notNull<Int>()
    lateinit var name: String

    @BeforeAll
    fun initAndTestGetCategories() {
        val categoryPage =
                categoryService.getCategories(Page(1, 10), 5)
        assertThat(categoryPage.content, notNullValue())
        assertThat(categoryPage.content!!.size, greaterThan(0))
        id = categoryPage.content!![0].id!!
        name = categoryPage.content!![0].name.toString()
    }

    @Test
    fun testFindForId() {
        val result = categoryService.findForId(id)
        assertThat(result, notNullValue())
    }

    @Test
    @Tag("FindForId exception testing")
    fun testFindForIdException() {
        val testId = 114514
        val throwable = assertThrows(ApiException::class.java
        ) { categoryService.findForId(testId) }
        assertEquals("the id: $testId is not in database", throwable.message)
    }

    @Test
    @Transactional
    fun testSave() {
        val testName = "human beings should be obliterated"
        val resultDo = categoryService.saveCategory(testName)
        assertEquals(resultDo.name, testName)
    }

    @Test
    @Transactional
    fun testSaveException() {
        val throwable = assertThrows(ApiException::class.java)
        { categoryService.saveCategory(name) }
        assertEquals(throwable.message, "category already exist.")
    }

    @Test
    @Transactional
    fun testUpdateExceptionForDuplicateName() {
        val throwable = assertThrows(ApiException::class.java) {
            categoryService.updateForId(14, name)
        }
        assertEquals(throwable.message, "category already exist.")
    }

    @Test
    @Transactional
    fun testUpdateForId() {
        val testId = 114514
        val throwable = assertThrows(ApiException::class.java) {
            categoryService.updateForId(testId, name)
        }

        assertEquals(throwable.message, "the id: $testId is not in database")
    }


}
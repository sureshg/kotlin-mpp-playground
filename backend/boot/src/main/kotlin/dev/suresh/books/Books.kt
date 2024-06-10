package dev.suresh.books

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Serializable data class Author(val id: Long, val name: String)

@Serializable
data class Book(
    val id: Long,
    val title: String,
    val description: String,
    val created: LocalDateTime,
    val author: Author
)

@Controller
@ResponseBody
@RequestMapping("/books")
class BooksController {

  @GetMapping fun books() = emptyList<Book>()
}

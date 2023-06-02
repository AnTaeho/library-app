package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 저장 테스트")
    fun saveBookTest() {
        //given
        val request = BookRequest("title", BookType.COMPUTER)

        //when
        bookService.saveBook(request)

        //then
        val result = bookRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("title")
        assertThat(result[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("책 정상 대출 테스트")
    fun loanBookTest() {
        //given
        bookRepository.save(Book.fixture())
        val savedUser = userRepository.save(User("A", null))
        val request = BookLoanRequest("A", "책 이름")

        //when
        bookService.loanBook(request)

        //then
        val result = userLoanHistoryRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].bookName).isEqualTo("책 이름")
        assertThat(result[0].user.id).isEqualTo(savedUser.id)
        assertThat(result[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("책 예외 대출 테스트")
    fun loanBookFailTest() {
        //given
        bookRepository.save(Book.fixture())
        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "책 이름"))
        val request = BookLoanRequest("A", "책 이름")

        //when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    @DisplayName("책 반납 테스트")
    fun returnBookTest() {
        //given
        bookRepository.save(Book.fixture())
        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "title"))
        val request = BookReturnRequest("A", "title")

        //when
        bookService.returnBook(request)

        //then
        val result = userLoanHistoryRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    @DisplayName("책 대여 권수를 정상 확인한다.")
    fun countLoanedBookTest() {
        //given
        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "title"),
            UserLoanHistory.fixture(savedUser, "title1", UserLoanStatus.RETURNED),
            UserLoanHistory.fixture(savedUser, "title2", UserLoanStatus.RETURNED),
        ))
        
        //when
        val result = bookService.countLoanedBook()

        //then
        assertThat(result).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 책 권수를 정상 확인한다.")
    fun getBookStatisticsTest() {
        //given
        bookRepository.saveAll(listOf(
            Book.fixture("A", BookType.COMPUTER),
            Book.fixture("B", BookType.COMPUTER),
            Book.fixture("C", BookType.SCIENCE),
        ))

        //when
        val results = bookService.getBookStatistics()

        //then
        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2)
        assertCount(results, BookType.SCIENCE, 1)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Int) {
        assertThat(results.first { result -> result.type == type }.count).isEqualTo(count)
    }

}
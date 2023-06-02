package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("유저 저장 테스트")
    fun saveUserTest() {
        //given
        val request = UserCreateRequest("tate", null)

        //when
        userService.saveUser(request)

        //then
        val result = userRepository.findAll()
        assertThat(result).hasSize(1)
        assertThat(result.get(0).name).isEqualTo("tate")
        assertThat(result.get(0).age).isNull()
    }

    @Test
    @DisplayName("유저 조회 테스트")
    fun getUserTest() {
        //given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", null)
        ))

        //when
        val result = userService.getUsers()

        //then
        assertThat(result).hasSize(2)
        assertThat(result).extracting("name")
            .containsExactlyInAnyOrder("A", "B")
        assertThat(result).extracting("age")
            .containsExactlyInAnyOrder(20, null)
    }

    @Test
    @DisplayName("유저 수정 테스트")
    fun updateUserNameTest() {
        //given
        val savedUser = userRepository.save(User("A", null))
        val request = UserUpdateRequest(savedUser.id!!, "B")

        //when
        userService.updateUserName(request)

        //then
        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("B")
    }

    @Test
    @DisplayName("유저 삭제 테스트")
    fun deleteUserTest() {
        //given
        userRepository.save(User("A", null))

        //when
        userService.deleteUser("A")

        //then
        assertThat(userRepository.findAll()).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 없는 멤버도 응답에 포함된다.")
    fun getUserLoaHistoriesTest1() {
        //given
        userRepository.save(User("A", null))

        //when
        val results = userService.getUserLoanHistories()

        //then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 많은 멤버의 응답이 정상 작동한다.")
    fun getUserLoaHistoriesTest2() {
        //given
        val savedUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "book1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "book2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUser, "book3", UserLoanStatus.RETURNED),
        ))

        //when
        val results = userService.getUserLoanHistories()

        //then
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("name")
            .containsExactlyInAnyOrder("book1", "book2", "book3")
        assertThat(results[0].books).extracting("isReturn")
            .containsExactlyInAnyOrder(true, false, false)
    }

    @Test
    @DisplayName("1/2 번 모두 테스트")
    fun getUserLoaHistoriesTest3() {

        // 복잡한 테스트 코드 하나보단 간단한 테스트 코드 2개가 낫다

        //given
        val savedUsers = userRepository.saveAll(listOf(
            User("A", null),
            User("B", null)
        ))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUsers[0], "book1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUsers[0], "book2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(savedUsers[0], "book3", UserLoanStatus.RETURNED),
        ))

        //when
        val results = userService.getUserLoanHistories()

        //then
        assertThat(results).hasSize(2)

        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("name")
            .containsExactlyInAnyOrder("book1", "book2", "book3")
        assertThat(results[0].books).extracting("isReturn")
            .containsExactlyInAnyOrder(true, false, false)

        assertThat(results[1].name).isEqualTo("B")
        assertThat(results[1].books).isEmpty()
    }

}
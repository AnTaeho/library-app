package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
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
    private val userService: UserService
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
        val request = UserUpdateRequest(savedUser.id, "B")

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

}
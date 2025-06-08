package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.querysentinel.annotation.EnableQuerySentinel;
import com.querysentinel.annotation.ExpectNoDb;
import com.querysentinel.annotation.ExpectQuery;
import com.querysentinel.annotation.ExpectTime;

@SpringBootTest
@EnableQuerySentinel
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @ExpectQuery(select = 2, insert = 1)
    @ExpectTime(300)
    @ExpectNoDb
    void testUser() {
        saveUser();
        List<User> users = loadUsers();
        assertThat(users).hasSize(1);
    }

    private void saveUser() {
        userRepository.save(new User("Alice", "alice@example.com"));
    }

    private List<User> loadUsers() {
        return userRepository.findAll();
    }
}
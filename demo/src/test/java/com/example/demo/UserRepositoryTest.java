package com.example.demo;

import com.querysentinel.annotation.EnableQuerySentinel;
import com.querysentinel.annotation.ExpectQuery;
import com.querysentinel.annotation.ExpectTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableQuerySentinel
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @ExpectQuery(select = 2, insert = 1)
    @ExpectTime(300)
    void findAll_expect() {
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
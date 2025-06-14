package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.querykeeper.annotation.EnableQueryKeeper;
import com.querykeeper.annotation.ExpectDetachedAccess;
import com.querykeeper.annotation.ExpectNoDb;
import com.querykeeper.annotation.ExpectNoTx;
import com.querykeeper.annotation.ExpectQuery;
import com.querykeeper.annotation.ExpectTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@EnableQueryKeeper
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @ExpectQuery(select = 1, insert = 1)
    @ExpectTime(500)
    @ExpectNoTx(strict = false)
    @ExpectNoDb
    void testCombinedAssertions() {
        User user = new User("Alice", "alice@example.com");
        user.addRole(new Role("ADMIN"));
        user.addRole(new Role("USER"));
        userRepository.save(user);
        userRepository.findAll();

        entityManager.clear();
        List<User> users = userRepository.findAll();
        users.get(0).getRoles().size();

        int sum = 0;
        for (int i = 0; i < 1000; i++)
            sum += i;
        assertThat(sum).isGreaterThan(0);
    }

    @Test
    @ExpectDetachedAccess
    void testDetachedAccess() {
        userService.triggerDetachedAccess();
    }
}
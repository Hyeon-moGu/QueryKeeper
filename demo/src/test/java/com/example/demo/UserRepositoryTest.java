package com.example.demo;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.querysentinel.annotation.EnableQuerySentinel;
import com.querysentinel.annotation.ExpectLazyLoad;
import com.querysentinel.annotation.ExpectNoTx;
import com.querysentinel.annotation.ExpectQuery;
import com.querysentinel.annotation.ExpectTime;

@SpringBootTest
@EnableQuerySentinel
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    @ExpectQuery(select = 1, insert = 1)
    @ExpectTime(300)
    @ExpectNoTx(strict = false)
    void testQueryTimeNoTx() {

        User user = new User("Alice", "alice@example.com");
        user.addRole(new Role("ADMIN"));
        userRepository.save(user);

        List<User> users = userRepository.findAll();
    }

    @Test
    @ExpectLazyLoad
    void testLazyLoad() {
        userService.triggerLazyException();
    }

    @Test
    @ExpectLazyLoad
    void testLazyLoad_pass() {
        List<User> users = userRepository.findAllWithRoles();
        for (User u : users) {
            System.out.println("Roles: " + u.getRoles().size());
        }
    }

}
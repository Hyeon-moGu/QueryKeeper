package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void triggerLazyException() {
        User user = new User("Alice", "alice@example.com");
        user.addRole(new Role("ADMIN"));
        userRepository.save(user);

        List<User> users = userRepository.findAll();
        for (User u : users) {
            System.out.println("Role count: " + u.getRoles().size());
        }
    }
}

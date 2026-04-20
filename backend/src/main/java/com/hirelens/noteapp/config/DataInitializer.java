package com.hirelens.noteapp.config;

import com.hirelens.noteapp.enums.Role;
import com.hirelens.noteapp.models.User;
import com.hirelens.noteapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@noteapp.com").isEmpty()) {
            User admin = new User();
            admin.setNickname("admin");
            admin.setEmail("admin@noteapp.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
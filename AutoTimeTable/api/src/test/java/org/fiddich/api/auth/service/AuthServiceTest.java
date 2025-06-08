package org.fiddich.api.auth.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AutoClose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional

class AuthServiceTest {

    @Autowired
    AuthService authService;

}
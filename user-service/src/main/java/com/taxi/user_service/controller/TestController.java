package com.taxi.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "User Service is running!";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
package com.oneum.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping
    public String index(){
        return "Hello Security!";
    }

    @GetMapping("/user/info")
    public String userInfo() {
        return "Hello User";
    }

    @GetMapping("/admin/info")
    public String adminInfo() {
        return "Hello admin";
    }
}

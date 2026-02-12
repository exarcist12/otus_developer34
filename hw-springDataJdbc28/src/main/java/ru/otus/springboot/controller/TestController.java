package ru.otus.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test-thymeleaf")
    public String test(Model model) {
        model.addAttribute("message", "Thymeleaf работает!");
        return "test";
    }
}

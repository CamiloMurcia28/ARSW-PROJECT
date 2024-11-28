package edu.escuelaing.co.leotankcicos.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@CrossOrigin
@RequiredArgsConstructor
public class LoginController {

    @GetMapping("/home")
    public String home() {
        return "Private home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Admin";
    }
}

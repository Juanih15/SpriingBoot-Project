package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService users;

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest req) {
        users.register(req.username(), req.password());
    }

    /** `/api/login` is handled automatically by Spring Security’s HTTP Basic. */
}

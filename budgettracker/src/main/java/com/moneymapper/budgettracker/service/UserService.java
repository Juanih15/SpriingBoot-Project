package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public User register(String username, String rawPassword) {
        User user = new User(username, encoder.encode(rawPassword));
        return repo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}

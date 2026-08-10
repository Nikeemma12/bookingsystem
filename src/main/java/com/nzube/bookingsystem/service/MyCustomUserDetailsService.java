package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.model.UserPrincipal;
import com.nzube.bookingsystem.repo.UserRepo;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@NullMarked
public class MyCustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    public MyCustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        return new UserPrincipal(user);
    }
}
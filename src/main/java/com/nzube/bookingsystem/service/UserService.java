package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.exception.UserNotFoundException;
import com.nzube.bookingsystem.dto.BookingsResponseDto;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.repo.UserRepo;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public UserService(UserRepo userRepo, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }


    public User registerUser(String name, String password, String email, String role){
        User user = new User();
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(role);
        return userRepo.save(user);
    }
    public User addUser(String name){
        User user = new User();
        user.setName(name);
        return userRepo.save(user);
    }

    public List<User> getAllUsers() {
      return userRepo.findAll();
    }

    public User getUserById(int userId) {
        return userRepo.findById(userId).orElseThrow(()->new UserNotFoundException("User not found"));
    }

    public List<BookingsResponseDto> getUserBookings(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(()->new UserNotFoundException("User not found"))
                .getBookings().stream().map(BookingsResponseDto::from).toList();
    }

    public String loginUser(String email, String password) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        return jwtService.generateToken(email);
    }
}

package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.dto.UserRegisterDto;
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


    public User registerUser(UserRegisterDto userRegisterDto){
        User user = new User();
        user.setName(userRegisterDto.name());
        user.setPassword(passwordEncoder.encode(userRegisterDto.password()));
        user.setEmail(userRegisterDto.email());
        user.setRole("ROLE_USER");
        return userRepo.save(user);
    }
    public User registerByAdmin(UserRegisterDto userRegisterDto){
        User user = new User();
        user.setName(userRegisterDto.name());
        user.setPassword(passwordEncoder.encode(userRegisterDto.password()));
        user.setEmail(userRegisterDto.email());
        if (userRegisterDto.role().equals("ROLE_ADMIN")) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }
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

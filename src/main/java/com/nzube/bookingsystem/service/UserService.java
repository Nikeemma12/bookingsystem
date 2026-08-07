package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.exception.UserNotFoundException;

import com.nzube.bookingsystem.model.BookingsResponseDto;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.repo.UserRepo;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service

public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User addUser(User user){
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

}

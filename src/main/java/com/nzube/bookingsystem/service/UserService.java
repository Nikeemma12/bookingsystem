package com.nzube.bookingsystem.service;

import com.nzube.bookingsystem.exception.UserNotFoundException;

import com.nzube.bookingsystem.dto.BookingsResponseDto;
import com.nzube.bookingsystem.model.User;
import com.nzube.bookingsystem.repo.UserRepo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
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

}

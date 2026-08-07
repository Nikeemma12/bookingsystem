package com.nzube.bookingsystem;


import com.nzube.bookingsystem.service.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingRaceConditionTest {

    @Autowired
    private BookingService bookingService;

    @Test
    void twoUsersBookingSameSeat_onlyOneShouldSucceed() throws InterruptedException {
        int seatId = 9;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        BiFunction<Integer, String, Runnable> bookAttempt = (userId, key) -> () -> {
            try {
                startLatch.await(); // both threads wait here until released together
                bookingService.createBookings(userId, List.of(seatId), key);
                successCount.incrementAndGet();
            } catch (Exception e) {
                System.out.println("Failure reason: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        };

        new Thread(bookAttempt.apply(1, "race-key-3")).start();
        new Thread(bookAttempt.apply(2, "race-key-4")).start();

        startLatch.countDown(); // release both threads at the exact same moment
        doneLatch.await(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

    }
}
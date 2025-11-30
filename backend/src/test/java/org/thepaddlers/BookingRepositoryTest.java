package org.thepaddlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thepaddlers.model.Booking;
import org.thepaddlers.model.Court;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.BookingRepository;
import org.thepaddlers.repository.CourtRepository;
import org.thepaddlers.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingRepositoryTest {
    @Autowired BookingRepository bookingRepository;
    @Autowired CourtRepository courtRepository;
    @Autowired UserRepository userRepository;

    @Test
    public void testBookingOverlapCases() {
        Court court = courtRepository.save(new Court());
        User user = userRepository.save(new User());
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(1);
        Booking b1 = new Booking();
        b1.setCourt(court); b1.setUser(user); b1.setStartAt(start); b1.setEndAt(end);
        bookingRepository.save(b1);

        // Adjacent: allowed
        Booking b2 = new Booking();
        b2.setCourt(court); b2.setUser(user); b2.setStartAt(end); b2.setEndAt(end.plusHours(1));
        assertTrue(bookingRepository.findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(court.getId(), b2.getEndAt(), b2.getStartAt()).isEmpty());

        // Exact overlap: blocked
        Booking b3 = new Booking();
        b3.setCourt(court); b3.setUser(user); b3.setStartAt(start); b3.setEndAt(end);
        assertFalse(bookingRepository.findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(court.getId(), b3.getEndAt(), b3.getStartAt()).isEmpty());

        // Contained inside another: blocked
        Booking b4 = new Booking();
        b4.setCourt(court); b4.setUser(user); b4.setStartAt(start.plusMinutes(10)); b4.setEndAt(end.minusMinutes(10));
        assertFalse(bookingRepository.findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(court.getId(), b4.getEndAt(), b4.getStartAt()).isEmpty());

        // Surrounding: blocked
        Booking b5 = new Booking();
        b5.setCourt(court); b5.setUser(user); b5.setStartAt(start.minusMinutes(10)); b5.setEndAt(end.plusMinutes(10));
        assertFalse(bookingRepository.findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(court.getId(), b5.getEndAt(), b5.getStartAt()).isEmpty());
    }
}


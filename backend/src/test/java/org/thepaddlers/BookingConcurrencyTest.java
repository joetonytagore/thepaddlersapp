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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingConcurrencyTest {
    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    CourtRepository courtRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    public void concurrentBookingCreatesAtMostOne() throws InterruptedException, ExecutionException {
        // Prepare: ensure a court and two users exist
        Court court = new Court();
        court.setName("ConcurrencyCourt");
        Court savedCourt = courtRepository.save(court);

        User u1 = new User(); u1.setName("u1"); u1.setEmail("u1@example.com"); User savedU1 = userRepository.save(u1);
        User u2 = new User(); u2.setName("u2"); u2.setEmail("u2@example.com"); User savedU2 = userRepository.save(u2);

        final Court finalCourt = savedCourt;
        final User finalU1 = savedU1;
        final User finalU2 = savedU2;

        OffsetDateTime start = OffsetDateTime.now().plusMinutes(10);
        OffsetDateTime end = start.plusHours(1);

        int threads = 6;
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            tasks.add(() -> {
                try {
                    Booking b = new Booking();
                    b.setCourt(finalCourt);
                    b.setUser(idx % 2 == 0 ? finalU1 : finalU2);
                    b.setStartAt(start);
                    b.setEndAt(end);
                    bookingRepository.save(b);
                    return true;
                } catch (Exception exx) {
                    // expected for conflicting attempts
                    return false;
                }
            });
        }

        List<Future<Boolean>> results = ex.invokeAll(tasks);
        ex.shutdown();
        ex.awaitTermination(30, TimeUnit.SECONDS);

        int success = 0;
        for (Future<Boolean> f : results) if (f.get()) success++;

        // Only one booking should have succeeded for the same court/time
        assertEquals(1, bookingRepository.findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(court.getId(), end, start).size());
    }
}

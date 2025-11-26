package org.thepaddlers;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.thepaddlers.model.Court;
import org.thepaddlers.model.User;
import org.thepaddlers.model.Booking;
import org.thepaddlers.model.Role;
import org.thepaddlers.repository.CourtRepository;
import org.thepaddlers.repository.UserRepository;
import org.thepaddlers.repository.BookingRepository;

import java.time.OffsetDateTime;

@Component
public class DataLoader {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;

    public DataLoader(UserRepository userRepository, CourtRepository courtRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
    }

    @PostConstruct
    public void load() {
        // Wrap seeding logic to avoid failing application startup if the DB schema
        // is missing columns (e.g. role). We log and skip seeding if DB access fails.
        try {
            if (userRepository.count() == 0) {
                User u = new User();
                u.setName("Demo User");
                u.setEmail("demo@paddlers.test");
                u.setRole(Role.ROLE_PLAYER);
                userRepository.save(u);
                System.out.println("Seeded user: " + u.getEmail());

                // seed an admin user for local testing
                User admin = new User();
                admin.setName("Local Admin");
                admin.setEmail("admin@paddlers.test");
                admin.setRole(Role.ROLE_ADMIN);
                userRepository.save(admin);
                System.out.println("Seeded admin user: " + admin.getEmail());
            }
            if (courtRepository.count() == 0) {
                Court c = new Court();
                c.setName("Court 1");
                courtRepository.save(c);
                System.out.println("Seeded court: " + c.getName());
            }

            // seed a booking if none exists (1 hour from now for 1 hour)
            if (bookingRepository.count() == 0) {
                // get first user & court
                User user = userRepository.findAll().stream().findFirst().orElse(null);
                Court court = courtRepository.findAll().stream().findFirst().orElse(null);
                if (user != null && court != null) {
                    Booking b = new Booking();
                    OffsetDateTime start = OffsetDateTime.now().plusHours(1).withNano(0);
                    b.setStartAt(start);
                    b.setEndAt(start.plusHours(1));
                    b.setCourt(court);
                    b.setUser(user);
                    bookingRepository.save(b);
                    System.out.println("Seeded booking: user=" + user.getEmail() + " court=" + court.getName() + " start=" + start);
                }
            }
        } catch (DataAccessException dae) {
            log.warn("Database schema not ready for seeding; skipping data seeding. Reason: {}", dae.getMessage());
        } catch (Exception ex) {
            log.warn("Unexpected error during data seeding; skipping. Will not prevent application startup.", ex);
        }
    }
}

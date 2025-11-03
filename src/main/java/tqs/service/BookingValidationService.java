package tqs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tqs.data.Booking.BookingRepository;
import tqs.data.BulkItem.BulkItem;
import tqs.data.BookingStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service responsible for validating booking business rules
 */
@Service
public class BookingValidationService {

    private static final Logger logger = LoggerFactory.getLogger(BookingValidationService.class);
    
    // Business rules as constants
    private static final int MAX_ADVANCE_DAYS = 90;
    private static final int MIN_ADVANCE_DAYS = 1;
    private static final int MAX_DAILY_BOOKINGS_PER_MUNICIPALITY = 10;

    private final BookingRepository bookingRepository;

    public BookingValidationService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }


    public boolean validateBookingDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.plusDays(MIN_ADVANCE_DAYS);
        LocalDate maxDate = today.plusDays(MAX_ADVANCE_DAYS);

        boolean valid = !date.isBefore(minDate) && !date.isAfter(maxDate);
        
        if (!valid) {
            logger.warn("Invalid booking date. Must be between {} and {}", minDate, maxDate);
        }
        
        return valid;
    }


    public boolean canAcceptBooking(String municipality, LocalDate date) {
        // Count active bookings (not cancelled or completed) for the specific date
        long activeBookings = bookingRepository.findByMunicipality(municipality).stream()
                .filter(b -> b.getCollectionDate().equals(date))
                .filter(b -> b.getCurrentStatus() != BookingStatus.CANCELLED)
                .filter(b -> b.getCurrentStatus() != BookingStatus.COMPLETED)
                .count();

        boolean canAccept = activeBookings < MAX_DAILY_BOOKINGS_PER_MUNICIPALITY;
        
        if (!canAccept) {
            logger.warn("This Municipality has reached capacity for the specified date.");
        }
        
        return canAccept;
    }
    
    public boolean validateBulkItems(List<BulkItem> items) {
        boolean valid = items != null && !items.isEmpty();
        
        if (!valid) {
            logger.warn("Booking validation failed: No items provided");
        }
        
        return valid;
    }
}
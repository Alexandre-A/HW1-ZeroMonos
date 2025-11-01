package tqs.data.StatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tqs.data.BookingStatus;
import tqs.data.Booking.Booking;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for StatusHistory entity
 */
@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    
    List<StatusHistory> findByBookingOrderByDatetimeAsc(Booking booking);
    
    List<StatusHistory> findByBookingIdOrderByDatetimeAsc(Long bookingId);
    
    List<StatusHistory> findByStatus(BookingStatus status);
    
    List<StatusHistory> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);
    
    long countByBooking(Booking booking);
}

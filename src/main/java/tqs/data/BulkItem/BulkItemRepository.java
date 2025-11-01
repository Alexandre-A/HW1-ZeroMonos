package tqs.data.BulkItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tqs.data.Booking.Booking;

import java.util.List;

/**
 * Repository interface for BulkItem entity
 */
@Repository
public interface BulkItemRepository extends JpaRepository<BulkItem, Long> {
    
    List<BulkItem> findByBooking(Booking booking);
    
    List<BulkItem> findByBookingId(Long bookingId);
    
    long countByBooking(Booking booking);
    
    List<BulkItem> findByNameContainingIgnoreCase(String name);
}

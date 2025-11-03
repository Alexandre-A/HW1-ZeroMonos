package tqs.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tqs.data.Booking.Booking;
import tqs.data.StatusHistory.StatusHistory;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StatusHistory entity
 */
@DisplayName("StatusHistory Entity Tests")
class StatusHistoryTest {

    private StatusHistory statusHistory;
    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking("Porto", LocalDate.now(), "Morning");
        statusHistory = new StatusHistory(BookingStatus.RECEIVED, booking);
    }

    @Test
    @DisplayName("Should create status history with valid data")
    void testCreateStatusHistory() {
        assertThat(statusHistory).isNotNull();
        assertThat(statusHistory.getStatus()).isEqualTo(BookingStatus.RECEIVED);
        assertThat(statusHistory.getBooking()).isEqualTo(booking);
    }

    @Test
    @DisplayName("Should properly set and get all fields")
    void testGettersAndSetters() {
        StatusHistory history = new StatusHistory();
        
        history.setStatus(BookingStatus.ASSIGNED);
        history.setBooking(booking);

        assertThat(history.getStatus()).isEqualTo(BookingStatus.ASSIGNED);
        assertThat(history.getBooking()).isEqualTo(booking);
    }

    @Test
    @DisplayName("Should track all booking statuses")
    void testAllStatusTypes() {
        StatusHistory received = new StatusHistory(BookingStatus.RECEIVED, booking);
        StatusHistory assigned = new StatusHistory(BookingStatus.ASSIGNED, booking);
        StatusHistory inProgress = new StatusHistory(BookingStatus.IN_PROGRESS, booking);
        StatusHistory completed = new StatusHistory(BookingStatus.COMPLETED, booking);
        StatusHistory cancelled = new StatusHistory(BookingStatus.CANCELLED, booking);

        assertThat(received.getStatus()).isEqualTo(BookingStatus.RECEIVED);
        assertThat(assigned.getStatus()).isEqualTo(BookingStatus.ASSIGNED);
        assertThat(inProgress.getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(completed.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void testToString() {
        String str = statusHistory.toString();
        
        assertThat(str).contains("StatusHistory");
        assertThat(str).contains("RECEIVED");
    }

    @Test
    @DisplayName("Should maintain reference to booking")
    void testBookingReference() {
        StatusHistory history1 = new StatusHistory(BookingStatus.RECEIVED, booking);
        StatusHistory history2 = new StatusHistory(BookingStatus.ASSIGNED, booking);

        assertThat(history1.getBooking()).isEqualTo(booking);
        assertThat(history2.getBooking()).isEqualTo(booking);
        assertThat(history1.getBooking()).isEqualTo(history2.getBooking());
    }

    @Test
    @DisplayName("Should support status change sequence")
    void testStatusChangeSequence() {
        Booking testBooking = new Booking("Lisbon", LocalDate.now(), "Afternoon");
        
        StatusHistory step1 = new StatusHistory(BookingStatus.RECEIVED, testBooking);
        StatusHistory step2 = new StatusHistory(BookingStatus.ASSIGNED, testBooking);
        StatusHistory step3 = new StatusHistory(BookingStatus.IN_PROGRESS, testBooking);
        StatusHistory step4 = new StatusHistory(BookingStatus.COMPLETED, testBooking);

        testBooking.addStatusHistory(step1);
        testBooking.addStatusHistory(step2);
        testBooking.addStatusHistory(step3);
        testBooking.addStatusHistory(step4);

        assertThat(testBooking.getStatusHistories()).hasSize(5); // Initial RECEIVED + 4 added
        assertThat(testBooking.getStatusHistories().get(0).getStatus()).isEqualTo(BookingStatus.RECEIVED);
        assertThat(testBooking.getStatusHistories().get(4).getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should handle cancellation status")
    void testCancellationStatus() {
        StatusHistory received = new StatusHistory(BookingStatus.RECEIVED, booking);
        StatusHistory cancelled = new StatusHistory(BookingStatus.CANCELLED, booking);

        booking.addStatusHistory(received);
        booking.addStatusHistory(cancelled);

        assertThat(booking.getStatusHistories()).hasSize(3); // Initial RECEIVED + 2 added
        assertThat(booking.getStatusHistories().get(2).getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should properly handle equals and hashCode")
    void testEqualsAndHashCode() {
        StatusHistory history1 = new StatusHistory(BookingStatus.RECEIVED, booking);
        StatusHistory history2 = new StatusHistory(BookingStatus.ASSIGNED, booking);
        
        // Test equals with same object
        assertThat(history1.equals(history1)).isTrue();
        
        // Test equals with different type
        assertThat(history1.equals("not a status history")).isFalse();
        
        // Test equals with null
        assertThat(history1.equals(null)).isFalse();
        
        // Test hashCode consistency
        assertThat(history1.hashCode()).isEqualTo(history1.hashCode());
        assertThat(history2.hashCode()).isEqualTo(history2.hashCode());
    }

    @Test
    @DisplayName("Should properly set id and maintain datetime")
    void testIdAndDatetimeHandling() {
        StatusHistory history = new StatusHistory(BookingStatus.RECEIVED, booking);
        
        // Test setting ID
        history.setId(1L);
        assertThat(history.getId()).isEqualTo(1L);
        
        // Test setting datetime
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        history.setDatetime(now);
        assertThat(history.getDatetime()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should include datetime in toString when set")
    void testToStringWithDatetime() {
        StatusHistory history = new StatusHistory(BookingStatus.ASSIGNED, booking);
        history.setDatetime(java.time.LocalDateTime.of(2025, 11, 1, 14, 30));
        
        String str = history.toString();
        assertThat(str).contains("StatusHistory");
        assertThat(str).contains("ASSIGNED");
        assertThat(str).contains("2025-11-01T14:30");
    }

    @Test
    @DisplayName("Should properly set and get booking")
    void testSetBooking() {
        StatusHistory history = new StatusHistory();
        
        history.setBooking(booking);
        assertThat(history.getBooking()).isEqualTo(booking);
        
        // Change booking
        Booking newBooking = new Booking("Lisbon", LocalDate.now(), "Evening");
        history.setBooking(newBooking);
        assertThat(history.getBooking()).isEqualTo(newBooking);
    }
}

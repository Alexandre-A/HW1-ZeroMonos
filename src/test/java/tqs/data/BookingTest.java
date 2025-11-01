package tqs.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tqs.data.Booking.Booking;
import tqs.data.BulkItem.BulkItem;
import tqs.data.StatusHistory.StatusHistory;
import tqs.data.state.InvalidStateTransitionException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Booking entity
 */
@DisplayName("Booking Entity Tests")
class BookingTest {

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking("Porto", LocalDate.of(2025, 11, 15), "Morning (9:00-12:00)");
    }

    @Test
    @DisplayName("Should create booking with valid data")
    void testCreateBooking() {
        assertThat(booking).isNotNull();
        assertThat(booking.getMunicipality()).isEqualTo("Porto");
        assertThat(booking.getCollectionDate()).isEqualTo(LocalDate.of(2025, 11, 15));
        assertThat(booking.getTimeSlot()).isEqualTo("Morning (9:00-12:00)");
    }

    @Test
    @DisplayName("Should generate unique access token on creation")
    void testAccessTokenGeneration() {
        Booking booking1 = new Booking("Porto", LocalDate.now(), "Morning");
        Booking booking2 = new Booking("Lisbon", LocalDate.now(), "Afternoon");

        assertThat(booking1.getAccessToken()).isNotNull();
        assertThat(booking2.getAccessToken()).isNotNull();
        assertThat(booking1.getAccessToken()).isNotEqualTo(booking2.getAccessToken());
    }

    @Test
    @DisplayName("Should initialize with RECEIVED status")
    void testInitialStatus() {
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);
    }

    @Test
    @DisplayName("Should add bulk item correctly")
    void testAddBulkItem() {
        BulkItem item = new BulkItem("Sofa", "Old leather sofa", 50.0f, 2.5f);
        
        booking.addBulkItem(item);

        assertThat(booking.getBulkItems()).hasSize(1);
        assertThat(booking.getBulkItems()).contains(item);
        assertThat(item.getBooking()).isEqualTo(booking);
    }

    @Test
    @DisplayName("Should remove bulk item correctly")
    void testRemoveBulkItem() {
        BulkItem item = new BulkItem("Sofa", "Old leather sofa", 50.0f, 2.5f);
        
        booking.addBulkItem(item);
        assertThat(booking.getBulkItems()).hasSize(1);

        booking.removeBulkItem(item);
        assertThat(booking.getBulkItems()).isEmpty();
        assertThat(item.getBooking()).isNull();
    }

    @Test
    @DisplayName("Should add multiple bulk items")
    void testAddMultipleBulkItems() {
        BulkItem item1 = new BulkItem("Sofa", "Old sofa", 50.0f, 2.5f);
        BulkItem item2 = new BulkItem("Mattress", "King size", 30.0f, 1.8f);
        BulkItem item3 = new BulkItem("Fridge", "Old refrigerator", 70.0f, 0.8f);

        booking.addBulkItem(item1);
        booking.addBulkItem(item2);
        booking.addBulkItem(item3);

        assertThat(booking.getBulkItems()).hasSize(3);
        assertThat(booking.getBulkItems()).containsExactly(item1, item2, item3);
    }

    @Test
    @DisplayName("Should add status history correctly")
    void testAddStatusHistory() {
        StatusHistory history = new StatusHistory(BookingStatus.ASSIGNED, booking);
        
        booking.addStatusHistory(history);

        assertThat(booking.getStatusHistories()).hasSize(1);
        assertThat(booking.getStatusHistories()).contains(history);
        assertThat(history.getBooking()).isEqualTo(booking);
    }

    @Test
    @DisplayName("Should remove status history correctly")
    void testRemoveStatusHistory() {
        StatusHistory history = new StatusHistory(BookingStatus.ASSIGNED, booking);
        
        booking.addStatusHistory(history);
        assertThat(booking.getStatusHistories()).hasSize(1);

        booking.removeStatusHistory(history);
        assertThat(booking.getStatusHistories()).isEmpty();
        assertThat(history.getBooking()).isNull();
    }

    @Test
    @DisplayName("Should track status changes")
    void testStatusChanges() {
        StatusHistory history1 = new StatusHistory(BookingStatus.RECEIVED, booking);
        StatusHistory history2 = new StatusHistory(BookingStatus.ASSIGNED, booking);
        StatusHistory history3 = new StatusHistory(BookingStatus.IN_PROGRESS, booking);

        booking.addStatusHistory(history1);
        booking.addStatusHistory(history2);
        booking.addStatusHistory(history3);

        assertThat(booking.getStatusHistories()).hasSize(3);
        assertThat(booking.getStatusHistories()).containsExactly(history1, history2, history3);
    }

    @Test
    @DisplayName("Should update current status")
    void testUpdateCurrentStatus() {
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);

        booking.setCurrentStatus(BookingStatus.ASSIGNED);
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.ASSIGNED);

        booking.setCurrentStatus(BookingStatus.COMPLETED);
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void testToString() {
        String str = booking.toString();
        
        assertThat(str).contains("Booking").contains("Porto");
        assertThat(str).contains("Morning");
        assertThat(str).contains(booking.getAccessToken());
    }

    @Test
    @DisplayName("Should properly set and get all fields")
    void testGettersAndSetters() {
        Booking newBooking = new Booking();
        
        newBooking.setMunicipality("Lisbon");
        newBooking.setCollectionDate(LocalDate.of(2025, 12, 1));
        newBooking.setTimeSlot("Afternoon (14:00-17:00)");
        newBooking.setCurrentStatus(BookingStatus.ASSIGNED);

        assertThat(newBooking.getMunicipality()).isEqualTo("Lisbon");
        assertThat(newBooking.getCollectionDate()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(newBooking.getTimeSlot()).isEqualTo("Afternoon (14:00-17:00)");
        assertThat(newBooking.getCurrentStatus()).isEqualTo(BookingStatus.ASSIGNED);
        assertThat(newBooking.getAccessToken()).isNotNull(); // Generated in constructor
    }

    @Test
    @DisplayName("Should initialize empty collections")
    void testEmptyCollections() {
        Booking newBooking = new Booking();
        
        assertThat(newBooking.getBulkItems()).isNotNull();
        assertThat(newBooking.getBulkItems()).isEmpty();
        assertThat(newBooking.getStatusHistories()).isNotNull();
        assertThat(newBooking.getStatusHistories()).isEmpty();
    }

    // State Transition Tests

    @Test
    @DisplayName("Should transition from RECEIVED to ASSIGNED using assign()")
    void testAssignFromReceived() {
        // Given - booking starts in RECEIVED state
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);

        // When
        booking.assign();

        // Then
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.ASSIGNED);
        assertThat(booking.getStatusHistories()).hasSize(1);
        assertThat(booking.getStatusHistories().get(0).getStatus()).isEqualTo(BookingStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Should transition from ASSIGNED to IN_PROGRESS using start()")
    void testStartFromAssigned() {
        // Given
        booking.setCurrentStatus(BookingStatus.ASSIGNED);

        // When
        booking.start();

        // Then
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(booking.getStatusHistories()).hasSize(1);
        assertThat(booking.getStatusHistories().get(0).getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should transition from IN_PROGRESS to COMPLETED using complete()")
    void testCompleteFromInProgress() {
        // Given
        booking.setCurrentStatus(BookingStatus.IN_PROGRESS);

        // When
        booking.complete();

        // Then
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(booking.getStatusHistories()).hasSize(1);
        assertThat(booking.getStatusHistories().get(0).getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should cancel booking from RECEIVED state")
    void testCancelFromReceived() {
        // Given - booking starts in RECEIVED state
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);

        // When
        booking.cancel();

        // Then
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getStatusHistories()).hasSize(1);
        assertThat(booking.getStatusHistories().get(0).getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should cancel booking from ASSIGNED state")
    void testCancelFromAssigned() {
        // Given
        booking.setCurrentStatus(BookingStatus.ASSIGNED);

        // When
        booking.cancel();

        // Then
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should cancel booking from IN_PROGRESS state")
    void testCancelFromInProgress() {
        // Given
        booking.setCurrentStatus(BookingStatus.IN_PROGRESS);

        // When
        booking.cancel();

        // Then
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw exception when starting from RECEIVED state")
    void testInvalidStartFromReceived() {
        // Given - booking starts in RECEIVED state
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);

        // When/Then
        assertThatThrownBy(() -> booking.start())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("start")
                .hasMessageContaining("RECEIVED");
    }

    @Test
    @DisplayName("Should throw exception when completing from RECEIVED state")
    void testInvalidCompleteFromReceived() {
        // Given - booking starts in RECEIVED state
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);

        // When/Then
        assertThatThrownBy(() -> booking.complete())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("complete")
                .hasMessageContaining("RECEIVED");
    }

    @Test
    @DisplayName("Should throw exception when assigning from ASSIGNED state")
    void testInvalidAssignFromAssigned() {
        // Given
        booking.setCurrentStatus(BookingStatus.ASSIGNED);

        // When/Then
        assertThatThrownBy(() -> booking.assign())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("assign")
                .hasMessageContaining("ASSIGNED");
    }

    @Test
    @DisplayName("Should throw exception when cancelling from COMPLETED state")
    void testInvalidCancelFromCompleted() {
        // Given
        booking.setCurrentStatus(BookingStatus.COMPLETED);

        // When/Then
        assertThatThrownBy(() -> booking.cancel())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("cancel")
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("Should complete full booking lifecycle")
    void testCompleteLifecycle() {
        // Given - starts as RECEIVED
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);

        // When - go through full lifecycle
        booking.assign();
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.ASSIGNED);

        booking.start();
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.IN_PROGRESS);

        booking.complete();
        assertThat(booking.getCurrentStatus()).isEqualTo(BookingStatus.COMPLETED);

        // Then - should have 3 status history entries
        assertThat(booking.getStatusHistories()).hasSize(3);
        assertThat(booking.getStatusHistories().get(0).getStatus()).isEqualTo(BookingStatus.ASSIGNED);
        assertThat(booking.getStatusHistories().get(1).getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(booking.getStatusHistories().get(2).getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should not create status history when status doesn't change")
    void testNoHistoryWhenNoChange() {
        // This would happen if someone calls setCurrentStatus directly with same value
        booking.setCurrentStatus(BookingStatus.RECEIVED);
        
        int initialHistorySize = booking.getStatusHistories().size();
        
        // Calling setCurrentStatus with same value shouldn't add history
        // (only the state transition methods add history)
        booking.setCurrentStatus(BookingStatus.RECEIVED);
        
        assertThat(booking.getStatusHistories()).hasSize(initialHistorySize);
    }

    @Test
    @DisplayName("Should properly handle equals and hashCode")
    void testEqualsAndHashCode() {
        Booking booking1 = new Booking("Porto", LocalDate.now(), "Morning");
        Booking booking2 = new Booking("Lisbon", LocalDate.now(), "Afternoon");
        
        // Test equals with same object
        assertThat(booking1.equals(booking1)).isTrue();
        
        // Test equals with different type
        assertThat(booking1.equals("not a booking")).isFalse();
        
        // Test equals with null
        assertThat(booking1.equals(null)).isFalse();
        
        // Test hashCode consistency
        assertThat(booking1.hashCode()).isEqualTo(booking1.hashCode());
        assertThat(booking2.hashCode()).isEqualTo(booking2.hashCode());
    }

    @Test
    @DisplayName("Should properly set id and version")
    void testIdAndVersionHandling() {
        Booking newBooking = new Booking("Braga", LocalDate.now(), "Morning");
        
        // Test setting ID
        newBooking.setId(1L);
        assertThat(newBooking.getId()).isEqualTo(1L);
        
        // Test version field
        assertThat(newBooking.getVersion()).isEqualTo(0L);
        
        // Test setting version
        newBooking.setVersion(5L);
        assertThat(newBooking.getVersion()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should maintain bidirectional relationship with bulk items")
    void testBidirectionalBulkItemRelationship() {
        BulkItem item1 = new BulkItem("Sofa", "Old sofa", 50.0f, 2.5f);
        BulkItem item2 = new BulkItem("Table", "Dining table", 20.0f, 1.0f);
        
        booking.addBulkItem(item1);
        booking.addBulkItem(item2);
        
        // Both items should reference the booking
        assertThat(item1.getBooking()).isEqualTo(booking);
        assertThat(item2.getBooking()).isEqualTo(booking);
        
        // Booking should have both items
        assertThat(booking.getBulkItems()).containsExactly(item1, item2);
        
        // Remove one item
        booking.removeBulkItem(item1);
        assertThat(item1.getBooking()).isNull();
        assertThat(booking.getBulkItems()).containsExactly(item2);
    }

    @Test
    @DisplayName("Should maintain bidirectional relationship with status histories")
    void testBidirectionalStatusHistoryRelationship() {
        StatusHistory history1 = new StatusHistory(BookingStatus.ASSIGNED, booking);
        StatusHistory history2 = new StatusHistory(BookingStatus.IN_PROGRESS, booking);
        
        booking.addStatusHistory(history1);
        booking.addStatusHistory(history2);
        
        // Both histories should reference the booking
        assertThat(history1.getBooking()).isEqualTo(booking);
        assertThat(history2.getBooking()).isEqualTo(booking);
        
        // Booking should have both histories
        assertThat(booking.getStatusHistories()).containsExactly(history1, history2);
        
        // Remove one history
        booking.removeStatusHistory(history1);
        assertThat(history1.getBooking()).isNull();
        assertThat(booking.getStatusHistories()).containsExactly(history2);
    }

    @Test
    @DisplayName("Should handle created timestamp")
    void testCreatedAtTimestamp() {
        // Created timestamp should be set (even if null before persistence)
        assertThat(booking.getCreatedAt()).isNull(); // Not persisted yet
        
        // Can manually set for testing
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        booking.setCreatedAt(now);
        assertThat(booking.getCreatedAt()).isEqualTo(now);
    }
}


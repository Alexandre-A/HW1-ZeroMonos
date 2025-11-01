package tqs.data.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tqs.data.BookingStatus;
import tqs.data.Booking.Booking;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for BookingStateFactory
 */
class BookingStateFactoryTest {

    @Test
    @DisplayName("Should create ReceivedState for RECEIVED status")
    void testCreateReceivedState() {
        // Given
        Booking booking = new Booking("Porto", LocalDate.now().plusDays(3), "morning");

        // When
        BookingState state = BookingStateFactory.getState(booking);

        // Then
        assertThat(state).isInstanceOf(ReceivedState.class);
        assertThat(state.getStateName()).isEqualTo("RECEIVED");
    }

    @Test
    @DisplayName("Should create AssignedState for ASSIGNED status")
    void testCreateAssignedState() {
        // Given
        Booking booking = new Booking("Lisboa", LocalDate.now().plusDays(2), "afternoon");
        booking.setCurrentStatus(BookingStatus.ASSIGNED);

        // When
        BookingState state = BookingStateFactory.getState(booking);

        // Then
        assertThat(state).isInstanceOf(AssignedState.class);
        assertThat(state.getStateName()).isEqualTo("ASSIGNED");
    }

    @Test
    @DisplayName("Should create InProgressState for IN_PROGRESS status")
    void testCreateInProgressState() {
        // Given
        Booking booking = new Booking("Braga", LocalDate.now(), "morning");
        booking.setCurrentStatus(BookingStatus.IN_PROGRESS);

        // When
        BookingState state = BookingStateFactory.getState(booking);

        // Then
        assertThat(state).isInstanceOf(InProgressState.class);
        assertThat(state.getStateName()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("Should create CompletedState for COMPLETED status")
    void testCreateCompletedState() {
        // Given
        Booking booking = new Booking("Coimbra", LocalDate.now().minusDays(1), "afternoon");
        booking.setCurrentStatus(BookingStatus.COMPLETED);

        // When
        BookingState state = BookingStateFactory.getState(booking);

        // Then
        assertThat(state).isInstanceOf(CompletedState.class);
        assertThat(state.getStateName()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Should create CancelledState for CANCELLED status")
    void testCreateCancelledState() {
        // Given
        Booking booking = new Booking("Faro", LocalDate.now().plusDays(1), "evening");
        booking.setCurrentStatus(BookingStatus.CANCELLED);

        // When
        BookingState state = BookingStateFactory.getState(booking);

        // Then
        assertThat(state).isInstanceOf(CancelledState.class);
        assertThat(state.getStateName()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("Should throw exception when booking is null")
    void testNullBooking() {
        // When/Then
        assertThatThrownBy(() -> BookingStateFactory.getState(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Booking and status cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when status is null")
    void testNullStatus() {
        // Given
        Booking booking = new Booking();
        booking.setCurrentStatus(null);

        // When/Then
        assertThatThrownBy(() -> BookingStateFactory.getState(booking))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Booking and status cannot be null");
    }
}

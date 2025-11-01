package tqs.data.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import tqs.data.BookingStatus;
import tqs.data.Booking.Booking;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive parameterized tests for all booking state transitions
 */
@DisplayName("Booking State Transitions")
class BookingStateTransitionsTest {

    /**
     * Test valid state transitions
     */
    @ParameterizedTest(name = "{0} -> {1} via {2}()")
    @MethodSource("validTransitions")
    @DisplayName("Should allow valid state transitions")
    void testValidTransitions(BookingStatus fromStatus, BookingStatus toStatus, String action) {
        // Given
        Booking booking = new Booking("Porto", LocalDate.now().plusDays(7), "morning");
        booking.setCurrentStatus(fromStatus);
        BookingState state = BookingStateFactory.getState(booking);

        // When
        switch (action) {
            case "assign" -> state.assign();
            case "start" -> state.start();
            case "complete" -> state.complete();
            case "cancel" -> state.cancel();
        }

        // Then
        assertThat(booking.getCurrentStatus()).isEqualTo(toStatus);
    }

    /**
     * Test invalid state transitions
     */
    @ParameterizedTest(name = "{0}: {1}() should fail")
    @MethodSource("invalidTransitions")
    @DisplayName("Should reject invalid state transitions")
    void testInvalidTransitions(BookingStatus currentStatus, String action) {
        // Given
        Booking booking = new Booking("Porto", LocalDate.now().plusDays(7), "morning");
        booking.setCurrentStatus(currentStatus);
        BookingState state = BookingStateFactory.getState(booking);

        // When/Then
        assertThatThrownBy(() -> {
            switch (action) {
                case "assign" -> state.assign();
                case "start" -> state.start();
                case "complete" -> state.complete();
                case "cancel" -> state.cancel();
            }
        })
        .isInstanceOf(InvalidStateTransitionException.class)
        .hasMessageContaining("Cannot perform action '" + action + "' in state '" + currentStatus + "'");
    }

    /**
     * Test that state names match their BookingStatus
     */
    @ParameterizedTest(name = "{0} state name")
    @MethodSource("stateNames")
    @DisplayName("Should return correct state name")
    void testStateNames(BookingStatus status, String expectedName) {
        // Given
        Booking booking = new Booking("Porto", LocalDate.now().plusDays(7), "morning");
        booking.setCurrentStatus(status);
        
        // When
        BookingState state = BookingStateFactory.getState(booking);
        
        // Then
        assertThat(state.getStateName()).isEqualTo(expectedName);
    }

    // ==================== Test Data Providers ====================

    /**
     * Provides all valid state transitions in the format:
     * fromStatus, toStatus, action
     */
    static Stream<Arguments> validTransitions() {
        return Stream.of(
            // From RECEIVED
            Arguments.of(BookingStatus.RECEIVED, BookingStatus.ASSIGNED, "assign"),
            Arguments.of(BookingStatus.RECEIVED, BookingStatus.CANCELLED, "cancel"),
            
            // From ASSIGNED
            Arguments.of(BookingStatus.ASSIGNED, BookingStatus.IN_PROGRESS, "start"),
            Arguments.of(BookingStatus.ASSIGNED, BookingStatus.CANCELLED, "cancel"),
            
            // From IN_PROGRESS
            Arguments.of(BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED, "complete"),
            Arguments.of(BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED, "cancel")
            
            // COMPLETED and CANCELLED are terminal states with no valid transitions
        );
    }

    /**
     * Provides all invalid state transitions in the format:
     * currentStatus, action
     */
    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
            // RECEIVED cannot: start, complete
            Arguments.of(BookingStatus.RECEIVED, "start"),
            Arguments.of(BookingStatus.RECEIVED, "complete"),
            
            // ASSIGNED cannot: assign, complete
            Arguments.of(BookingStatus.ASSIGNED, "assign"),
            Arguments.of(BookingStatus.ASSIGNED, "complete"),
            
            // IN_PROGRESS cannot: assign, start
            Arguments.of(BookingStatus.IN_PROGRESS, "assign"),
            Arguments.of(BookingStatus.IN_PROGRESS, "start"),
            
            // COMPLETED cannot: assign, start, complete, cancel (terminal)
            Arguments.of(BookingStatus.COMPLETED, "assign"),
            Arguments.of(BookingStatus.COMPLETED, "start"),
            Arguments.of(BookingStatus.COMPLETED, "complete"),
            Arguments.of(BookingStatus.COMPLETED, "cancel"),
            
            // CANCELLED cannot: assign, start, complete, cancel (terminal)
            Arguments.of(BookingStatus.CANCELLED, "assign"),
            Arguments.of(BookingStatus.CANCELLED, "start"),
            Arguments.of(BookingStatus.CANCELLED, "complete"),
            Arguments.of(BookingStatus.CANCELLED, "cancel")
        );
    }

    /**
     * Provides state names for verification
     */
    static Stream<Arguments> stateNames() {
        return Stream.of(
            Arguments.of(BookingStatus.RECEIVED, "RECEIVED"),
            Arguments.of(BookingStatus.ASSIGNED, "ASSIGNED"),
            Arguments.of(BookingStatus.IN_PROGRESS, "IN_PROGRESS"),
            Arguments.of(BookingStatus.COMPLETED, "COMPLETED"),
            Arguments.of(BookingStatus.CANCELLED, "CANCELLED")
        );
    }

    /**
     * Test that InvalidStateTransitionException properly stores state information
     */
    @org.junit.jupiter.api.Test
    @DisplayName("Should capture state and action in InvalidStateTransitionException")
    void testInvalidStateTransitionExceptionDetails() {
        // Given
        Booking booking = new Booking("Porto", LocalDate.now().plusDays(7), "morning");
        booking.setCurrentStatus(BookingStatus.RECEIVED);

        // When/Then - attempt invalid transition and verify exception details
        assertThatThrownBy(() -> booking.start())
            .isInstanceOf(InvalidStateTransitionException.class)
            .satisfies(e -> {
                InvalidStateTransitionException ex = (InvalidStateTransitionException) e;
                assertThat(ex.getCurrentState()).isEqualTo("RECEIVED");
                assertThat(ex.getAttemptedAction()).isEqualTo("start");
                assertThat(ex.getMessage()).contains("start").contains("RECEIVED");
            });
    }
}

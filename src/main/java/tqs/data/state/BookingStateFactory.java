package tqs.data.state;

import tqs.data.Booking.Booking;

/**
 * Factory for creating appropriate BookingState instances based on current status
 */
public class BookingStateFactory {

    private BookingStateFactory() {
    }

    /**
     * Creates and returns the appropriate state object for the given booking
     * @param booking the booking to create state for
     * @return the appropriate BookingState implementation
     * @throws IllegalArgumentException if status is null or unknown
     */
    public static BookingState getState(Booking booking) {
        if (booking == null || booking.getCurrentStatus() == null) {
            throw new IllegalArgumentException("Booking and status cannot be null");
        }

        return switch (booking.getCurrentStatus()) {
            case RECEIVED -> new ReceivedState(booking);
            case ASSIGNED -> new AssignedState(booking);
            case IN_PROGRESS -> new InProgressState(booking);
            case COMPLETED -> new CompletedState();
            case CANCELLED -> new CancelledState();
        };
    }
}

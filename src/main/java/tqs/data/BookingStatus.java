package tqs.data;

/**
 * Enum representing the different states a booking can go through
 */
public enum BookingStatus {
    RECEIVED,       // Initial state when booking is created
    ASSIGNED,       // Booking has been assigned to a worker
    IN_PROGRESS,    // Collection is in progress
    COMPLETED,      // Collection has been completed
    CANCELLED       // Booking has been cancelled
}

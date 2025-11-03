package tqs.data.state;

import tqs.data.BookingStatus;
import tqs.data.Booking.Booking;

/**
 * State representing a booking assigned to a worker
 * Valid transitions: IN_PROGRESS, CANCELLED
 */
public class AssignedState extends AbstractBookingState {

    public AssignedState(Booking booking) {
        super(booking);
    }

    @Override
    public void start() {
        // Valid transition: ASSIGNED -> IN_PROGRESS
        booking.setCurrentStatus(BookingStatus.IN_PROGRESS);
    }

    @Override
    public void cancel() {
        // Valid transition: ASSIGNED -> CANCELLED
        booking.setCurrentStatus(BookingStatus.CANCELLED);
    }

    @Override
    public String getStateName() {
        return "ASSIGNED";
    }
}

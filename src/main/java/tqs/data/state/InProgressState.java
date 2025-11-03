package tqs.data.state;

import tqs.data.BookingStatus;
import tqs.data.Booking.Booking;

/**
 * State representing a booking that is currently being collected
 * Valid transitions: COMPLETED, CANCELLED
 */
public class InProgressState extends AbstractBookingState {

    public InProgressState(Booking booking) {
        super(booking);
    }

    @Override
    public void complete() {
        // Valid transition: IN_PROGRESS -> COMPLETED
        booking.setCurrentStatus(BookingStatus.COMPLETED);
    }

    @Override
    public void cancel() {
        // Valid transition: IN_PROGRESS -> CANCELLED (e.g., emergency cancellation)
        booking.setCurrentStatus(BookingStatus.CANCELLED);
    }

    @Override
    public String getStateName() {
        return "IN_PROGRESS";
    }
}

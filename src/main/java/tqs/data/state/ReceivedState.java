package tqs.data.state;

import tqs.data.BookingStatus;
import tqs.data.Booking.Booking;

/**
 * State representing a newly received booking
 * Valid transitions: ASSIGNED, CANCELLED
 */
public class ReceivedState extends AbstractBookingState {

    public ReceivedState(Booking booking) {
        super(booking);
    }

    @Override
    public void assign() {
        // Valid transition: RECEIVED -> ASSIGNED
        booking.setCurrentStatus(BookingStatus.ASSIGNED);
    }

    @Override
    public void cancel() {
        // Valid transition: RECEIVED -> CANCELLED
        booking.setCurrentStatus(BookingStatus.CANCELLED);
    }

    @Override
    public String getStateName() {
        return "RECEIVED";
    }
}

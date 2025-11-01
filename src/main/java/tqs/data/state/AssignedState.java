package tqs.data.state;

import tqs.data.BookingStatus;
import tqs.data.Booking.Booking;

/**
 * State representing a booking assigned to a worker
 * Valid transitions: IN_PROGRESS, CANCELLED
 */
public class AssignedState implements BookingState {

    private final Booking booking;

    public AssignedState(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void assign() {
        throw new InvalidStateTransitionException(getStateName(), "assign");
    }

    @Override
    public void start() {
        // Valid transition: ASSIGNED -> IN_PROGRESS
        booking.setCurrentStatus(BookingStatus.IN_PROGRESS);
    }

    @Override
    public void complete() {
        throw new InvalidStateTransitionException(getStateName(), "complete");
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

package tqs.data.state;

import tqs.data.Booking.Booking;

/**
 * Abstract base class for all booking states
 * Provides default implementations that throw InvalidStateTransitionException
 * Subclasses override only the transitions they allow
 */
public abstract class AbstractBookingState implements BookingState {

    protected final Booking booking;

    protected AbstractBookingState(Booking booking) {
        this.booking = booking;
    }

    @Override
    public void assign() {
        throw new InvalidStateTransitionException(getStateName(), "assign");
    }

    @Override
    public void start() {
        throw new InvalidStateTransitionException(getStateName(), "start");
    }

    @Override
    public void complete() {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    @Override
    public void cancel() {
        throw new InvalidStateTransitionException(getStateName(), "cancel");
    }
}

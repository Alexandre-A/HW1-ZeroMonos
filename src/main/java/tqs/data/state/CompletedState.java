package tqs.data.state;

/**
 * State representing a completed booking
 * No transitions allowed (terminal state)
 */
public class CompletedState implements BookingState {

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

    @Override
    public String getStateName() {
        return "COMPLETED";
    }
}

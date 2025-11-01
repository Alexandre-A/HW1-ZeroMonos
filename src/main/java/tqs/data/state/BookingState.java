package tqs.data.state;

/**
 * State interface for the State pattern implementation
 */
public interface BookingState {

    /**
     * Transition to ASSIGNED state
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    void assign();

    /**
     * Transition to IN_PROGRESS state
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    void start();

    /**
     * Transition to COMPLETED state
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    void complete();

    /**
     * Transition to CANCELLED state
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    void cancel();

    /**
     * Get the name of the current state
     * @return state name
     */
    String getStateName();
}

package tqs.data.state;

/**
 * Exception thrown when an invalid state transition is attempted
 */
public class InvalidStateTransitionException extends RuntimeException {

    private final String currentState;
    private final String attemptedAction;

    public InvalidStateTransitionException(String currentState, String attemptedAction) {
        super(String.format("Cannot perform action '%s' in state '%s'", attemptedAction, currentState));
        this.currentState = currentState;
        this.attemptedAction = attemptedAction;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}

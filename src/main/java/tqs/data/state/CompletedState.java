package tqs.data.state;

/**
 * State representing a completed booking
 * No transitions allowed (terminal state)
 */
public class CompletedState extends AbstractBookingState {

    public CompletedState() {
        super(null); // Terminal state doesn't need booking reference
    }

    @Override
    public String getStateName() {
        return "COMPLETED";
    }
}

package tqs.data.state;

/**
 * State representing a cancelled booking
 * No transitions allowed (terminal state)
 */
public class CancelledState extends AbstractBookingState {

    public CancelledState() {
        super(null); // Terminal state doesn't need booking reference
    }

    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}

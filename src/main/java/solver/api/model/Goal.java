package solver.api.model;

/**
 * Created by canaud on 9/29/2015.
 */
public class Goal {

    private GOAL_SENSE sense;

    public Goal (GOAL_SENSE sense) {
        this.sense = sense;
    }

    public GOAL_SENSE getSense() {
        return sense;
    }

    public enum GOAL_SENSE {
        MAXIMIZE,
        MINIMIZE
    }
}

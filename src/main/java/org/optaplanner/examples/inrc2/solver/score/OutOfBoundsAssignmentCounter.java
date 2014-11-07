package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;

public class OutOfBoundsAssignmentCounter extends Counter {

    private static final int countAssignmentsOutOfBounds(final NurseWorkWeek week) {
        final Nurse n = week.getNurse();
        final int totalAssignments = week.countAssignedDays() + n.getNumPreviousAssignments();
        final int minAllowedAssignments = n.getContract().getMinAssignments();
        final int maxAllowedAssignments = n.getContract().getMaxAssignments();
        if (totalAssignments > maxAllowedAssignments) {
            return totalAssignments - maxAllowedAssignments;
        } else if (minAllowedAssignments > totalAssignments) {
            return minAllowedAssignments - totalAssignments;
        } else {
            return 0;
        }
    }

    @Override
    public void afterAdded(final Shift shift, final NurseWorkWeek week) {
        this.count += OutOfBoundsAssignmentCounter.countAssignmentsOutOfBounds(week);
    }

    @Override
    public void afterRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.count += OutOfBoundsAssignmentCounter.countAssignmentsOutOfBounds(week);
    }

    @Override
    public void beforeAdded(final Shift shift, final NurseWorkWeek week) {
        this.count -= OutOfBoundsAssignmentCounter.countAssignmentsOutOfBounds(week);
    }

    @Override
    public void beforeRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.count -= OutOfBoundsAssignmentCounter.countAssignmentsOutOfBounds(week);
    }

    @Override
    int retrieve(final ScoreKeeper tracker) {
        return tracker.getAssignmentsOutOfBoundsCount();
    }

    @Override
    public void store(final ScoreKeeper tracker) {
        tracker.setAssignmentsOutOfBoundsCount(this.count);
    }

}

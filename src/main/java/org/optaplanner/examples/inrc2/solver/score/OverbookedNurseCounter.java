package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;

public class OverbookedNurseCounter extends Counter {

    private boolean wasOverbooked = false;

    @Override
    public void afterAdded(final Shift shift, final NurseWorkWeek week) {
        final boolean isOverbooked = week.isOverbooked(shift.getDay());
        if (isOverbooked && !this.wasOverbooked) {
            this.count++;
        }
    }

    @Override
    public void afterRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        final boolean isOverbooked = week.isOverbooked(shift.getDay());
        if (this.wasOverbooked && !isOverbooked) {
            this.count--;
        }
    }

    @Override
    public void beforeAdded(final Shift shift, final NurseWorkWeek week) {
        this.wasOverbooked = week.isOverbooked(shift.getDay());
    }

    @Override
    public void beforeRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.wasOverbooked = week.isOverbooked(shift.getDay());
    }

    @Override
    int retrieve(final ScoreKeeper tracker) {
        return tracker.getOverbookedNurseCount();
    }

    @Override
    public void store(final ScoreKeeper tracker) {
        tracker.setOverbookedNurseCount(this.count);
    }

}

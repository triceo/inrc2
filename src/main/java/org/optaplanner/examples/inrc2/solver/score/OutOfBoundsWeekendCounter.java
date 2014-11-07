package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;

public class OutOfBoundsWeekendCounter extends Counter {

    private static final int countWeekendsOutsideBounds(final NurseWorkWeek week) {
        final Nurse n = week.getNurse();
        final int previousWorkingWeekends = n.getNumPreviousWorkingWeekends();
        final int maxAllowedWorkingWeekends = n.getContract().getMaxWorkingWeekends();
        final boolean hasWorkingWeekend = week.isOccupied(DayOfWeek.SATURDAY) || week.isOccupied(DayOfWeek.SUNDAY);
        final int totalWorkingWeekendCount = hasWorkingWeekend ? previousWorkingWeekends + 1 : previousWorkingWeekends;
        return Math.max(0, totalWorkingWeekendCount - maxAllowedWorkingWeekends);
    }

    public void after(final Shift shift, final NurseWorkWeek week) {
        if (!shift.getDay().isWeekend()) {
            return;
        }
        this.count += OutOfBoundsWeekendCounter.countWeekendsOutsideBounds(week);
    }

    @Override
    public void afterAdded(final Shift shift, final NurseWorkWeek week) {
        this.after(shift, week);
    }

    @Override
    public void afterRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.after(shift, week);
    }

    public void before(final Shift shift, final NurseWorkWeek week) {
        if (!shift.getDay().isWeekend()) {
            return;
        }
        this.count -= OutOfBoundsWeekendCounter.countWeekendsOutsideBounds(week);
    }

    @Override
    public void beforeAdded(final Shift shift, final NurseWorkWeek week) {
        this.before(shift, week);
    }
    
    @Override
    public void beforeRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.before(shift, week);
    }

    @Override
    int retrieve(final ScoreKeeper tracker) {
        return tracker.getWeekendsOverLimitCount();
    }

    @Override
    public void store(final ScoreKeeper tracker) {
        tracker.setWeekendsOverLimitCount(this.count);
    }

}

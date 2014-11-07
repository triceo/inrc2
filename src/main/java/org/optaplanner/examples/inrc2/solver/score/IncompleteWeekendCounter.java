package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;

public class IncompleteWeekendCounter extends Counter {

    private static final boolean hasCompleteWeekend(final NurseWorkWeek week) {
        if (!week.getNurse().getContract().isCompleteWeekends()) {
            return true;
        }
        final boolean saturday = week.isOccupied(DayOfWeek.SATURDAY);
        final boolean sunday = week.isOccupied(DayOfWeek.SUNDAY);
        if (saturday && sunday) {
            return true;
        } else if (!saturday && !sunday) {
            return true;
        } else {
            return false;
        }
    }

    boolean wasCompleteWeekend = false;

    @Override
    public void afterAdded(final Shift shift, final NurseWorkWeek week) {
        if (!shift.getDay().isWeekend()) {
            return;
        }
        final boolean isCompleteWeekend = IncompleteWeekendCounter.hasCompleteWeekend(week);
        if (isCompleteWeekend && !this.wasCompleteWeekend) {
            this.count -= 1;
        } else if (!isCompleteWeekend && this.wasCompleteWeekend) {
            this.count += 1;
        }
    }

    @Override
    public void afterRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        if (!shift.getDay().isWeekend()) {
            return;
        }
        final boolean isCompleteWeekend = IncompleteWeekendCounter.hasCompleteWeekend(week);
        if (isCompleteWeekend && !this.wasCompleteWeekend) {
            this.count -= 1;
        } else if (!isCompleteWeekend && this.wasCompleteWeekend) {
            this.count += 1;
        }
    }

    @Override
    public void beforeAdded(final Shift shift, final NurseWorkWeek week) {
        if (!shift.getDay().isWeekend()) {
            return;
        }
        this.wasCompleteWeekend = IncompleteWeekendCounter.hasCompleteWeekend(week);
    }

    @Override
    public void beforeRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        if (!shift.getDay().isWeekend()) {
            return;
        }
        this.wasCompleteWeekend = IncompleteWeekendCounter.hasCompleteWeekend(week);
    }

    @Override
    int retrieve(final ScoreKeeper tracker) {
        return tracker.getIncompleteWeekendsCount();
    }

    @Override
    public void store(final ScoreKeeper tracker) {
        tracker.setIncompleteWeekendsCount(this.count);
    }

}

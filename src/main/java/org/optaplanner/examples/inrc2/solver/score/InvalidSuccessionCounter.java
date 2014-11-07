package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;

public class InvalidSuccessionCounter extends Counter {

    public static int countBreaksInSuccession(final NurseWorkWeek week, final DayOfWeek day) {
        if (week.isOverbooked(day)) {
            if (day == DayOfWeek.SUNDAY) {
                return 1; // succession only broken towards saturday
            } else {
                return 2; // succession in both ways is broken
            }
        }
        int total = 0;
        final ShiftType currentShift = week.getShiftType(day);
        final DayOfWeek previousDay = day.getPrevious();
        if (previousDay == null) {
            final ShiftType previousShift = week.getNurse().getPreviousAssignedShiftType();
            if (previousShift != null && !previousShift.isAllowedBefore(currentShift)) {
                total++;
            }
        } else if (week.isOverbooked(previousDay)) {
            total++;
        } else {
            final ShiftType previousShift = week.getShiftType(previousDay);
            if (previousShift != null && !previousShift.isAllowedBefore(currentShift)) {
                total++;
            }
        }
        final DayOfWeek nextDay = day.getNext();
        if (nextDay == null) {
            // nothing; there is nothing after sunday
        } else if (week.isOverbooked(nextDay)) {
            total++;
        } else {
            final ShiftType nextShift = week.getShiftType(nextDay);
            if (nextShift != null && !nextShift.isAllowedAfter(currentShift)) {
                total++;
            }
        }
        return total;
    }

    @Override
    public void afterAdded(final Shift shift, final NurseWorkWeek week) {
        this.count += InvalidSuccessionCounter.countBreaksInSuccession(week, shift.getDay());
    }

    @Override
    public void afterRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.count += InvalidSuccessionCounter.countBreaksInSuccession(week, shift.getDay());
    }

    @Override
    public void beforeAdded(final Shift shift, final NurseWorkWeek week) {
        this.count -= InvalidSuccessionCounter.countBreaksInSuccession(week, shift.getDay());
    }

    @Override
    public void beforeRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.count -= InvalidSuccessionCounter.countBreaksInSuccession(week, shift.getDay());
    }

    @Override
    int retrieve(final ScoreKeeper tracker) {
        return tracker.getInvalidShiftSuccessionCount();
    }

    @Override
    public void store(final ScoreKeeper tracker) {
        tracker.setInvalidShiftSuccessionCount(this.count);
    }

}

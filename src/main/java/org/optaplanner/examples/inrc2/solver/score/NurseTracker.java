package org.optaplanner.examples.inrc2.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;

class NurseTracker {

    // FIXME this is wrong!
    private static final int countAssignmentsOutOfBounds(final NurseWorkWeek week) {
        final Nurse n = week.getNurse();
        final int totalAssignments = week.countAssignedDays() + n.getNumPreviousAssignments();
        final int minAllowedAssignments = n.getContract().getMinAssignments();
        final int maxAllowedAssignments = n.getContract().getMaxAssignments();
        if (totalAssignments > maxAllowedAssignments) {
            return totalAssignments - maxAllowedAssignments;
        } else if (minAllowedAssignments > totalAssignments) {
            /*
             * as the assignments increase towards the minimum allowed, this penalty will have a decreasing
             * tendency; therefore we need to invert it on this interval.
             */
            return totalAssignments - minAllowedAssignments;
        } else {
            return 0;
        }
    }

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

    private static final int countWeekendsOutsideBounds(final NurseWorkWeek week) {
        final Nurse n = week.getNurse();
        final int previousWorkingWeekends = n.getNumPreviousWorkingWeekends();
        final int maxAllowedWorkingWeekends = n.getContract().getMaxWorkingWeekends();
        final boolean hasWorkingWeekend = week.isOccupied(DayOfWeek.SATURDAY) || week.isOccupied(DayOfWeek.SUNDAY);
        final int totalWorkingWeekendCount = hasWorkingWeekend ? previousWorkingWeekends + 1 : previousWorkingWeekends;
        return Math.max(0, totalWorkingWeekendCount - maxAllowedWorkingWeekends);
    }

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

    private int assignmentsOutOfBoundsCount = 0;
    private int consecutiveDayOffViolationsCount = 0;
    private int consecutiveShiftTypeViolationsCount = 0;
    private int consecutiveWorkingDayViolationsCount = 0;
    private int ignoredShiftOffRequestCount = 0;
    private int incompleteWeekendsCount = 0;
    private int invalidShiftSuccessionCount = 0;
    private int overbookedNurseCount = 0;
    private int weekendsOverLimitCount = 0;
    private final Map<Nurse, NurseWorkWeek> workWeeks;

    public NurseTracker(final Roster r) {
        this.workWeeks = new HashMap<Nurse, NurseWorkWeek>(r.getNurses().size());
        /*
         * some nurses may never be assigned to any shift and will therefore never pass through this class. for this
         * case, we're doing the following. otherwise, days off for such nurses would not be penalized.
         */
        this.consecutiveDayOffViolationsCount = r.getMaximumPenaltyForConsecutiveDaysOff();
    }

    public void add(final Shift shift) {
        final Nurse nurse = shift.getNurse();
        if (nurse == null) {
            return;
        }
        final NurseWorkWeek week = this.getWorkWeek(nurse);
        this.assignmentsOutOfBoundsCount -= NurseTracker.countAssignmentsOutOfBounds(week);
        // only for weekends
        final boolean isWeekend = shift.getDay().isWeekend();
        boolean wasCompleteWeekend = false;
        if (isWeekend) {
            wasCompleteWeekend = NurseTracker.hasCompleteWeekend(week);
            this.weekendsOverLimitCount -= NurseTracker.countWeekendsOutsideBounds(week);
        }
        final DayOfWeek day = shift.getDay();
        final boolean wasOverbooked = week.isOverbooked(day);
        this.invalidShiftSuccessionCount -= NurseTracker.countBreaksInSuccession(week, day);
        this.consecutiveDayOffViolationsCount -= SuccessionEvaluator.countConsecutiveDayOffViolations(week);
        this.consecutiveWorkingDayViolationsCount -= SuccessionEvaluator.countConsecutiveWorkingDayViolations(week);
        this.consecutiveShiftTypeViolationsCount -= SuccessionEvaluator.countConsecutiveShiftTypeViolations(week);
        week.addShiftType(day, shift.getShiftType());
        this.consecutiveShiftTypeViolationsCount += SuccessionEvaluator.countConsecutiveShiftTypeViolations(week);
        this.consecutiveWorkingDayViolationsCount += SuccessionEvaluator.countConsecutiveWorkingDayViolations(week);
        this.consecutiveDayOffViolationsCount += SuccessionEvaluator.countConsecutiveDayOffViolations(week);
        this.invalidShiftSuccessionCount += NurseTracker.countBreaksInSuccession(week, day);
        final boolean isOverbooked = week.isOverbooked(day);
        if (isOverbooked && !wasOverbooked) {
            this.overbookedNurseCount++;
        }
        this.assignmentsOutOfBoundsCount += NurseTracker.countAssignmentsOutOfBounds(week);
        if (isWeekend) { // only for weekends
            this.weekendsOverLimitCount += NurseTracker.countWeekendsOutsideBounds(week);
            final boolean isCompleteWeekend = NurseTracker.hasCompleteWeekend(week);
            if (isCompleteWeekend && !wasCompleteWeekend) {
                this.incompleteWeekendsCount -= 1;
            } else if (!isCompleteWeekend && wasCompleteWeekend) {
                this.incompleteWeekendsCount += 1;
            }
        }
        if (nurse.shiftOffRequested(day, shift.getShiftType())) {
            this.ignoredShiftOffRequestCount++;
        }
    }

    public int countAssignmentsOutOfBounds() {
        return this.assignmentsOutOfBoundsCount;
    }

    public int countConsecutiveDayOffViolations() {
        return this.consecutiveDayOffViolationsCount;
    }

    public int countConsecutiveShiftTypeViolations() {
        return this.consecutiveShiftTypeViolationsCount;
    }

    public int countConsecutiveWorkingDayViolations() {
        return this.consecutiveWorkingDayViolationsCount;
    }

    public int countIgnoredShiftPreferences() {
        return this.ignoredShiftOffRequestCount;
    }

    public int countIncompleteWeekends() {
        return this.incompleteWeekendsCount;
    }

    public int countInvalidShiftSuccessions() {
        return this.invalidShiftSuccessionCount;
    }

    public int countOverbookedNurses() {
        return this.overbookedNurseCount;
    }

    public int countWeekendsOutOfBounds() {
        return this.weekendsOverLimitCount;
    }

    private NurseWorkWeek getWorkWeek(final Nurse n) {
        if (n == null) { // defensive programming
            throw new IllegalArgumentException("Cannot operate on null nurses.");
        }
        NurseWorkWeek w = this.workWeeks.get(n);
        if (w == null) {
            w = new NurseWorkWeek(n);
            this.workWeeks.put(n, w);
        }
        return w;
    }

    public void remove(final Shift shift) {
        this.remove(shift, shift.getNurse());
    }

    public void remove(final Shift shift, final Nurse nurse) {
        if (nurse == null) {
            return;
        }
        final NurseWorkWeek week = this.getWorkWeek(nurse);
        this.assignmentsOutOfBoundsCount -= NurseTracker.countAssignmentsOutOfBounds(week);
        // only for weekends
        final boolean isWeekend = shift.getDay().isWeekend();
        boolean wasCompleteWeekend = false;
        if (isWeekend) {
            wasCompleteWeekend = NurseTracker.hasCompleteWeekend(week);
            this.weekendsOverLimitCount -= NurseTracker.countWeekendsOutsideBounds(week);
        }
        final DayOfWeek day = shift.getDay();
        final boolean wasOverbooked = week.isOverbooked(day);
        this.invalidShiftSuccessionCount -= NurseTracker.countBreaksInSuccession(week, day);
        this.consecutiveDayOffViolationsCount -= SuccessionEvaluator.countConsecutiveDayOffViolations(week);
        this.consecutiveWorkingDayViolationsCount -= SuccessionEvaluator.countConsecutiveWorkingDayViolations(week);
        this.consecutiveShiftTypeViolationsCount -= SuccessionEvaluator.countConsecutiveShiftTypeViolations(week);
        week.removeShiftType(day, shift.getShiftType());
        this.consecutiveShiftTypeViolationsCount += SuccessionEvaluator.countConsecutiveShiftTypeViolations(week);
        this.consecutiveWorkingDayViolationsCount += SuccessionEvaluator.countConsecutiveWorkingDayViolations(week);
        this.consecutiveDayOffViolationsCount += SuccessionEvaluator.countConsecutiveDayOffViolations(week);
        this.invalidShiftSuccessionCount += NurseTracker.countBreaksInSuccession(week, day);
        final boolean isOverbooked = week.isOverbooked(day);
        if (wasOverbooked && !isOverbooked) {
            this.overbookedNurseCount--;
        }
        this.assignmentsOutOfBoundsCount += NurseTracker.countAssignmentsOutOfBounds(week);
        if (isWeekend) { // only for weekends
            this.weekendsOverLimitCount += NurseTracker.countWeekendsOutsideBounds(week);
            final boolean isCompleteWeekend = NurseTracker.hasCompleteWeekend(week);
            if (isCompleteWeekend && !wasCompleteWeekend) {
                this.incompleteWeekendsCount -= 1;
            } else if (!isCompleteWeekend && wasCompleteWeekend) {
                this.incompleteWeekendsCount += 1;
            }
        }
        if (nurse.shiftOffRequested(day, shift.getShiftType())) {
            this.ignoredShiftOffRequestCount--;
        }
    }

}

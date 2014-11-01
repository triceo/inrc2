package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.Contract;
import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;

final class SuccessionTracker {

    private static int calculateSuccessionPenalty(final ShiftType previous, final ShiftType current, final ShiftType next) {
        if (current == null) {
            return 0;
        }
        int penalty = 0;
        if (!current.isAllowedAfter(previous)) {
            penalty++;
        }
        if (!current.isAllowedBefore(next)) {
            penalty++;
        }
        return penalty;
    }

    protected static final int getDayIndex(final DayOfWeek day) {
        return day.getNumber() + 1; // 0 is the last shift from previous week
    }

    private final int maxAllowedAssignments;
    private final int maxAllowedConsecutiveWorkingDays;
    private final int maxAllowedWorkingWeekends;
    private final int minAllowedAssignments;
    private final int minAllowedConsecutiveWorkingDays;

    private final int previousConsecutiveAssignments;
    private final int previousConsecutiveWorkingDays;
    private final int previousWorkingWeekends;
    private final boolean requiresCompleteWeekend;
    private int successionPenalty = 0;
    // +2 as 0 is previous and 8 prevents AIOOBE
    private final ShiftType[] successions = new ShiftType[DayOfWeek.values().length + 2];
    private int totalAssignments = 0;

    public SuccessionTracker(final Nurse n) {
        this.successions[0] = n.getPreviousAssignedShiftType();
        final Contract c = n.getContract();
        this.requiresCompleteWeekend = c.isCompleteWeekends();
        this.minAllowedAssignments = c.getMinAssignments();
        this.maxAllowedAssignments = c.getMaxAssignments();
        this.minAllowedConsecutiveWorkingDays = c.getMinConsecutiveDaysOn();
        this.maxAllowedConsecutiveWorkingDays = c.getMaxConsecutiveDaysOn();
        this.maxAllowedWorkingWeekends = c.getMaxWorkingWeekends();
        this.totalAssignments = n.getNumPreviousAssignments();
        this.previousWorkingWeekends = n.getNumPreviousWorkingWeekends();
        this.previousConsecutiveWorkingDays = n.getNumPreviousConsecutiveDaysOn();
        this.previousConsecutiveAssignments = n.getNumPreviousConsecutiveAssignmentsOfSameShiftType();
    }

    public void add(final Shift shift) {
        final ShiftType currentShiftType = shift.getShiftType();
        final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
        final ShiftType previousShiftType = this.successions[dayIndex - 1];
        final ShiftType nextShiftType = this.successions[dayIndex + 1];
        this.successions[dayIndex] = currentShiftType;
        this.successionPenalty += SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
        if (currentShiftType != null) {
            this.totalAssignments += 1;
        }
    }

    public int countAssignmentsOutsideBounds() {
        if (this.totalAssignments > this.maxAllowedAssignments) {
            return this.totalAssignments - this.maxAllowedAssignments;
        } else if (this.minAllowedAssignments > this.totalAssignments) {
            /*
             * as the assignments increase towards the minimum allowed, this penalty will have a decreasing
             * tendency; therefore we need to invert it on this interval.
             */
            return this.totalAssignments - this.minAllowedAssignments;
        } else {
            return 0;
        }
    }

    public int countConsecutiveShiftTypeViolations() {
        return SuccessionEvaluator.countConsecutiveShiftTypeViolations(this.successions, this.previousConsecutiveAssignments, this.minAllowedConsecutiveWorkingDays, this.maxAllowedConsecutiveWorkingDays);
    }

    public int countConsecutiveWorkingDayViolations() {
        return SuccessionEvaluator.countConsecutiveWorkingDayViolations(this.successions, this.previousConsecutiveWorkingDays, this.minAllowedConsecutiveWorkingDays, this.maxAllowedConsecutiveWorkingDays);
    }

    public int countWeekendsOutsideBounds() {
        final int totalWorkingWeekendCount = this.hasWorkingWeekend() ? this.previousWorkingWeekends + 1 : this.previousWorkingWeekends;
        return Math.min(0, totalWorkingWeekendCount - this.maxAllowedWorkingWeekends);
    }

    public int getSuccessionPenalty() {
        return this.successionPenalty;
    }

    public boolean hasCompleteWeekend() {
        if (!this.requiresCompleteWeekend) {
            return true;
        }
        final ShiftType saturday = this.successions[SuccessionTracker.getDayIndex(DayOfWeek.SATURDAY)];
        final ShiftType sunday = this.successions[SuccessionTracker.getDayIndex(DayOfWeek.SUNDAY)];
        if (saturday == null && sunday == null) {
            return true;
        } else if (saturday != null && sunday != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasWorkingWeekend() {
        final ShiftType saturday = this.successions[SuccessionTracker.getDayIndex(DayOfWeek.SATURDAY)];
        final ShiftType sunday = this.successions[SuccessionTracker.getDayIndex(DayOfWeek.SUNDAY)];
        return (saturday != null || sunday != null);
    }

    public void changeShiftType(final Shift shift, final ShiftType formerShiftType) {
        final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
        final ShiftType currentShiftType = shift.getShiftType();
        final ShiftType previousShiftType = this.successions[dayIndex - 1];
        final ShiftType nextShiftType = this.successions[dayIndex + 1];
        this.successions[dayIndex] = currentShiftType;
        this.successionPenalty += SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType) - SuccessionTracker.calculateSuccessionPenalty(previousShiftType, formerShiftType, nextShiftType);
        if (currentShiftType == null && formerShiftType != null) {
            this.totalAssignments -= 1;
        } else if (currentShiftType != null && formerShiftType == null) {
            this.totalAssignments += 1;
        }
    }

    public void remove(final Shift shift) {
        final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
        final ShiftType currentShiftType = this.successions[dayIndex];
        final ShiftType previousShiftType = this.successions[dayIndex - 1];
        final ShiftType nextShiftType = this.successions[dayIndex + 1];
        this.successions[dayIndex] = null;
        this.successionPenalty -= SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
        if (currentShiftType != null) {
            this.totalAssignments -= 1;
        }
    }

}
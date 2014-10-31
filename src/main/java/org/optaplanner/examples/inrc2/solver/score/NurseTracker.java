package org.optaplanner.examples.inrc2.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.Contract;
import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;

public class NurseTracker {

    private final static class SuccessionTracker {

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

        private static final int getDayIndex(final DayOfWeek day) {
            return day.getNumber() + 1; // 0 is the last shift from previous week
        }

        private int incompleteWeekendPenalty = 0;

        private final int maxAllowedAssignments;
        private final int maxAllowedWorkingWeekends;
        private final int minAllowedAssignments;

        private final boolean requiresCompleteWeekend;
        private int successionPenalty = 0;
        // +2 as 0 is previous and 8 prevents AIOUB
        private final ShiftType[] successions = new ShiftType[DayOfWeek.values().length + 2];
        private int totalAssignments = 0;
        private int totalWorkingWeekends = 0;

        public SuccessionTracker(final Nurse n) {
            this.successions[0] = n.getPreviousAssignedShiftType();
            final Contract c = n.getContract();
            this.requiresCompleteWeekend = c.isCompleteWeekends();
            this.minAllowedAssignments = c.getMinAssignments();
            this.maxAllowedAssignments = c.getMaxAssignments();
            this.maxAllowedWorkingWeekends = c.getMaxWorkingWeekends();
            this.totalAssignments = n.getNumPreviousAssignments();
            this.totalWorkingWeekends = n.getNumPreviousWorkingWeekends();
        }

        public void add(final Shift shift) {
            final boolean previouslyWorkingWeekend = this.hasWorkingWeekend();
            final boolean previouslyCompleteWeekend = this.hasCompletedWeekend();
            final ShiftType currentShiftType = shift.getShiftType();
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = currentShiftType;
            this.successionPenalty += SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
            if (currentShiftType != null) {
                this.totalAssignments += 1;
            }
            final boolean nowCompleteWeekend = this.hasCompletedWeekend();
            if (previouslyCompleteWeekend && !nowCompleteWeekend) {
                this.incompleteWeekendPenalty += Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            } else if (!previouslyCompleteWeekend && nowCompleteWeekend) {
                this.incompleteWeekendPenalty -= Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            }
            final boolean nowWorkingWeekend = this.hasWorkingWeekend();
            if (previouslyWorkingWeekend && !nowWorkingWeekend) {
                this.totalWorkingWeekends -= 1;
            } else if (!previouslyWorkingWeekend && nowWorkingWeekend) {
                this.totalWorkingWeekends += 1;
            }
        }

        public int getIncompleteWeekendPenalty() {
            return this.incompleteWeekendPenalty;
        }

        public int getSuccessionPenalty() {
            return this.successionPenalty;
        }

        public int getTotalAssignmentsPenalty() {
            if (this.totalAssignments > this.maxAllowedAssignments) {
                return (this.totalAssignments - this.maxAllowedAssignments) * Inrc2IncrementalScoreCalculator.TOTAL_ASSIGNMENTS_WEIGHT;
            } else if (this.minAllowedAssignments > this.totalAssignments) {
                /*
                 *  as the assignments increase towards the minimum allowed, this penalty will have a decreasing 
                 *  tendency; therefore we need to invert it on this inteval.
                 */
                return -(this.minAllowedAssignments - this.totalAssignments) * Inrc2IncrementalScoreCalculator.TOTAL_ASSIGNMENTS_WEIGHT;
            } else {
                return 0;
            }
        }

        public int getTotalWorkingWeekendsPenalty() {
            return Math.max(0, this.totalWorkingWeekends - this.maxAllowedWorkingWeekends) * Inrc2IncrementalScoreCalculator.WORKING_WEEKENDS_WEIGHT;
        }

        private boolean hasCompletedWeekend() {
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
            final boolean previouslyWorkingWeekend = this.hasWorkingWeekend();
            final boolean previouslyCompleteWeekend = this.hasCompletedWeekend();
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
            final boolean nowCompleteWeekend = this.hasCompletedWeekend();
            if (previouslyCompleteWeekend && !nowCompleteWeekend) {
                this.incompleteWeekendPenalty += Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            } else if (!previouslyCompleteWeekend && nowCompleteWeekend) {
                this.incompleteWeekendPenalty -= Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            }
            final boolean nowWorkingWeekend = this.hasWorkingWeekend();
            if (previouslyWorkingWeekend && !nowWorkingWeekend) {
                this.totalWorkingWeekends -= 1;
            } else if (!previouslyWorkingWeekend && nowWorkingWeekend) {
                this.totalWorkingWeekends += 1;
            }
        }

        public void remove(final Shift shift) {
            final boolean previouslyWorkingWeekend = this.hasWorkingWeekend();
            final boolean previouslyCompleteWeekend = this.hasCompletedWeekend();
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType currentShiftType = this.successions[dayIndex];
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = null;
            this.successionPenalty -= SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
            if (currentShiftType != null) {
                this.totalAssignments -= 1;
            }
            final boolean nowCompleteWeekend = this.hasCompletedWeekend();
            if (previouslyCompleteWeekend && !nowCompleteWeekend) {
                this.incompleteWeekendPenalty += Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            } else if (!previouslyCompleteWeekend && nowCompleteWeekend) {
                this.incompleteWeekendPenalty -= Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            }
            final boolean nowWorkingWeekend = this.hasWorkingWeekend();
            if (previouslyWorkingWeekend && !nowWorkingWeekend) {
                this.totalWorkingWeekends -= 1;
            } else if (!previouslyWorkingWeekend && nowWorkingWeekend) {
                this.totalWorkingWeekends += 1;
            }
        }

    }

    private int incompleteWeekendsPenalty = 0;
    private final Map<Nurse, SuccessionTracker> nurses = new HashMap<Nurse, SuccessionTracker>();

    private int preferencePenalty = 0;
    private int successionPenalty = 0;
    private int totalAssignmentsPenalty = 0;
    private int totalWorkingWeekendsPenalty = 0;

    public NurseTracker(final Roster r) {
        // by default, all nurses who require complete weekends have incomplete weekends
        this.incompleteWeekendsPenalty = r.getNursesRequiringCompleteWeekends().size() * Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
    }

    public void add(final Shift shift) {
        if (shift.getSkill() == null) { // nurse is not actually assigned
            return;
        }
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsPenalty -= t.getIncompleteWeekendPenalty();
        this.totalWorkingWeekendsPenalty -= t.getTotalWorkingWeekendsPenalty();
        this.totalAssignmentsPenalty -= t.getTotalAssignmentsPenalty();
        t.add(shift);
        this.totalAssignmentsPenalty += t.getTotalAssignmentsPenalty();
        this.totalWorkingWeekendsPenalty += t.getTotalWorkingWeekendsPenalty();
        this.incompleteWeekendsPenalty += t.getIncompleteWeekendPenalty();
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.preferencePenalty += Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT;
        }
    }

    private SuccessionTracker forNurse(final Nurse n) {
        SuccessionTracker pnt = this.nurses.get(n);
        if (pnt == null) {
            pnt = new SuccessionTracker(n);
            this.nurses.put(n, pnt);
        }
        return pnt;
    }

    public int getIncompleteWeekendsPenalty() {
        return this.incompleteWeekendsPenalty;
    }

    public int getPreferencePenalty() {
        return this.preferencePenalty;
    }

    public int getSuccessionPenalty() {
        return this.successionPenalty;
    }

    public int getTotalAssignmentsPenalty() {
        return this.totalAssignmentsPenalty;
    }

    public int getTotalWorkingWeekendsPenalty() {
        return this.totalWorkingWeekendsPenalty;
    }

    public void changeShiftType(final Shift shift, final ShiftType previous) {
        if (shift.getSkill() == null) { // nurse is not actually assigned
            return;
        }
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsPenalty -= t.getIncompleteWeekendPenalty();
        this.totalWorkingWeekendsPenalty -= t.getTotalWorkingWeekendsPenalty();
        this.totalAssignmentsPenalty -= t.getTotalAssignmentsPenalty();
        t.changeShiftType(shift, previous);
        this.totalAssignmentsPenalty += t.getTotalAssignmentsPenalty();
        this.totalWorkingWeekendsPenalty += t.getTotalWorkingWeekendsPenalty();
        this.incompleteWeekendsPenalty += t.getIncompleteWeekendPenalty();
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (previous != null && !shift.isDesired(previous)) {
            this.preferencePenalty -= Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT;
        }
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.preferencePenalty += Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT;
        }
    }

    /**
     * Remove a nurse that is assigned to a shift.
     *
     */
    public void remove(final Shift shift) {
        this.remove(shift, false);
    }

    /**
     * Remove a nurse.
     *
     * @param shift
     * @param force
     *            If true, nurse will be removed even when it is not assigned to a shift. This is necessary, as when
     *            unsetting the skill, it will have already been null at the time of reaching this method.
     */
    public void remove(final Shift shift, final boolean force) {
        if (!force && shift.getSkill() == null) { // nurse is not actually assigned
            return;
        }
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsPenalty -= t.getIncompleteWeekendPenalty();
        this.totalWorkingWeekendsPenalty -= t.getTotalWorkingWeekendsPenalty();
        this.totalAssignmentsPenalty -= t.getTotalAssignmentsPenalty();
        t.remove(shift);
        this.totalAssignmentsPenalty += t.getTotalAssignmentsPenalty();
        this.totalWorkingWeekendsPenalty += t.getTotalWorkingWeekendsPenalty();
        this.incompleteWeekendsPenalty += t.getIncompleteWeekendPenalty();
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.preferencePenalty -= Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT;
        }
    }

}

package org.optaplanner.examples.inrc2.solver.score;

import java.util.HashMap;
import java.util.Map;

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

        private final boolean requiresCompleteWeekend;
        private int successionPenalty = 0;

        // +2 as 0 is previous and 8 prevents AIOUB
        private final ShiftType[] successions = new ShiftType[DayOfWeek.values().length + 2];

        public SuccessionTracker(final Nurse n) {
            this.successions[0] = n.getPreviousAssignedShiftType();
            this.requiresCompleteWeekend = n.getContract().isCompleteWeekends();
        }

        public void add(final Shift shift) {
            final boolean previouslyCompleteWeekend = this.hasCompletedWeekend();
            final ShiftType currentShiftType = shift.getShiftType();
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = currentShiftType;
            this.successionPenalty += SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
            final boolean nowCompleteWeekend = this.hasCompletedWeekend();
            if (previouslyCompleteWeekend && !nowCompleteWeekend) {
                this.incompleteWeekendPenalty += Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            } else if (!previouslyCompleteWeekend && nowCompleteWeekend) {
                this.incompleteWeekendPenalty -= Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            }
        }

        public int getIncompleteWeekendPenalty() {
            return this.incompleteWeekendPenalty;
        }

        public int getSuccessionPenalty() {
            return this.successionPenalty;
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

        public void changeShiftType(final Shift shift, final ShiftType formerShiftType) {
            final boolean previouslyCompleteWeekend = this.hasCompletedWeekend();
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType currentShiftType = shift.getShiftType();
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = currentShiftType;
            this.successionPenalty += SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType) - SuccessionTracker.calculateSuccessionPenalty(previousShiftType, formerShiftType, nextShiftType);
            final boolean nowCompleteWeekend = this.hasCompletedWeekend();
            if (previouslyCompleteWeekend && !nowCompleteWeekend) {
                this.incompleteWeekendPenalty += Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            } else if (!previouslyCompleteWeekend && nowCompleteWeekend) {
                this.incompleteWeekendPenalty -= Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            }
        }

        public void remove(final Shift shift) {
            final boolean previouslyCompleteWeekend = this.hasCompletedWeekend();
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType currentShiftType = this.successions[dayIndex];
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = null;
            this.successionPenalty -= SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
            final boolean nowCompleteWeekend = this.hasCompletedWeekend();
            if (previouslyCompleteWeekend && !nowCompleteWeekend) {
                this.incompleteWeekendPenalty += Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            } else if (!previouslyCompleteWeekend && nowCompleteWeekend) {
                this.incompleteWeekendPenalty -= Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
            }
        }

    }

    private int incompleteWeekendsPenalty = 0;

    private final Map<Nurse, SuccessionTracker> nurses = new HashMap<Nurse, SuccessionTracker>();
    private int preferencePenalty = 0;
    private int successionPenalty = 0;

    public NurseTracker(final Roster r) {
        // by default, all nurses who require complete weekends have incomplete weekends
        this.incompleteWeekendsPenalty = r.getNursesRequiringCompleteWeekends().size() * Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT;
    }

    public void add(final Shift shift) {
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsPenalty -= t.getIncompleteWeekendPenalty();
        t.add(shift);
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

    public void changeShiftType(final Shift shift, final ShiftType previous) {
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsPenalty -= t.getIncompleteWeekendPenalty();
        t.changeShiftType(shift, previous);
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

    public void remove(final Shift shift) {
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsPenalty -= t.getIncompleteWeekendPenalty();
        t.remove(shift);
        this.incompleteWeekendsPenalty += t.getIncompleteWeekendPenalty();
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.preferencePenalty -= Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT;
        }
    }

}

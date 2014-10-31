package org.optaplanner.examples.inrc2.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
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

        private int successionPenalty = 0;

        // +2 as 0 is previous and 8 prevents AIOUB
        private final ShiftType[] successions = new ShiftType[DayOfWeek.values().length + 2];

        public SuccessionTracker(final Nurse n) {
            this.successions[0] = n.getPreviousAssignedShiftType();
        }

        public void add(final Shift shift) {
            final ShiftType currentShiftType = shift.getShiftType();
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = currentShiftType;
            this.successionPenalty += SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
        }

        public int getSuccessionPenalty() {
            return this.successionPenalty;
        }

        public void changeShiftType(final Shift shift, final ShiftType formerShiftType) {
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType currentShiftType = shift.getShiftType();
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = currentShiftType;
            this.successionPenalty += SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType) - SuccessionTracker.calculateSuccessionPenalty(previousShiftType, formerShiftType, nextShiftType);
        }

        public void remove(final Shift shift) {
            final int dayIndex = SuccessionTracker.getDayIndex(shift.getDay());
            final ShiftType currentShiftType = this.successions[dayIndex];
            final ShiftType previousShiftType = this.successions[dayIndex - 1];
            final ShiftType nextShiftType = this.successions[dayIndex + 1];
            this.successions[dayIndex] = null;
            this.successionPenalty -= SuccessionTracker.calculateSuccessionPenalty(previousShiftType, currentShiftType, nextShiftType);
        }

    }

    private final Map<Nurse, SuccessionTracker> nurses = new HashMap<Nurse, SuccessionTracker>();

    private int preferencePenalty = 0;
    private int successionPenalty = 0;

    public NurseTracker(final int nurseCount) {

    }

    public void add(final Shift shift) {
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        t.add(shift);
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

    public int getPreferencePenalty() {
        return this.preferencePenalty;
    }

    public int getSuccessionPenalty() {
        return this.successionPenalty;
    }

    public void changeShiftType(final Shift shift, final ShiftType previous) {
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        t.changeShiftType(shift, previous);
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
        t.remove(shift);
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.preferencePenalty -= Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT;
        }
    }

}

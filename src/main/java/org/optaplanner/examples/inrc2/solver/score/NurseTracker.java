package org.optaplanner.examples.inrc2.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;

public class NurseTracker {

    private int ignoredShiftPreferenceCount = 0;
    private int incompleteWeekendsCount = 0;
    private final Map<Nurse, SuccessionTracker> nurses = new HashMap<Nurse, SuccessionTracker>();
    private int totalAssignmentsOutOfBounds = 0;
    private int totalConsecutiveDayOffViolations = 0;
    private int totalConsecutiveShiftTypeViolations = 0;
    private int totalConsecutiveWorkingDayViolations = 0;
    private int totalInvalidShiftSuccessions = 0;
    private int totalWeekendsOverLimit = 0;

    public NurseTracker(final Roster r) {
        this.totalConsecutiveDayOffViolations = r.getConsecutiveDayOffViolationsForUnusedNurses();
    }

    public void add(final Shift shift) {
        if (shift.getSkill() == null) { // nurse is not actually assigned
            return;
        }
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        // only for weekends
        final boolean isWeekend = shift.getDay().isWeekend();
        boolean wasCompleteWeekend = false;
        if (isWeekend) {
            wasCompleteWeekend = t.hasCompleteWeekend();
            this.totalWeekendsOverLimit -= t.countWeekendsOutsideBounds();
        }
        // and the rest
        this.totalInvalidShiftSuccessions -= t.countBrokenSuccessions();
        this.totalAssignmentsOutOfBounds -= t.countAssignmentsOutsideBounds();
        this.totalConsecutiveWorkingDayViolations -= t.countConsecutiveWorkingDayViolations();
        this.totalConsecutiveShiftTypeViolations -= t.countConsecutiveShiftTypeViolations();
        this.totalConsecutiveDayOffViolations -= t.countConsecutiveDayOffViolations();
        t.add(shift); // actualy preform the operation
        this.totalConsecutiveDayOffViolations += t.countConsecutiveDayOffViolations();
        this.totalConsecutiveShiftTypeViolations += t.countConsecutiveShiftTypeViolations();
        this.totalConsecutiveWorkingDayViolations += t.countConsecutiveWorkingDayViolations();
        this.totalAssignmentsOutOfBounds += t.countAssignmentsOutsideBounds();
        if (isWeekend) { // only for weekends
            this.totalWeekendsOverLimit += t.countWeekendsOutsideBounds();
            final boolean isCompleteWeekend = t.hasCompleteWeekend();
            if (isCompleteWeekend && !wasCompleteWeekend) {
                this.incompleteWeekendsCount -= 1;
            } else if (!isCompleteWeekend && wasCompleteWeekend) {
                this.incompleteWeekendsCount += 1;
            }
        }
        this.totalInvalidShiftSuccessions += t.countBrokenSuccessions();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.ignoredShiftPreferenceCount += 1;
        }
    }

    public int countAssignmentsOutOfBounds() {
        return this.totalAssignmentsOutOfBounds;
    }

    public int countConsecutiveDayOffViolations() {
        return this.totalConsecutiveDayOffViolations;
    }

    public int countConsecutiveShiftTypeViolations() {
        return this.totalConsecutiveShiftTypeViolations;
    }

    public int countConsecutiveWorkingDayViolations() {
        return this.totalConsecutiveWorkingDayViolations;
    }

    public int countIgnoredShiftPreferences() {
        return this.ignoredShiftPreferenceCount;
    }

    public int countIncompleteWeekends() {
        return this.incompleteWeekendsCount;
    }

    public int countInvalidShiftSuccessions() {
        return this.totalInvalidShiftSuccessions;
    }

    public int countWeekendsOutOfBounds() {
        return this.totalWeekendsOverLimit;
    }

    private SuccessionTracker forNurse(final Nurse n) {
        SuccessionTracker pnt = this.nurses.get(n);
        if (pnt == null) {
            pnt = new SuccessionTracker(n);
            this.nurses.put(n, pnt);
        }
        return pnt;
    }

    public void changeShiftType(final Shift shift, final ShiftType previous) {
        if (shift.getSkill() == null) { // nurse is not actually assigned
            return;
        }
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        // only for weekends
        final boolean isWeekend = shift.getDay().isWeekend();
        boolean wasCompleteWeekend = false;
        if (isWeekend) {
            wasCompleteWeekend = t.hasCompleteWeekend();
            this.totalWeekendsOverLimit -= t.countWeekendsOutsideBounds();
        }
        // and the rest
        this.totalInvalidShiftSuccessions -= t.countBrokenSuccessions();
        this.totalAssignmentsOutOfBounds -= t.countAssignmentsOutsideBounds();
        this.totalConsecutiveWorkingDayViolations -= t.countConsecutiveWorkingDayViolations();
        this.totalConsecutiveShiftTypeViolations -= t.countConsecutiveShiftTypeViolations();
        this.totalConsecutiveDayOffViolations -= t.countConsecutiveDayOffViolations();
        t.changeShiftType(shift, previous);
        this.totalConsecutiveDayOffViolations += t.countConsecutiveDayOffViolations();
        this.totalConsecutiveShiftTypeViolations += t.countConsecutiveShiftTypeViolations();
        this.totalConsecutiveWorkingDayViolations += t.countConsecutiveWorkingDayViolations();
        this.totalAssignmentsOutOfBounds += t.countAssignmentsOutsideBounds();
        this.totalInvalidShiftSuccessions += t.countBrokenSuccessions();
        // determine preference penalty
        if (previous != null && !shift.isDesired(previous)) {
            this.ignoredShiftPreferenceCount -= 1;
        }
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.ignoredShiftPreferenceCount += 1;
        }
        if (isWeekend) { // only for weekends
            this.totalWeekendsOverLimit += t.countWeekendsOutsideBounds();
            final boolean isCompleteWeekend = t.hasCompleteWeekend();
            if (isCompleteWeekend && !wasCompleteWeekend) {
                this.incompleteWeekendsCount -= 1;
            } else if (!isCompleteWeekend && wasCompleteWeekend) {
                this.incompleteWeekendsCount += 1;
            }
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
        // only for weekends
        final boolean isWeekend = shift.getDay().isWeekend();
        boolean wasCompleteWeekend = false;
        if (isWeekend) {
            wasCompleteWeekend = t.hasCompleteWeekend();
            this.totalWeekendsOverLimit -= t.countWeekendsOutsideBounds();
        }
        // and the rest
        this.totalInvalidShiftSuccessions -= t.countBrokenSuccessions();
        this.totalAssignmentsOutOfBounds -= t.countAssignmentsOutsideBounds();
        this.totalConsecutiveWorkingDayViolations -= t.countConsecutiveWorkingDayViolations();
        this.totalConsecutiveShiftTypeViolations -= t.countConsecutiveShiftTypeViolations();
        this.totalConsecutiveDayOffViolations -= t.countConsecutiveDayOffViolations();
        t.remove(shift);
        this.totalConsecutiveDayOffViolations += t.countConsecutiveDayOffViolations();
        this.totalConsecutiveShiftTypeViolations += t.countConsecutiveShiftTypeViolations();
        this.totalConsecutiveWorkingDayViolations += t.countConsecutiveWorkingDayViolations();
        this.totalAssignmentsOutOfBounds += t.countAssignmentsOutsideBounds();
        this.totalInvalidShiftSuccessions += t.countBrokenSuccessions();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.ignoredShiftPreferenceCount -= 1;
        }
        if (isWeekend) { // only for weekends
            this.totalWeekendsOverLimit += t.countWeekendsOutsideBounds();
            final boolean isCompleteWeekend = t.hasCompleteWeekend();
            if (isCompleteWeekend && !wasCompleteWeekend) {
                this.incompleteWeekendsCount -= 1;
            } else if (!isCompleteWeekend && wasCompleteWeekend) {
                this.incompleteWeekendsCount += 1;
            }
        }
    }

}

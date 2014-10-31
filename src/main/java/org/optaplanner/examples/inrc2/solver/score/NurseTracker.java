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
    private int successionPenalty = 0;

    private int totalAssignmentsOutOfBounds = 0;
    private int totalWeekedsOverLimit = 0;

    public NurseTracker(final Roster r) {
        // by default, all nurses who require complete weekends have incomplete weekends
        this.incompleteWeekendsCount = r.getNursesRequiringCompleteWeekends().size();
    }

    public void add(final Shift shift) {
        if (shift.getSkill() == null) { // nurse is not actually assigned
            return;
        }
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsCount -= t.getIncompleteWeekendPenalty();
        this.totalAssignmentsOutOfBounds -= t.countAssignmentsOutsideBounds();
        this.totalWeekedsOverLimit -= t.countWeekendsOutsideBounds();
        t.add(shift);
        this.totalWeekedsOverLimit += t.countWeekendsOutsideBounds();
        this.totalAssignmentsOutOfBounds += t.countAssignmentsOutsideBounds();
        this.incompleteWeekendsCount += t.getIncompleteWeekendPenalty();
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.ignoredShiftPreferenceCount += 1;
        }
    }

    public int countIgnoredShiftPreferences() {
        return this.ignoredShiftPreferenceCount;
    }

    public int countIncompleteWeekends() {
        return this.incompleteWeekendsCount;
    }

    public int countTotalAssignmentsOutOfBounds() {
        return this.totalAssignmentsOutOfBounds;
    }

    public int countTotalWeekdsOutOfBounds() {
        return this.totalWeekedsOverLimit;
    }

    private SuccessionTracker forNurse(final Nurse n) {
        SuccessionTracker pnt = this.nurses.get(n);
        if (pnt == null) {
            pnt = new SuccessionTracker(n);
            this.nurses.put(n, pnt);
        }
        return pnt;
    }

    public int getSuccessionPenalty() {
        return this.successionPenalty;
    }

    public void changeShiftType(final Shift shift, final ShiftType previous) {
        if (shift.getSkill() == null) { // nurse is not actually assigned
            return;
        }
        final SuccessionTracker t = this.forNurse(shift.getNurse());
        this.successionPenalty -= t.getSuccessionPenalty();
        this.incompleteWeekendsCount -= t.getIncompleteWeekendPenalty();
        this.totalAssignmentsOutOfBounds -= t.countAssignmentsOutsideBounds();
        this.totalWeekedsOverLimit -= t.countWeekendsOutsideBounds();
        t.changeShiftType(shift, previous);
        this.totalWeekedsOverLimit += t.countWeekendsOutsideBounds();
        this.totalAssignmentsOutOfBounds += t.countAssignmentsOutsideBounds();
        this.incompleteWeekendsCount += t.getIncompleteWeekendPenalty();
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (previous != null && !shift.isDesired(previous)) {
            this.ignoredShiftPreferenceCount -= 1;
        }
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.ignoredShiftPreferenceCount += 1;
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
        this.incompleteWeekendsCount -= t.getIncompleteWeekendPenalty();
        this.totalAssignmentsOutOfBounds -= t.countAssignmentsOutsideBounds();
        this.totalWeekedsOverLimit -= t.countWeekendsOutsideBounds();
        t.remove(shift);
        this.totalWeekedsOverLimit += t.countWeekendsOutsideBounds();
        this.totalAssignmentsOutOfBounds += t.countAssignmentsOutsideBounds();
        this.incompleteWeekendsCount += t.getIncompleteWeekendPenalty();
        this.successionPenalty += t.getSuccessionPenalty();
        // determine preference penalty
        if (shift.getShiftType() != null && !shift.isDesired()) {
            this.ignoredShiftPreferenceCount -= 1;
        }
    }

}

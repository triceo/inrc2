package org.optaplanner.examples.inrc2.solver.score;

import java.util.LinkedHashMap;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;
import org.optaplanner.examples.inrc2.domain.Skill;

public class StaffingTracker {

    private int nursesMissingFromMinimal;
    private int nursesMissingFromOptimal;
    private final Roster roster;

    private final Map<ShiftType, ShiftTracker[]> shifts = new LinkedHashMap<ShiftType, ShiftTracker[]>();

    public StaffingTracker(final Roster r) {
        this.roster = r;
        this.nursesMissingFromMinimal = r.getTotalMinimalRequirements();
        this.nursesMissingFromOptimal = r.getTotalOptimalRequirements();
    }

    public void add(final Shift shift) {
        final Skill skill = shift.getSkill();
        if (skill == null) { // nurse is not actually assigned
            return;
        }
        final ShiftTracker t = this.forShift(shift);
        final int previousMissing = t.countNursesMissingFromMinimal(skill);
        final int previousOptimal = t.countNursesMissingFromOptimal(skill);
        t.add(shift);
        final int nowMissing = t.countNursesMissingFromMinimal(skill);
        final int nowOptimal = t.countNursesMissingFromOptimal(skill);
        this.updateMinimalMissingCounts(previousMissing, nowMissing);
        this.updateOptimalMissingCounts(previousOptimal, nowOptimal);
    }

    public int countNursesMissingFromMinimal() {
        return this.nursesMissingFromMinimal;
    }

    public int countNursesMissingFromOptimal() {
        return this.nursesMissingFromOptimal;
    }

    private ShiftTracker forShift(final Shift s) {
        return this.forShift(s.getShiftType(), s.getDay());
    }

    private ShiftTracker forShift(final ShiftType st, final DayOfWeek day) {
        ShiftTracker[] m = this.shifts.get(st);
        if (m == null) {
            m = new ShiftTracker[DayOfWeek.values().length];
            this.shifts.put(st, m);
        }
        final int dayNumber = day.getNumber();
        ShiftTracker t = m[dayNumber];
        if (t == null) {
            t = new ShiftTracker(day, this.roster.getRequirementByShiftTypeAndSkill(st));
            m[dayNumber] = t;
        }
        return t;
    }

    public void changeShiftType(final Shift shift, final ShiftType previous) {
        final Skill skill = shift.getSkill();
        if (skill == null) {
            return;
        }
        // remove nurse from old shift
        final ShiftTracker prev = this.forShift(previous, shift.getDay());
        final int previousMissing1 = prev.countNursesMissingFromMinimal(skill);
        final int previousOptimal1 = prev.countNursesMissingFromOptimal(skill);
        prev.remove(shift);
        final int nowMissing1 = prev.countNursesMissingFromMinimal(skill);
        final int nowOptimal1 = prev.countNursesMissingFromOptimal(skill);
        this.updateMinimalMissingCounts(previousMissing1, nowMissing1);
        this.updateOptimalMissingCounts(previousOptimal1, nowOptimal1);
        // add them to new shift
        final ShiftTracker current = this.forShift(shift);
        final int previousMissing2 = current.countNursesMissingFromMinimal(skill);
        final int previousOptimal2 = current.countNursesMissingFromOptimal(skill);
        current.add(shift);
        final int nowMissing2 = current.countNursesMissingFromMinimal(skill);
        final int nowOptimal2 = current.countNursesMissingFromOptimal(skill);
        this.updateMinimalMissingCounts(previousMissing2, nowMissing2);
        this.updateOptimalMissingCounts(previousOptimal2, nowOptimal2);
    }

    /**
     * Must never be called with {@link Shift#getSkill()} == null.
     *
     * @param shift
     * @param previous
     */
    public void changeSkill(final Shift shift, final Skill previous) {
        final Skill skill = shift.getSkill();
        if (skill == null) {
            throw new IllegalStateException("Should never happen.");
        }
        final ShiftTracker t = this.forShift(shift);
        final int previousMissingCurrent = t.countNursesMissingFromMinimal(skill);
        final int previousMissingPrevious = t.countNursesMissingFromMinimal(previous);
        final int previousOptimalCurrent = t.countNursesMissingFromOptimal(skill);
        final int previousOptimalPrevious = t.countNursesMissingFromOptimal(previous);
        t.changeSkill(shift, previous);
        final int nowMissingPrevious = t.countNursesMissingFromMinimal(previous);
        final int nowMissingCurrent = t.countNursesMissingFromMinimal(skill);
        final int nowOptimalPrevious = t.countNursesMissingFromOptimal(previous);
        final int nowOptimalCurrent = t.countNursesMissingFromOptimal(skill);
        this.updateMinimalMissingCounts(previousMissingPrevious, nowMissingPrevious);
        this.updateMinimalMissingCounts(previousMissingCurrent, nowMissingCurrent);
        this.updateOptimalMissingCounts(previousOptimalPrevious, nowOptimalPrevious);
        this.updateOptimalMissingCounts(previousOptimalCurrent, nowOptimalCurrent);
    }

    /**
     * Remove a nurse that is assigned to a shift.
     *
     */
    public void remove(final Shift shift) {
        this.remove(shift, shift.getSkill());
    }

    /**
     * Remove a nurse.
     *
     * @param shift
     * @param skill
     *            The skill assigned to the shift. This is necessary, as when unsetting the skill, it will have already
     *            been null at the time of reaching this method.
     */
    public void remove(final Shift shift, final Skill skill) {
        final ShiftTracker t = this.forShift(shift);
        final int previousMissing = t.countNursesMissingFromMinimal(skill);
        final int previousOptimal = t.countNursesMissingFromOptimal(skill);
        t.remove(skill, shift.getNurse());
        final int nowMissing = t.countNursesMissingFromMinimal(skill);
        final int nowOptimal = t.countNursesMissingFromOptimal(skill);
        this.updateMinimalMissingCounts(previousMissing, nowMissing);
        this.updateOptimalMissingCounts(previousOptimal, nowOptimal);
    }

    private void updateMinimalMissingCounts(final int previous, final int now) {
        if (previous > now) {
            this.nursesMissingFromMinimal -= 1;
        } else if (previous < now) {
            this.nursesMissingFromMinimal += 1;
        }
    }

    private void updateOptimalMissingCounts(final int previous, final int now) {
        if (previous > now) {
            this.nursesMissingFromOptimal -= 1;
        } else if (previous < now) {
            this.nursesMissingFromOptimal += 1;
        }
    }
}

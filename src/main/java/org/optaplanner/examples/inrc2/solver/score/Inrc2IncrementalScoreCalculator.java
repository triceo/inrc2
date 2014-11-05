package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;

public class Inrc2IncrementalScoreCalculator implements IncrementalScoreCalculator<Roster> {

    private static final int COMPLETE_WEEKENDS_WEIGHT = 30;
    private static final int CONSECUTIVE_DAYS_OFF_WEIGHT = 30;
    private static final int CONSECUTIVE_DAYS_ON_WEIGHT = 30;
    private static final int CONSECUTIVE_SHIFTS_WEIGHT = 15;
    private static final int PREFERENCE_WEIGHT = 10;
    private static final int SUBOMPTIMAL_WEIGHT = 30;
    private static final int TOTAL_ASSIGNMENTS_WEIGHT = 20;
    private static final int WORKING_WEEKENDS_WEIGHT = 30;

    private NurseTracker nurseTracker;
    private Nurse previousNurse;

    private StaffingTracker staffingTracker;

    private void addShift(final Shift entity) {
        this.nurseTracker.add(entity);
        this.staffingTracker.add(entity);
    }

    @Override
    public void afterEntityAdded(final Object entity) {
        this.addShift((Shift) entity);
    }

    @Override
    public void afterEntityRemoved(final Object entity) {
        this.removeShift((Shift) entity);
    }

    @Override
    public void afterVariableChanged(final Object entity, final String variableName) {
        final Shift shift = (Shift) entity;
        final Nurse currentNurse = shift.getNurse();
        if (this.previousNurse == null) {
            this.onNurseSet(shift);
        } else if (currentNurse == null) {
            this.onNurseUnset(shift, this.previousNurse);
        } else {
            this.onNurseUpdated(shift, this.previousNurse);
        }
        this.previousNurse = null;
    }

    @Override
    public void beforeEntityAdded(final Object entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public void beforeEntityRemoved(final Object entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public void beforeVariableChanged(final Object entity, final String variableName) {
        final Shift shift = (Shift) entity;
        this.previousNurse = shift.getNurse();
    }

    @Override
    public HardSoftScore calculateScore() {
        final int hard = -(this.nurseTracker.countInvalidShiftSuccessions() +
                this.staffingTracker.countNursesMissingFromMinimal() +
                this.nurseTracker.countOverbookedNurses());
        final int soft = -(this.nurseTracker.countIgnoredShiftPreferences() * Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT +
                this.nurseTracker.countIncompleteWeekends() * Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT +
                this.nurseTracker.countWeekendsOutOfBounds() * Inrc2IncrementalScoreCalculator.WORKING_WEEKENDS_WEIGHT +
                this.nurseTracker.countAssignmentsOutOfBounds() * Inrc2IncrementalScoreCalculator.TOTAL_ASSIGNMENTS_WEIGHT +
                this.staffingTracker.countNursesMissingFromOptimal() * Inrc2IncrementalScoreCalculator.SUBOMPTIMAL_WEIGHT +
                this.nurseTracker.countConsecutiveWorkingDayViolations() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_ON_WEIGHT +
                this.nurseTracker.countConsecutiveShiftTypeViolations() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_SHIFTS_WEIGHT +
                this.nurseTracker.countConsecutiveDayOffViolations() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_OFF_WEIGHT);
        return HardSoftScore.valueOf(hard, soft);
    }

    public HardSoftScore calculateScoreWithOutput() {
        System.out.println("Illegal shift type succession constraints: " + this.nurseTracker.countInvalidShiftSuccessions());
        System.out.println("Minimal coverage constraints:              " + this.staffingTracker.countNursesMissingFromMinimal());
        System.out.println("Total assignment constraints:              " + this.nurseTracker.countAssignmentsOutOfBounds() * Inrc2IncrementalScoreCalculator.TOTAL_ASSIGNMENTS_WEIGHT);
        System.out.println("Consecutive constraints:                   " + (this.nurseTracker.countConsecutiveWorkingDayViolations() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_ON_WEIGHT + this.nurseTracker.countConsecutiveShiftTypeViolations() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_SHIFTS_WEIGHT));
        System.out.println("Non working days constraints:              " + this.nurseTracker.countConsecutiveDayOffViolations() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_OFF_WEIGHT);
        System.out.println("Preferences:                               " + this.nurseTracker.countIgnoredShiftPreferences() * Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT);
        System.out.println("Max working weekend:                       " + this.nurseTracker.countWeekendsOutOfBounds() * Inrc2IncrementalScoreCalculator.WORKING_WEEKENDS_WEIGHT);
        System.out.println("Complete weekends:                         " + this.nurseTracker.countIncompleteWeekends() * Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT);
        System.out.println("Optimal coverage constraints:              " + this.staffingTracker.countNursesMissingFromOptimal() * Inrc2IncrementalScoreCalculator.SUBOMPTIMAL_WEIGHT);
        return this.calculateScore();
    }

    private void onNurseSet(final Shift entity) {
        this.nurseTracker.add(entity); // nurse has been assigned
        this.staffingTracker.add(entity);
    }

    private void onNurseUnset(final Shift entity, final Nurse previous) {
        this.nurseTracker.remove(entity, previous); // nurse has been unassigned
        this.staffingTracker.remove(entity, previous);
    }

    private void onNurseUpdated(final Shift entity, final Nurse previous) {
        this.nurseTracker.remove(entity, previous);
        this.nurseTracker.add(entity);
        this.staffingTracker.remove(entity, previous);
        this.staffingTracker.add(entity);
    }

    private void removeShift(final Shift entity) {
        this.nurseTracker.remove(entity);
        this.staffingTracker.remove(entity);
    }

    @Override
    public void resetWorkingSolution(final Roster workingSolution) {
        this.nurseTracker = new NurseTracker(workingSolution);
        this.staffingTracker = new StaffingTracker(workingSolution);
        for (final Shift shift : workingSolution.getShifts()) {
            this.addShift(shift);
        }
    }

}

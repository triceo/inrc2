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

    private ScoreKeeper keeper;
    private Nurse previousNurse;

    private void addShift(final Shift entity) {
        this.keeper.add(entity);
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
        final int hard = -(this.keeper.getInvalidShiftSuccessionCount() +
                this.keeper.getNurseMissingFromMinimalCount() +
                this.keeper.getOverbookedNurseCount());
        final int soft = -(this.keeper.countIgnoredShiftPreferences() * Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT +
                this.keeper.getIncompleteWeekendsCount() * Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT +
                this.keeper.getWeekendsOverLimitCount() * Inrc2IncrementalScoreCalculator.WORKING_WEEKENDS_WEIGHT +
                this.keeper.getAssignmentsOutOfBoundsCount() * Inrc2IncrementalScoreCalculator.TOTAL_ASSIGNMENTS_WEIGHT +
                this.keeper.getNurseMissingFromOptimalCount() * Inrc2IncrementalScoreCalculator.SUBOMPTIMAL_WEIGHT +
                this.keeper.getConsecutiveWorkingDayViolationsCount() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_ON_WEIGHT +
                this.keeper.getConsecutiveShiftTypeViolationsCount() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_SHIFTS_WEIGHT +
                this.keeper.getConsecutiveDayOffViolationsCount() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_OFF_WEIGHT);
        return HardSoftScore.valueOf(hard, soft);
    }

    public HardSoftScore calculateScoreWithOutput() {
        System.out.println("Illegal shift type succession constraints: " + this.keeper.getInvalidShiftSuccessionCount());
        System.out.println("Minimal coverage constraints:              " + this.keeper.getNurseMissingFromMinimalCount());
        System.out.println("Total assignment constraints:              " + this.keeper.getAssignmentsOutOfBoundsCount() * Inrc2IncrementalScoreCalculator.TOTAL_ASSIGNMENTS_WEIGHT);
        System.out.println("Consecutive constraints (On):              " + this.keeper.getConsecutiveWorkingDayViolationsCount() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_ON_WEIGHT);
        System.out.println("Consecutive constraints (Shift):           " + this.keeper.getConsecutiveShiftTypeViolationsCount() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_SHIFTS_WEIGHT);
        System.out.println("Non working days constraints:              " + this.keeper.getConsecutiveDayOffViolationsCount() * Inrc2IncrementalScoreCalculator.CONSECUTIVE_DAYS_OFF_WEIGHT);
        System.out.println("Preferences:                               " + this.keeper.countIgnoredShiftPreferences() * Inrc2IncrementalScoreCalculator.PREFERENCE_WEIGHT);
        System.out.println("Max working weekend:                       " + this.keeper.getWeekendsOverLimitCount() * Inrc2IncrementalScoreCalculator.WORKING_WEEKENDS_WEIGHT);
        System.out.println("Complete weekends:                         " + this.keeper.getIncompleteWeekendsCount() * Inrc2IncrementalScoreCalculator.COMPLETE_WEEKENDS_WEIGHT);
        System.out.println("Optimal coverage constraints:              " + this.keeper.getNurseMissingFromOptimalCount() * Inrc2IncrementalScoreCalculator.SUBOMPTIMAL_WEIGHT);
        return this.calculateScore();
    }

    private void onNurseSet(final Shift entity) {
        this.keeper.add(entity); // nurse has been assigned
    }

    private void onNurseUnset(final Shift entity, final Nurse previous) {
        this.keeper.remove(entity, previous); // nurse has been unassigned
    }

    private void onNurseUpdated(final Shift entity, final Nurse previous) {
        this.keeper.remove(entity, previous);
        this.keeper.add(entity);
    }

    private void removeShift(final Shift entity) {
        this.keeper.remove(entity);
    }

    @Override
    public void resetWorkingSolution(final Roster workingSolution) {
        this.keeper = new ScoreKeeper(workingSolution);
        for (final Shift shift : workingSolution.getShifts()) {
            this.addShift(shift);
        }
    }

}

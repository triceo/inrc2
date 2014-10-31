package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;
import org.optaplanner.examples.inrc2.domain.Skill;

public class Inrc2IncrementalScoreCalculator implements IncrementalScoreCalculator<Roster> {

    public static final int COMPLETE_WEEKENDS_WEIGHT = 30;
    public static final int PREFERENCE_WEIGHT = 10;
    public static final int WORKING_WEEKENDS_WEIGHT = 30;

    private NurseTracker nurseTracker;
    private ShiftType previousShiftType;

    private Skill previousSkill;

    private void addShift(final Shift entity) {
        this.nurseTracker.add(entity);
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
        if (variableName.equals("skill")) {
            final Skill currentSkill = shift.getSkill();
            if (this.previousSkill == null) {
                this.onSkillSet(shift);
            } else if (currentSkill == null) {
                this.onSkillUnset(shift, this.previousSkill);
            } else {
                this.onSkillUpdated(shift, this.previousSkill);
            }
        } else if (variableName.equals("shiftType")) {
            final ShiftType currentShiftType = shift.getShiftType();
            if (this.previousShiftType == null) {
                this.onShiftTypeSet(shift);
            } else if (currentShiftType == null) {
                this.onShiftTypeUnset(shift, this.previousShiftType);
            } else {
                this.onShiftTypeUpdated(shift, this.previousShiftType);
            }
        }
        this.previousSkill = null;
        this.previousShiftType = null;
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
        this.previousShiftType = shift.getShiftType();
        this.previousSkill = shift.getSkill();
    }

    @Override
    public BendableScore calculateScore() {
        final int hard = -this.nurseTracker.getSuccessionPenalty();
        final int soft = -(this.nurseTracker.getPreferencePenalty() + this.nurseTracker.getIncompleteWeekendsPenalty() + this.nurseTracker.getTotalWorkingWeekendsPenalty());
        return BendableScore.valueOf(new int[]{hard, 0}, new int[]{soft, 0, 0});
    }

    private void onShiftTypeSet(final Shift entity) {
        this.nurseTracker.changeShiftType(entity, null);
    }

    private void onShiftTypeUnset(final Shift entity, final ShiftType previous) {
        this.nurseTracker.changeShiftType(entity, previous);
    }

    private void onShiftTypeUpdated(final Shift entity, final ShiftType previous) {
        this.nurseTracker.changeShiftType(entity, previous);
    }

    private void onSkillSet(final Shift entity) {

    }

    private void onSkillUnset(final Shift entity, final Skill previous) {

    }

    private void onSkillUpdated(final Shift entity, final Skill previous) {

    }

    private void removeShift(final Shift entity) {
        this.nurseTracker.remove(entity);
    }

    @Override
    public void resetWorkingSolution(final Roster workingSolution) {
        this.nurseTracker = new NurseTracker(workingSolution);
        for (final Shift shift : workingSolution.getShifts()) {
            this.addShift(shift);
        }
    }

}

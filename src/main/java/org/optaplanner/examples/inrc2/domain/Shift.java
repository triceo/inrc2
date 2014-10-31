package org.optaplanner.examples.inrc2.domain;

import java.util.Collection;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

// one nurse at most 1 shift a day; hard-codes the H1 hard constraint
@PlanningEntity(difficultyComparatorClass = NurseDifficultyComparator.class)
public class Shift {

    private DayOfWeek day;

    private Nurse nurse;

    private ShiftType shiftType;

    private Skill skill;

    protected Shift() {
        // Planner cloning prevent immutability
    }

    protected Shift(final Nurse nurse, final DayOfWeek day) {
        this.nurse = nurse;
        this.day = day;
    }

    public DayOfWeek getDay() {
        return this.day;
    }

    public Nurse getNurse() {
        return this.nurse;
    }
    
    public boolean isDesired() {
        return this.isDesired(this.getShiftType());
    }

    public boolean isDesired(ShiftType shiftType) {
        if (shiftType == null) {
            throw new IllegalStateException("Null shift type. Cannot tell whether desired or not.");
        }
        return !getNurse().shiftOffRequested(day, shiftType);
    }

    @ValueRangeProvider(id = "nurseSkills")
    public Collection<Skill> getNurseSkills() {
        return this.getNurse().getSkills();
    }

    @PlanningVariable(valueRangeProviderRefs = "shiftType")
    public ShiftType getShiftType() {
        return this.shiftType;
    }

    /**
     * Null means the nurse is not assigned to this shift.
     */
    @PlanningVariable(nullable = true, valueRangeProviderRefs = "nurseSkills")
    public Skill getSkill() {
        return this.skill;
    }

    public void setDay(final DayOfWeek day) {
        this.day = day;
    }

    public void setNurse(final Nurse nurse) {
        this.nurse = nurse;
    }

    public void setShiftType(final ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public void setSkill(final Skill skill) {
        this.skill = skill;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Shift [day=").append(this.day).append(", shiftType=").append(this.shiftType).append(", nurse=").append(this.nurse).append(", skill=").append(this.skill).append("]");
        return builder.toString();
    }

}

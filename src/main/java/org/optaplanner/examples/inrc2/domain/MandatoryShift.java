package org.optaplanner.examples.inrc2.domain;

import java.util.Collection;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public final class MandatoryShift extends Shift {

    private Nurse nurse;
    
    protected MandatoryShift() {
        super();
    }
    
    protected MandatoryShift(DayOfWeek day, ShiftType shiftType, Skill skill, Collection<Nurse> suitableNurses) {
        super(day, shiftType, skill, suitableNurses);
    }

    // TODO report a bug in planner; range providers not inherited
    @Override
    @ValueRangeProvider(id = "mandatoryNurses")
    public Collection<Nurse> getSuitableNurses() {
        return super.getSuitableNurses();
    }

    @Override
    @PlanningVariable(valueRangeProviderRefs = {"mandatoryNurses"})
    public Nurse getNurse() {
        return nurse;
    }

    @Override
    public void setNurse(Nurse nurse) {
        this.nurse = nurse;
    }

}

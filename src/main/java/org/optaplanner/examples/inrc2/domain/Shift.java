package org.optaplanner.examples.inrc2.domain;

import java.util.Collection;
import java.util.Collections;

public abstract class Shift {

    private DayOfWeek day;

    private ShiftType shiftType;

    private Skill skill;

    private Collection<Nurse> suitableNurses;

    protected Shift() {

    }

    protected Shift(final DayOfWeek day, final ShiftType shiftType, final Skill skill, final Collection<Nurse> suitableNurses) {
        this.day = day;
        this.shiftType = shiftType;
        this.skill = skill;
        this.suitableNurses = Collections.unmodifiableCollection(suitableNurses);
    }

    public DayOfWeek getDay() {
        return this.day;
    }

    public abstract Nurse getNurse();

    public ShiftType getShiftType() {
        return this.shiftType;
    }

    public Skill getSkill() {
        return this.skill;
    }

    public Collection<Nurse> getSuitableNurses() {
        return this.suitableNurses;
    }

    public boolean isDesired() {
        return this.isDesired(this.getShiftType());
    }

    public boolean isDesired(final ShiftType shiftType) {
        if (this.getNurse() == null) {
            throw new IllegalStateException("Null nurse. Cannot tell whether desired or not.");
        }
        return !this.getNurse().shiftOffRequested(this.day, shiftType);
    }

    public abstract void setNurse(Nurse nurse);

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append(" [day=").append(this.day).append(", shiftType=").append(this.shiftType).append(", skill=").append(this.skill).append(", getNurse()=").append(this.getNurse()).append("]");
        return builder.toString();
    }

}

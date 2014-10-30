package org.optaplanner.examples.inrc2.domain;

public class ShiftOff {

    private final ShiftType type;

    private final DayOfWeek when;

    public ShiftOff(final DayOfWeek d, final ShiftType st) {
        this.when = d;
        this.type = st;
    }

    public ShiftType getType() {
        return this.type;
    }

    public DayOfWeek getWhen() {
        return this.when;
    }

}

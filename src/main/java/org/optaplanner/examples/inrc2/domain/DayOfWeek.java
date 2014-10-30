package org.optaplanner.examples.inrc2.domain;

public enum DayOfWeek {

    FRIDAY(4), MONDAY(0), SATURDAY(5, true), SUNDAY(6, true), THURSDAY(3), TUESDAY(1), WEDNESDAY(2);

    private final boolean isWeekend;

    private final int number;

    DayOfWeek(final int number) {
        this(number, false);
    }

    DayOfWeek(final int number, final boolean isWeekend) {
        this.number = number;
        this.isWeekend = isWeekend;
    }

    public int getNumber() {
        return this.number;
    }

    public boolean isWeekend() {
        return this.isWeekend;
    }

}

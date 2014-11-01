package org.optaplanner.examples.inrc2.domain;

public enum DayOfWeek {

    MONDAY(0, "Mon"), TUESDAY(1, "Tue"), WEDNESDAY(2, "Wed"), THURSDAY(3, "Thu"), FRIDAY(4, "Fri"), SATURDAY(5, "Sat", true), SUNDAY(6, "Sun", true);

    private final String abbreviation;

    private final boolean isWeekend;
    private final int number;

    DayOfWeek(final int number, final String abbreviation) {
        this(number, abbreviation, false);
    }

    DayOfWeek(final int number, final String abbreviation, final boolean isWeekend) {
        this.number = number;
        this.abbreviation = abbreviation;
        this.isWeekend = isWeekend;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public int getNumber() {
        return this.number;
    }

    public boolean isWeekend() {
        return this.isWeekend;
    }

}

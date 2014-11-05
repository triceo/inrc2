package org.optaplanner.examples.inrc2.domain;

import java.util.EnumMap;
import java.util.Map;

public enum DayOfWeek {

    MONDAY(0, "Mon"), TUESDAY(1, "Tue"), WEDNESDAY(2, "Wed"), THURSDAY(3, "Thu"), FRIDAY(4, "Fri"), SATURDAY(5, "Sat", true), SUNDAY(6, "Sun", true);

    private static final Map<DayOfWeek, DayOfWeek> TO_NEXT = new EnumMap<DayOfWeek, DayOfWeek>(DayOfWeek.class);
    private static final Map<DayOfWeek, DayOfWeek> TO_PREVIOUS = new EnumMap<DayOfWeek, DayOfWeek>(DayOfWeek.class);
    static {
        DayOfWeek.TO_PREVIOUS.put(TUESDAY, MONDAY);
        DayOfWeek.TO_PREVIOUS.put(WEDNESDAY, TUESDAY);
        DayOfWeek.TO_PREVIOUS.put(THURSDAY, WEDNESDAY);
        DayOfWeek.TO_PREVIOUS.put(FRIDAY, THURSDAY);
        DayOfWeek.TO_PREVIOUS.put(SATURDAY, FRIDAY);
        DayOfWeek.TO_PREVIOUS.put(SUNDAY, SATURDAY);
        DayOfWeek.TO_NEXT.put(MONDAY, TUESDAY);
        DayOfWeek.TO_NEXT.put(TUESDAY, WEDNESDAY);
        DayOfWeek.TO_NEXT.put(WEDNESDAY, THURSDAY);
        DayOfWeek.TO_NEXT.put(THURSDAY, FRIDAY);
        DayOfWeek.TO_NEXT.put(FRIDAY, SATURDAY);
        DayOfWeek.TO_NEXT.put(SATURDAY, SUNDAY);
    }

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

    public DayOfWeek getNext() {
        return DayOfWeek.TO_NEXT.get(this);
    }

    public int getNumber() {
        return this.number;
    }

    public DayOfWeek getPrevious() {
        return DayOfWeek.TO_PREVIOUS.get(this);
    }

    public boolean isWeekend() {
        return this.isWeekend;
    }
}

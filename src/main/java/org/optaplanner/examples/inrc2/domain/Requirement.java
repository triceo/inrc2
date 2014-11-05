package org.optaplanner.examples.inrc2.domain;

import java.util.Arrays;

import org.apache.commons.math3.util.Pair;

public class Requirement {

    private final int[] minimalRequirement = new int[7];

    private final int[] optimalRequirement = new int[7];

    private final ShiftType shiftType;

    private final Skill skill;

    public Requirement(final ShiftType shiftType, final Skill skill, final Pair<Integer, Integer> monday, final Pair<Integer, Integer> tuesday, final Pair<Integer, Integer> wednesday, final Pair<Integer, Integer> thursday, final Pair<Integer, Integer> friday, final Pair<Integer, Integer> saturday, final Pair<Integer, Integer> sunday) {
        if (shiftType == null || skill == null) {
            throw new IllegalStateException("Neither shift type nor skill may be null.");
        }
        this.shiftType = shiftType;
        this.skill = skill;
        this.setRequirement(DayOfWeek.MONDAY, monday);
        this.setRequirement(DayOfWeek.TUESDAY, tuesday);
        this.setRequirement(DayOfWeek.WEDNESDAY, wednesday);
        this.setRequirement(DayOfWeek.THURSDAY, thursday);
        this.setRequirement(DayOfWeek.FRIDAY, friday);
        this.setRequirement(DayOfWeek.SATURDAY, saturday);
        this.setRequirement(DayOfWeek.SUNDAY, sunday);
    }

    public int getMinimal(final DayOfWeek d) {
        return this.minimalRequirement[d.getNumber()];
    }

    public int getOptimal(final DayOfWeek d) {
        return this.optimalRequirement[d.getNumber()];
    }

    public ShiftType getShiftType() {
        return this.shiftType;
    }

    public Skill getSkill() {
        return this.skill;
    }

    private void setRequirement(final DayOfWeek day, final Pair<Integer, Integer> data) {
        this.minimalRequirement[day.getNumber()] = data.getFirst();
        this.optimalRequirement[day.getNumber()] = data.getSecond();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("WeeklyRequirement [shiftType=").append(this.shiftType).append(", skill=").append(this.skill).append(", minimalRequirement=").append(Arrays.toString(this.minimalRequirement)).append(", optimalRequirement=").append(Arrays.toString(this.optimalRequirement)).append("]");
        return builder.toString();
    }
}

package org.optaplanner.examples.inrc2.domain;

import java.util.Arrays;

import org.apache.commons.math3.util.Pair;

public class Requirement {

    private final boolean[] isRequiredOn = new boolean[7];

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
        this.minimalRequirement[DayOfWeek.MONDAY.getNumber()] = monday.getFirst();
        this.minimalRequirement[DayOfWeek.TUESDAY.getNumber()] = tuesday.getFirst();
        this.minimalRequirement[DayOfWeek.WEDNESDAY.getNumber()] = wednesday.getFirst();
        this.minimalRequirement[DayOfWeek.THURSDAY.getNumber()] = thursday.getFirst();
        this.minimalRequirement[DayOfWeek.FRIDAY.getNumber()] = friday.getFirst();
        this.minimalRequirement[DayOfWeek.SATURDAY.getNumber()] = saturday.getFirst();
        this.minimalRequirement[DayOfWeek.SUNDAY.getNumber()] = sunday.getFirst();
        this.optimalRequirement[DayOfWeek.MONDAY.getNumber()] = monday.getSecond();
        this.optimalRequirement[DayOfWeek.TUESDAY.getNumber()] = tuesday.getSecond();
        this.optimalRequirement[DayOfWeek.WEDNESDAY.getNumber()] = wednesday.getSecond();
        this.optimalRequirement[DayOfWeek.THURSDAY.getNumber()] = thursday.getSecond();
        this.optimalRequirement[DayOfWeek.FRIDAY.getNumber()] = friday.getSecond();
        this.optimalRequirement[DayOfWeek.SATURDAY.getNumber()] = saturday.getSecond();
        this.optimalRequirement[DayOfWeek.FRIDAY.getNumber()] = sunday.getSecond();
        for (final DayOfWeek d : DayOfWeek.values()) {
            final int num = d.getNumber();
            this.isRequiredOn[num] = (this.minimalRequirement[num] > 0) || (this.optimalRequirement[num] > 0);
        }
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

    public boolean isRequired(final DayOfWeek d) {
        return this.isRequiredOn[d.getNumber()];
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("WeeklyRequirement [shiftType=").append(this.shiftType).append(", skill=").append(this.skill).append(", minimalRequirement=").append(Arrays.toString(this.minimalRequirement)).append(", optimalRequirement=").append(Arrays.toString(this.optimalRequirement)).append("]");
        return builder.toString();
    }
}

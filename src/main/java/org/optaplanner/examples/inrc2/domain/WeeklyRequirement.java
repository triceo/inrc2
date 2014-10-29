package org.optaplanner.examples.inrc2.domain;

import java.util.Arrays;

import org.apache.commons.math3.util.Pair;

public class WeeklyRequirement {

    private final int[] minimalRequirement = new int[7];

    private final int[] optimalRequirement = new int[7];

    private final ShiftType shiftType;

    private final Skill skill;

    public WeeklyRequirement(final ShiftType shiftType, final Skill skill, final Pair<Integer, Integer> monday, final Pair<Integer, Integer> tuesday, final Pair<Integer, Integer> wednesday, final Pair<Integer, Integer> thursday, final Pair<Integer, Integer> friday, final Pair<Integer, Integer> saturday, final Pair<Integer, Integer> sunday) {
        this.shiftType = shiftType;
        this.skill = skill;
        this.minimalRequirement[0] = monday.getFirst();
        this.minimalRequirement[1] = tuesday.getFirst();
        this.minimalRequirement[2] = wednesday.getFirst();
        this.minimalRequirement[3] = thursday.getFirst();
        this.minimalRequirement[4] = friday.getFirst();
        this.minimalRequirement[5] = saturday.getFirst();
        this.minimalRequirement[6] = sunday.getFirst();
        this.optimalRequirement[0] = monday.getSecond();
        this.optimalRequirement[1] = tuesday.getSecond();
        this.optimalRequirement[2] = wednesday.getSecond();
        this.optimalRequirement[3] = thursday.getSecond();
        this.optimalRequirement[4] = friday.getSecond();
        this.optimalRequirement[5] = saturday.getSecond();
        this.optimalRequirement[6] = sunday.getSecond();
    }

    public int getFridayMinimal() {
        return this.getMinimal(4);
    }

    public int getFridayOptimal() {
        return this.getOptimal(4);
    }

    private int getMinimal(final int i) {
        return this.minimalRequirement[i];
    }

    public int getMondayMinimal() {
        return this.getMinimal(0);
    }

    public int getMondayOptimal() {
        return this.getOptimal(0);
    }

    private int getOptimal(final int i) {
        return this.optimalRequirement[i];
    }

    public int getSaturdayMinimal() {
        return this.getMinimal(5);
    }

    public int getSaturdayOptimal() {
        return this.getOptimal(5);
    }

    public ShiftType getShiftType() {
        return this.shiftType;
    }

    public Skill getSkill() {
        return this.skill;
    }

    public int getSundayMinimal() {
        return this.getMinimal(6);
    }

    public int getSundayOptimal() {
        return this.getOptimal(6);
    }

    public int getThursdayMinimal() {
        return this.getMinimal(3);
    }

    public int getThursdayOptimal() {
        return this.getOptimal(3);
    }

    public int getTuesdayMinimal() {
        return this.getMinimal(1);
    }

    public int getTuesdayOptimal() {
        return this.getOptimal(1);
    }

    public int getWednesdayMinimal() {
        return this.getMinimal(2);
    }

    public int getWednesOptimal() {
        return this.getOptimal(2);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("WeeklyRequirement [shiftType=").append(this.shiftType).append(", skill=").append(this.skill).append(", minimalRequirement=").append(Arrays.toString(this.minimalRequirement)).append(", optimalRequirement=").append(Arrays.toString(this.optimalRequirement)).append("]");
        return builder.toString();
    }
}

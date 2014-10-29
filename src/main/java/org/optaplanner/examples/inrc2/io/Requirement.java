package org.optaplanner.examples.inrc2.io;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.Pair;

class Requirement {

    private final List<Pair<Integer, Integer>> requirements;

    private final String shiftTypeId;

    private final String skillId;

    public Requirement(final String shiftTypeId, final String skillId, final Pair<Integer, Integer>... days) {
        if (days.length < 7) {
            throw new IllegalArgumentException("Need 1 requirement for each day of the week.");
        }
        this.shiftTypeId = shiftTypeId;
        this.skillId = skillId;
        this.requirements = Arrays.asList(days);
    }

    public Pair<Integer, Integer> getRequirementForDay(final int dayNo) {
        return this.requirements.get(dayNo);
    }

    public String getShiftTypeId() {
        return this.shiftTypeId;
    }

    public String getSkillId() {
        return this.skillId;
    }

}

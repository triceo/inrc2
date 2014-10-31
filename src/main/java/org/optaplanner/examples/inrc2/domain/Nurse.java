package org.optaplanner.examples.inrc2.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Nurse {

    private final Contract contract;

    private final String id;

    private final int numPreviousAssignments, numPreviousWorkingWeekends, numPreviousConsecutiveAssignments,
    numPreviousConsecutiveDaysOn, numPreviousConsecutiveDaysOff;

    private final ShiftType previousAssignedShiftType;

    private final Map<DayOfWeek, Set<ShiftType>> requestedShiftsOff = new EnumMap<DayOfWeek, Set<ShiftType>>(DayOfWeek.class);

    private final Set<Skill> skills;

    public Nurse(final String name, final Contract contract, final Set<Skill> hasSkills, final Set<ShiftOff> shiftsOff, final ShiftType previousAssignedShiftType, final int numPreviousAssignments, final int numPreviousConsecutiveAssignments, final int numPreviousConsecutiveDaysOn, final int numPreviousConsecutiveDaysOff, final int numPreviousWorkingWeekends) {
        this.id = name;
        if (contract == null) {
            throw new IllegalArgumentException("No contract for nurse " + name);
        }
        this.contract = contract;
        if (hasSkills.isEmpty()) {
            throw new IllegalArgumentException("No skills for nurse " + name);
        }
        this.skills = Collections.unmodifiableSet(new LinkedHashSet<Skill>(hasSkills));
        this.previousAssignedShiftType = previousAssignedShiftType;
        this.numPreviousAssignments = numPreviousAssignments;
        this.numPreviousConsecutiveAssignments = numPreviousConsecutiveAssignments;
        this.numPreviousConsecutiveDaysOff = numPreviousConsecutiveDaysOff;
        this.numPreviousConsecutiveDaysOn = numPreviousConsecutiveDaysOn;
        this.numPreviousWorkingWeekends = numPreviousWorkingWeekends;
        for (final ShiftOff off : shiftsOff) {
            final DayOfWeek when = off.getWhen();
            if (!this.requestedShiftsOff.containsKey(when)) {
                this.requestedShiftsOff.put(when, new HashSet<ShiftType>());
            }
            this.requestedShiftsOff.get(when).add(off.getType());
        }
    }

    public Contract getContract() {
        return this.contract;
    }

    public String getId() {
        return this.id;
    }

    public int getNumPreviousAssignments() {
        return this.numPreviousAssignments;
    }

    public int getNumPreviousConsecutiveAssignments() {
        return this.numPreviousConsecutiveAssignments;
    }

    public int getNumPreviousConsecutiveDaysOff() {
        return this.numPreviousConsecutiveDaysOff;
    }

    public int getNumPreviousConsecutiveDaysOn() {
        return this.numPreviousConsecutiveDaysOn;
    }

    public int getNumPreviousWorkingWeekends() {
        return this.numPreviousWorkingWeekends;
    }

    public ShiftType getPreviousAssignedShiftType() {
        return this.previousAssignedShiftType;
    }

    public Set<Skill> getSkills() {
        return this.skills;
    }

    public boolean shiftOffRequested(final DayOfWeek day, final ShiftType type) {
        if (!this.requestedShiftsOff.containsKey(day)) {
            return false;
        } else {
            return this.requestedShiftsOff.get(day).contains(type);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Nurse [id=").append(this.id).append("]");
        return builder.toString();
    }

}

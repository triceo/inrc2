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
        // basic sanity checks
        if (previousAssignedShiftType == null) {
            if (numPreviousConsecutiveAssignments > 0) {
                throw new IllegalStateException("No immediately previous shift, yet previous consecutive assignments > 0.");
            } else if (numPreviousConsecutiveDaysOn > 0) {
                throw new IllegalStateException("No immediately previous shift, yet previous consecutive days on > 0.");
            } else if (numPreviousConsecutiveDaysOff < 1) {
                throw new IllegalStateException("No immediately previous shift, yet previous consecutive days off < 1.");
            }
        } else {
            if (numPreviousConsecutiveDaysOff > 0) {
                throw new IllegalStateException("Have immediately previous shift, yet previous consecutive days off > 0.");
            } else if (numPreviousConsecutiveAssignments < 1) {
                throw new IllegalStateException("Have immediately previous shift, yet previous consecutive assignments < 1.");
            } else if (numPreviousConsecutiveDaysOn < 1) {
                throw new IllegalStateException("Have immediately previous shift, yet previous consecutive days on < 1.");
            }
        }
        if (contract == null) {
            throw new IllegalArgumentException("No contract for nurse " + name);
        }
        if (hasSkills.isEmpty()) {
            throw new IllegalArgumentException("No skills for nurse " + name);
        }
        // and fill with data
        this.id = name;
        this.contract = contract;
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

    public int getNumPreviousConsecutiveAssignmentsOfSameShiftType() {
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

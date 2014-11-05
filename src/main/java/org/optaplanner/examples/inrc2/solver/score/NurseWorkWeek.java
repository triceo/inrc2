package org.optaplanner.examples.inrc2.solver.score;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.ShiftType;

class NurseWorkWeek {

    private final Map<DayOfWeek, List<ShiftType>> dailyShifts = new EnumMap<DayOfWeek, List<ShiftType>>(DayOfWeek.class);

    private final Nurse nurse;

    public NurseWorkWeek(final Nurse n) {
        this.nurse = n;
    }

    public void addShiftType(final DayOfWeek d, final ShiftType st) {
        this.getShiftTypes(d).add(st);
    }

    public int countAssignedDays() {
        return this.dailyShifts.size();
    }

    public Nurse getNurse() {
        return this.nurse;
    }

    public ShiftType getShiftType(final DayOfWeek d) {
        if (!this.isOccupied(d)) {
            return null;
        }
        final List<ShiftType> shifts = this.dailyShifts.get(d);
        if (shifts.size() == 1) {
            return shifts.get(0);
        } else {
            throw new IllegalStateException("Nurse is overbooked. You need to check for this explicitly.");
        }
    }

    /**
     * Can only be used after we have made sure that the collection containsKey(day). Otherwise,
     * {@link #countAssignedDays()} will break.
     *
     * @param day
     * @return
     */
    private List<ShiftType> getShiftTypes(final DayOfWeek day) {
        List<ShiftType> shiftTypes = this.dailyShifts.get(day);
        if (shiftTypes == null) {
            shiftTypes = new LinkedList<ShiftType>();
            this.dailyShifts.put(day, shiftTypes);
        }
        return shiftTypes;
    }

    public boolean isOccupied(final DayOfWeek d) {
        return this.dailyShifts.containsKey(d);
    }

    public boolean isOverbooked(final DayOfWeek d) {
        return this.isOccupied(d) && this.getShiftTypes(d).size() > 1;
    }

    public void removeShiftType(final DayOfWeek d, final ShiftType st) {
        final List<ShiftType> types = this.getShiftTypes(d);
        final boolean result = types.remove(st);
        if (!result) { // defensive programming
            throw new IllegalArgumentException("Removing a shift type not previously assigned.");
        }
        if (types.isEmpty()) {
            this.dailyShifts.remove(d);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("NurseWorkWeek [dailyShifts=").append(this.dailyShifts).append(", nurse=").append(this.nurse).append("]");
        return builder.toString();
    }

}

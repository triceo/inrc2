package org.optaplanner.examples.inrc2.io;

class NurseHistory {

    private final String id;

    private final int numAssignments, numWorkingWeekends, numConsecutiveAssignments, numConsecutiveDaysOn,
    numConsecutiveDaysOff;

    private final String shiftTypeId;

    public NurseHistory(final String id, final String shiftTypeId, final int numAssignments, final int numWorkingWeekends, final int numConsecutiveAssignments, final int numConsecutiveDaysOn, final int numConsecutiveDaysOff) {
        this.id = id;
        this.shiftTypeId = shiftTypeId;
        this.numAssignments = numAssignments;
        this.numWorkingWeekends = numWorkingWeekends;
        this.numConsecutiveAssignments = numConsecutiveAssignments;
        this.numConsecutiveDaysOff = numConsecutiveDaysOff;
        this.numConsecutiveDaysOn = numConsecutiveDaysOn;
    }

    public String getId() {
        return this.id;
    }

    public int getNumAssignments() {
        return this.numAssignments;
    }

    public int getNumConsecutiveAssignments() {
        return this.numConsecutiveAssignments;
    }

    public int getNumConsecutiveDaysOff() {
        return this.numConsecutiveDaysOff;
    }

    public int getNumConsecutiveDaysOn() {
        return this.numConsecutiveDaysOn;
    }

    public int getNumWorkingWeekends() {
        return this.numWorkingWeekends;
    }

    public String getShiftTypeId() {
        return this.shiftTypeId;
    }

}

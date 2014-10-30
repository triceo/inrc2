package org.optaplanner.examples.inrc2.io;

class ShiftOffRequest {

    private final String day;

    private final String nurseId;

    private final String shiftTypeId;

    public ShiftOffRequest(final String shiftTypeId, final String nurseId, final String day) {
        this.shiftTypeId = shiftTypeId;
        this.nurseId = nurseId;
        this.day = day;
    }

    public String getDay() {
        return this.day;
    }

    public String getNurseId() {
        return this.nurseId;
    }

    public String getShiftTypeId() {
        return this.shiftTypeId;
    }

}

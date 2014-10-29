package org.optaplanner.examples.inrc2.domain;

public class Contract {

    private final boolean completeWeekends;

    private final String id;

    private final int maxAssignments;

    private final int maxConsecutiveDaysOff;

    private final int maxConsecutiveDaysOn;

    private final int maxWorkingWeekends;

    private final int minAssignments;

    private final int minConsecutiveDaysOff;

    private final int minConsecutiveDaysOn;

    public Contract(final String name, final int minAssignments, final int maxAssignments, final int minConsecutiveDaysOn, final int maxConsecutiveDaysOn, final int minConsecutiveDaysOff, final int maxConsecutiveDaysOff, final int maxWorkingWeekends, final boolean completeWeekends) {
        this.id = name;
        this.minAssignments = minAssignments;
        this.maxAssignments = maxAssignments;
        this.minConsecutiveDaysOff = minConsecutiveDaysOff;
        this.maxConsecutiveDaysOff = maxConsecutiveDaysOff;
        this.minConsecutiveDaysOn = minConsecutiveDaysOn;
        this.maxConsecutiveDaysOn = maxConsecutiveDaysOn;
        this.maxWorkingWeekends = maxWorkingWeekends;
        this.completeWeekends = completeWeekends;
    }

    public String getId() {
        return this.id;
    }

    public int getMaxAssignments() {
        return this.maxAssignments;
    }

    public int getMaxConsecutiveDaysOff() {
        return this.maxConsecutiveDaysOff;
    }

    public int getMaxConsecutiveDaysOn() {
        return this.maxConsecutiveDaysOn;
    }

    public int getMaxWorkingWeekends() {
        return this.maxWorkingWeekends;
    }

    public int getMinAssignments() {
        return this.minAssignments;
    }

    public int getMinConsecutiveDaysOff() {
        return this.minConsecutiveDaysOff;
    }

    public int getMinConsecutiveDaysOn() {
        return this.minConsecutiveDaysOn;
    }

    public boolean isCompleteWeekends() {
        return this.completeWeekends;
    }

}

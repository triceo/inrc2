package org.optaplanner.examples.inrc2.io;

class ShiftTypePrototype {

    private final String id;

    private final int minConsecutiveAssignments, maxConsecutiveAssignments;

    public ShiftTypePrototype(final String name, final int minConsecutiveAssignments, final int maxConsecutiveAssignments) {
        this.id = name;
        this.minConsecutiveAssignments = minConsecutiveAssignments;
        this.maxConsecutiveAssignments = maxConsecutiveAssignments;
    }

    public String getId() {
        return this.id;
    }

    public int getMaxConsecutiveAssignments() {
        return this.maxConsecutiveAssignments;
    }

    public int getMinConsecutiveAssignments() {
        return this.minConsecutiveAssignments;
    }

}

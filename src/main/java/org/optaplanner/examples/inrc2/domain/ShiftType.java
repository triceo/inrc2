package org.optaplanner.examples.inrc2.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ShiftType {

    private final Set<ShiftType> forbiddenSuccessions;

    private final String id;

    private final int minConsecutiveAssignments, maxConsecutiveAssignments;

    public ShiftType(final String name, final int minConsecutiveAssignments, final int maxConsecutiveAssignments, final Set<ShiftType> forbiddenSuccessions) {
        this.id = name;
        this.minConsecutiveAssignments = minConsecutiveAssignments;
        this.maxConsecutiveAssignments = maxConsecutiveAssignments;
        this.forbiddenSuccessions = Collections.unmodifiableSet(new LinkedHashSet<ShiftType>(forbiddenSuccessions));
    }

    public Set<ShiftType> getForbiddenSuccessions() {
        return this.forbiddenSuccessions;
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

    public boolean isAllowedAfter(final ShiftType type) {
        if (type == null) { // null = no previous shift
            return true;
        }
        return type.isAllowedBefore(this);
    }

    public boolean isAllowedBefore(final ShiftType type) {
        if (type == null) { // null = no next shift
            return true;
        }
        return !this.forbiddenSuccessions.contains(type);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ShiftType [id=").append(this.id).append("]");
        return builder.toString();
    }

}

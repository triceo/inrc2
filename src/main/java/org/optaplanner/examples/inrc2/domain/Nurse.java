package org.optaplanner.examples.inrc2.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Nurse {

    private final Contract contract;

    private final String id;

    private final Set<Skill> skills;

    public Nurse(final String name, final Contract contract, final Set<Skill> hasSkills) {
        this.id = name;
        if (contract == null) {
            throw new IllegalArgumentException("No contract for nurse " + name);
        }
        this.contract = contract;
        if (hasSkills.isEmpty()) {
            throw new IllegalArgumentException("No skills for nurse " + name);
        }
        this.skills = Collections.unmodifiableSet(new LinkedHashSet<Skill>(hasSkills));
    }

    public Contract getContract() {
        return this.contract;
    }

    public String getId() {
        return this.id;
    }

    public Set<Skill> getSkills() {
        return this.skills;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Nurse [id=").append(this.id).append(", contract=").append(this.contract).append(", skills=").append(this.skills).append("]");
        return builder.toString();
    }

}

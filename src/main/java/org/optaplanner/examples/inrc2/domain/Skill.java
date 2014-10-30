package org.optaplanner.examples.inrc2.domain;

public class Skill {

    private final String id;

    public Skill(final String name) {
        this.id = name;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Skill [id=").append(this.id).append("]");
        return builder.toString();
    }

}

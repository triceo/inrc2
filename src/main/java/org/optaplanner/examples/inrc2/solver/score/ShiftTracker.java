package org.optaplanner.examples.inrc2.solver.score;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Requirement;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.Skill;

class ShiftTracker {

    private final DayOfWeek day;
    private final Map<Skill, Collection<Nurse>> nurses = new HashMap<Skill, Collection<Nurse>>();
    private final Map<Skill, Requirement> requirements;

    public ShiftTracker(final DayOfWeek day, final Map<Skill, Requirement> requirements) {
        this.day = day;
        this.requirements = requirements;
    }

    public boolean add(final Shift shift) {
        return this.forSkill(shift.getSkill()).add(shift.getNurse());
    }

    public int countNursesMissingFromMinimal(final Skill skill) {
        return Math.max(0, this.requirements.get(skill).getMinimal(this.day) - this.forSkill(skill).size());
    }

    private Collection<Nurse> forSkill(final Skill s) {
        Collection<Nurse> result = this.nurses.get(s);
        if (result == null) {
            result = new HashSet<Nurse>();
            this.nurses.put(s, result);
        }
        return result;
    }

    public void changeSkill(final Shift shift, final Skill previous) {
        final Nurse n = shift.getNurse();
        this.forSkill(previous).remove(n);
        this.forSkill(shift.getSkill()).add(n);
    }

    public boolean remove(final Shift shift) {
        return this.remove(shift.getSkill(), shift.getNurse());
    }

    /**
     * This is necessary, as when unsetting the skill, it will have already been null at the time of reaching this
     * method.
     *
     * @param skill
     * @param nurse
     */
    public boolean remove(final Skill skill, final Nurse nurse) {
        return this.forSkill(skill).remove(nurse);
    }

}

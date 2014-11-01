package org.optaplanner.examples.inrc2.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;

@PlanningSolution
public class Roster implements Solution<BendableScore> {

    private int consecutiveDayOffViolationsForUnusedNurses;

    private Set<Contract> contracts;

    private SortedMap<String, Contract> contractsById;

    private int currentWeekNum;

    private String id;

    private int numWeeksTotal;

    private Set<Nurse> nurses;

    private SortedMap<String, Nurse> nursesById;

    private Set<Requirement> requirements;

    private Map<ShiftType, Map<Skill, Requirement>> requirementsByShiftTypeAndSkill;

    private BendableScore score;

    private final Set<Shift> shifts = new LinkedHashSet<Shift>();

    private Set<ShiftType> shiftTypes;

    private SortedMap<String, ShiftType> shiftTypesById;

    private Set<Skill> skills;

    // iterating over .values() is slower and memory is cheap; just copy the collections
    private SortedMap<String, Skill> skillsById;

    private int totalMinimalRequirements = 0;

    private int totalOptimalRequirements = 0;

    protected Roster() {
        // planner cloning prevents immutability
    }

    public Roster(final String id, final int totalNumberOfWeeks, final int currentWeekNumber, final SortedMap<String, Nurse> nurses, final SortedMap<String, Contract> contracts, final SortedMap<String, ShiftType> shiftTypes, final SortedMap<String, Skill> skills, final Collection<Requirement> requirements) {
        this.id = id;
        this.skillsById = Collections.unmodifiableSortedMap(skills);
        this.skills = Collections.unmodifiableSet(new LinkedHashSet<Skill>(skills.values()));
        this.nursesById = Collections.unmodifiableSortedMap(nurses);
        this.nurses = Collections.unmodifiableSet(new LinkedHashSet<Nurse>(nurses.values()));
        this.contractsById = Collections.unmodifiableSortedMap(contracts);
        this.contracts = Collections.unmodifiableSet(new LinkedHashSet<Contract>(contracts.values()));
        this.shiftTypesById = Collections.unmodifiableSortedMap(shiftTypes);
        this.shiftTypes = Collections.unmodifiableSet(new LinkedHashSet<ShiftType>(shiftTypes.values()));
        final Map<ShiftType, Map<Skill, Requirement>> a = new LinkedHashMap<ShiftType, Map<Skill, Requirement>>();
        for (final Requirement requirement : requirements) {
            final ShiftType st = requirement.getShiftType();
            if (!a.containsKey(st)) {
                a.put(st, new HashMap<Skill, Requirement>());
            }
            a.get(st).put(requirement.getSkill(), requirement);
        }
        this.requirementsByShiftTypeAndSkill = Collections.unmodifiableMap(a);
        this.requirements = new LinkedHashSet<Requirement>(requirements);
        // and now create the entities
        final Set<Nurse> unusedNurses = new HashSet<Nurse>();
        for (final DayOfWeek day : DayOfWeek.values()) {
            for (final Nurse nurse : this.nurses) {
                // only add those skills to the nurse that make sense; will eliminate a lot of useless selection
                final Map<ShiftType, Set<Skill>> nurseSpecificRequirements = new HashMap<ShiftType, Set<Skill>>();
                for (final Requirement r : requirements) {
                    if (!r.isRequired(day)) {
                        // on this day, we don't need a nurse of a particular skill for a particular shift type
                        continue;
                    } else if (!nurse.getSkills().contains(r.getSkill())) {
                        // this nurse does not have that particular skill
                        continue;
                    }
                    final ShiftType st = r.getShiftType();
                    if (!nurseSpecificRequirements.containsKey(st)) {
                        nurseSpecificRequirements.put(st, new LinkedHashSet<Skill>());
                    }
                    nurseSpecificRequirements.get(st).add(r.getSkill());
                }
                if (nurseSpecificRequirements.size() == 0) {
                    // this particular nurse is not required at all
                    unusedNurses.add(nurse);
                    continue;
                }
                this.shifts.add(new Shift(nurse, day, nurseSpecificRequirements));
            }
        }
        // sum up requirements to know how many at most we need
        for (final Requirement r : this.requirements) {
            for (final DayOfWeek d : DayOfWeek.values()) {
                this.totalMinimalRequirements += r.getMinimal(d);
                this.totalOptimalRequirements += r.getOptimal(d);
            }
        }
        /*
         * calculate the day-off penalties for unused nurses; they will never make it through the score calculator, so
         * this is essentially a constant that needs to be included.
         */
        for (final Nurse n : unusedNurses) {
            final int previous = n.getNumPreviousConsecutiveDaysOff();
            final int current = DayOfWeek.values().length;
            this.consecutiveDayOffViolationsForUnusedNurses += Math.min(current, previous + current);
        }
    }

    public int getConsecutiveDayOffViolationsForUnusedNurses() {
        return this.consecutiveDayOffViolationsForUnusedNurses;
    }

    public Contract getContractById(final String id) {
        return this.contractsById.get(id);
    }

    public Set<Contract> getContracts() {
        return this.contracts;
    }

    public int getCurrentWeekNum() {
        return this.currentWeekNum;
    }

    public String getId() {
        return this.id;
    }

    public int getNumWeeksTotal() {
        return this.numWeeksTotal;
    }

    public Nurse getNurseById(final String id) {
        return this.nursesById.get(id);
    }

    public Set<Nurse> getNurses() {
        return this.nurses;
    }

    @Override
    public Collection<? extends Object> getProblemFacts() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<Skill, Requirement> getRequirementByShiftTypeAndSkill(final ShiftType type) {
        return this.requirementsByShiftTypeAndSkill.get(type);
    }

    @Override
    public BendableScore getScore() {
        return this.score;
    }

    @PlanningEntityCollectionProperty
    public Collection<Shift> getShifts() {
        return this.shifts;
    }

    public ShiftType getShiftTypeById(final String id) {
        return this.shiftTypesById.get(id);
    }

    public Set<ShiftType> getShiftTypes() {
        return this.shiftTypes;
    }

    public Skill getSkillById(final String id) {
        return this.skillsById.get(id);
    }

    public Set<Skill> getSkills() {
        return this.skills;
    }

    public int getTotalMinimalRequirements() {
        return this.totalMinimalRequirements;
    }

    public int getTotalOptimalRequirements() {
        return this.totalOptimalRequirements;
    }

    @Override
    public void setScore(final BendableScore score) {
        this.score = score;
    }

}

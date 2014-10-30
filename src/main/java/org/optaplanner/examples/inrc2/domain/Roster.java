package org.optaplanner.examples.inrc2.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;

@PlanningSolution
public class Roster implements Solution<BendableScore> {

    private Set<Contract> contracts;

    private SortedMap<String, Contract> contractsById;

    private int currentWeekNum;

    private String id;

    private int numWeeksTotal;

    private Set<Nurse> nurses;

    private SortedMap<String, Nurse> nursesById;

    private Set<Requirement> requirements;

    private Map<ShiftType, Requirement> requirementsByShiftType;

    private Map<Skill, Requirement> requirementsBySkill;

    private BendableScore score;

    private final Set<Shift> shifts = new LinkedHashSet<Shift>();

    private Set<ShiftType> shiftTypes;

    private SortedMap<String, ShiftType> shiftTypesById;

    private Set<Skill> skills;

    // iterating over .values() is slower and memory is cheap; just copy the collections
    private SortedMap<String, Skill> skillsById;

    protected Roster() {
        // planner cloning prevents immutability
    }

    public Roster(final String id, final int totalNumberOfWeeks, final int currentWeekNumber, final SortedMap<String, Nurse> nurses, final SortedMap<String, Contract> contracts, final SortedMap<String, ShiftType> shiftTypes, final SortedMap<String, Skill> skills, final Collection<Requirement> requirements) {
        this.skillsById = Collections.unmodifiableSortedMap(skills);
        this.skills = Collections.unmodifiableSet(new LinkedHashSet<Skill>(skills.values()));
        this.nursesById = Collections.unmodifiableSortedMap(nurses);
        this.nurses = Collections.unmodifiableSet(new LinkedHashSet<Nurse>(nurses.values()));
        this.contractsById = Collections.unmodifiableSortedMap(contracts);
        this.contracts = Collections.unmodifiableSet(new LinkedHashSet<Contract>(contracts.values()));
        this.shiftTypesById = Collections.unmodifiableSortedMap(shiftTypes);
        this.shiftTypes = Collections.unmodifiableSet(new LinkedHashSet<ShiftType>(shiftTypes.values()));
        final Map<ShiftType, Requirement> a = new LinkedHashMap<ShiftType, Requirement>();
        final Map<Skill, Requirement> b = new LinkedHashMap<Skill, Requirement>();
        for (final Requirement requirement : requirements) {
            a.put(requirement.getShiftType(), requirement);
            b.put(requirement.getSkill(), requirement);
        }
        this.requirementsByShiftType = Collections.unmodifiableMap(a);
        this.requirementsBySkill = Collections.unmodifiableMap(b);
        this.requirements = new LinkedHashSet<Requirement>(requirements);
        // and now create the entities
        for (final DayOfWeek day : DayOfWeek.values()) {
            for (final Nurse nurse : this.nurses) {
                this.shifts.add(new Shift(nurse, day));
            }
        }
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

    public Requirement getRequirementByShiftType(final ShiftType type) {
        return this.requirementsByShiftType.get(type);
    }

    public Requirement getRequirementBySkill(final Skill skill) {
        return this.requirementsBySkill.get(skill);
    }

    public Set<Requirement> getRequirements() {
        return this.requirements;
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

    @ValueRangeProvider(id = "shiftType")
    public Set<ShiftType> getShiftTypes() {
        return this.shiftTypes;
    }

    public Skill getSkillById(final String id) {
        return this.skillsById.get(id);
    }

    public Set<Skill> getSkills() {
        return this.skills;
    }

    @Override
    public void setScore(final BendableScore score) {
        this.score = score;
    }

}

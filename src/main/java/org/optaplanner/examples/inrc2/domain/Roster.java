package org.optaplanner.examples.inrc2.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class Roster implements Solution<HardSoftScore> {

    private Set<Contract> contracts;

    private SortedMap<String, Contract> contractsById;

    private int currentWeekNum;

    private String id;

    private int maximumPenaltyForConsecutiveDaysOff = 0;

    private int numWeeksTotal;

    private Set<Nurse> nurses;
    private SortedMap<String, Nurse> nursesById;

    private Set<Requirement> requirements;

    private HardSoftScore score;

    private final List<Shift> shifts = new LinkedList<Shift>();

    private Set<ShiftType> shiftTypes;

    private SortedMap<String, ShiftType> shiftTypesById;

    private Set<Skill> skills;

    // iterating over .values() is slower and memory is cheap; just copy the collections
    private SortedMap<String, Skill> skillsById;

    private int totalMinimalRequirements = 0;

    private int totalOptimalRequirements = 0;

    private int totalPreviousAssignmentsOutsideBounds = 0;

    private int totalPreviousWorkingWeekendsOverLimit = 0;

    protected Roster() {
        // planner cloning prevents immutability
    }

    public Roster(final String id, final int totalNumberOfWeeks, final int currentWeekNumber, final SortedMap<String, Nurse> nurses, final SortedMap<String, Contract> contracts, final SortedMap<String, ShiftType> shiftTypes, final SortedMap<String, Skill> skills, final Collection<Requirement> requirements) {
        this.id = id;
        this.currentWeekNum = currentWeekNumber;
        this.numWeeksTotal = totalNumberOfWeeks;
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
        this.requirements = new LinkedHashSet<Requirement>(requirements);
        // assemble nurses suitable for a particular skill
        final Map<Skill, Set<Nurse>> suitableNursesPerSkill = new HashMap<Skill, Set<Nurse>>();
        for (final Nurse n : this.nurses) {
            // penalize already spent weekends
            final int previousWorkingWeekends = n.getNumPreviousWorkingWeekends();
            final int maxAllowedWorkingWeekends = n.getContract().getMaxWorkingWeekends();
            if (previousWorkingWeekends > maxAllowedWorkingWeekends) {
                this.totalPreviousWorkingWeekendsOverLimit += previousWorkingWeekends - maxAllowedWorkingWeekends;
            }
            // penalize already spent assignments
            final int previousAssignments = n.getNumPreviousAssignments();
            final int minAllowedAssignments = n.getContract().getMinAssignments();
            final int maxAllowedAssignments = n.getContract().getMaxAssignments();
            if (previousAssignments < minAllowedAssignments) {
                this.totalPreviousAssignmentsOutsideBounds += minAllowedAssignments - previousAssignments;
            } else if (previousAssignments > maxAllowedAssignments) {
                this.totalPreviousAssignmentsOutsideBounds += previousAssignments - maxAllowedAssignments;
            }
            // calculate the total maximum amount of day off penalties
            final int consecutiveBeforeMonday = n.getNumPreviousConsecutiveDaysOff();
            final int consecutiveIncludingBeforeMonday = consecutiveBeforeMonday + DayOfWeek.values().length;
            final int minAllowedConsecutive = n.getContract().getMinConsecutiveDaysOff();
            final int maxAllowedConsecutive = n.getContract().getMaxConsecutiveDaysOff();
            if (consecutiveIncludingBeforeMonday < minAllowedConsecutive) {
                this.maximumPenaltyForConsecutiveDaysOff += Math.min(consecutiveIncludingBeforeMonday - consecutiveBeforeMonday, minAllowedConsecutive - consecutiveIncludingBeforeMonday);
            } else if (consecutiveIncludingBeforeMonday > maxAllowedConsecutive) {
                this.maximumPenaltyForConsecutiveDaysOff += Math.min(consecutiveIncludingBeforeMonday - consecutiveBeforeMonday, consecutiveIncludingBeforeMonday - maxAllowedConsecutive);
            }
            for (final Skill s : n.getSkills()) {
                if (!suitableNursesPerSkill.containsKey(s)) {
                    suitableNursesPerSkill.put(s, new LinkedHashSet<Nurse>());
                }
                suitableNursesPerSkill.get(s).add(n);
            }
        }
        // and now create the entities
        for (final Requirement r : this.requirements) {
            final ShiftType st = r.getShiftType();
            final Skill sk = r.getSkill();
            final Set<Nurse> suitableNurses = suitableNursesPerSkill.get(sk);
            for (final DayOfWeek d : DayOfWeek.values()) {
                final int minimal = r.getMinimal(d);
                final int optimal = r.getOptimal(d);
                this.totalMinimalRequirements += minimal;
                this.totalOptimalRequirements += optimal;
                for (int i = 0; i < optimal; i++) {
                    if (i < minimal) {
                        this.shifts.add(new MandatoryShift(d, st, sk, suitableNurses));
                    } else {
                        this.shifts.add(new OptionalShift(d, st, sk, suitableNurses));
                    }
                }
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

    public int getMaximumPenaltyForConsecutiveDaysOff() {
        return this.maximumPenaltyForConsecutiveDaysOff;
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

    @Override
    public HardSoftScore getScore() {
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

    public int getTotalPreviousAssignmentsOutsideBounds() {
        return this.totalPreviousAssignmentsOutsideBounds;
    }

    public int getTotalPreviousWorkingWeekendsOverLimit() {
        return this.totalPreviousWorkingWeekendsOverLimit;
    }

    @Override
    public void setScore(final HardSoftScore score) {
        this.score = score;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Roster [id=").append(this.id).append(", currentWeekNum=").append(this.currentWeekNum).append(", numWeeksTotal=").append(this.numWeeksTotal).append(", shifts=").append(this.shifts).append(", score=").append(this.score).append("]");
        return builder.toString();
    }

}

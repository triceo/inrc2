package org.optaplanner.examples.inrc2.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.optaplanner.examples.inrc2.domain.Contract;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Requirement;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.ShiftType;
import org.optaplanner.examples.inrc2.domain.Skill;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ScenarioParser {

    public static Roster parse(final File json, final SortedMap<String, NurseHistory> histories, final int weekNumber, final Collection<WeekData> week) throws JsonProcessingException, IOException {
        final ObjectMapper om = new ObjectMapper();
        final JsonNode node = om.readTree(json);
        final String scenarioId = node.get("id").asText();
        final int numberOfWeeks = node.get("numberOfWeeks").asInt();
        final SortedMap<String, Skill> skills = ScenarioParser.parseSkills(node.withArray("skills"));
        final SortedMap<String, SortedSet<String>> forbiddenShiftSuccessions = ScenarioParser.parseForbiddenShiftSuccessions((ArrayNode) node.withArray("forbiddenShiftTypeSuccessions"));
        final SortedMap<String, ShiftType> shiftTypes = ScenarioParser.parseShiftTypes((ArrayNode) node.withArray("shiftTypes"), forbiddenShiftSuccessions);
        final SortedMap<String, Contract> contracts = ScenarioParser.parseContracts((ArrayNode) node.withArray("contracts"));
        final SortedMap<String, Nurse> nurses = ScenarioParser.parseNurses((ArrayNode) node.withArray("nurses"), contracts, shiftTypes, skills, histories);
        final List<Requirement> requirements = ScenarioParser.parseRequirements(week, skills, shiftTypes);
        return new Roster(scenarioId, numberOfWeeks, weekNumber, nurses, contracts, shiftTypes, skills, requirements);
    }

    private static SortedMap<String, Contract> parseContracts(final ArrayNode withArray) {
        final SortedMap<String, Contract> result = new TreeMap<String, Contract>();
        for (final JsonNode node : withArray) {
            final String name = node.get("id").asText();
            final int minAssignments = node.get("minimumNumberOfAssignments").asInt();
            final int maxAssignments = node.get("maximumNumberOfAssignments").asInt();
            final int minConsecutiveDaysOn = node.get("minimumNumberOfConsecutiveWorkingDays").asInt();
            final int maxConsecutiveDaysOn = node.get("maximumNumberOfConsecutiveWorkingDays").asInt();
            final int minConsecutiveDaysOff = node.get("minimumNumberOfConsecutiveDaysOff").asInt();
            final int maxConsecutiveDaysOff = node.get("maximumNumberOfConsecutiveDaysOff").asInt();
            final int maxWorkingWeekends = node.get("maximumNumberOfWorkingWeekends").asInt();
            final boolean completeWeekends = (node.get("completeWeekends").asInt() == 1);
            result.put(name, new Contract(name, minAssignments, maxAssignments, minConsecutiveDaysOn, maxConsecutiveDaysOn, minConsecutiveDaysOff, maxConsecutiveDaysOff, maxWorkingWeekends, completeWeekends));
        }
        return Collections.unmodifiableSortedMap(result);
    }

    private static SortedMap<String, SortedSet<String>> parseForbiddenShiftSuccessions(final ArrayNode withArray) {
        final SortedMap<String, SortedSet<String>> result = new TreeMap<String, SortedSet<String>>();
        for (final JsonNode node : withArray) {
            final String name = node.get("precedingShiftType").asText();
            final SortedSet<String> succeeding = new TreeSet<String>();
            for (final JsonNode type : (ArrayNode) node.withArray("succeedingShiftTypes")) {
                succeeding.add(type.asText());
            }
            result.put(name, succeeding);
        }
        return Collections.unmodifiableSortedMap(result);
    }

    private static SortedMap<String, Nurse> parseNurses(final ArrayNode withArray, final SortedMap<String, Contract> contracts, final SortedMap<String, ShiftType> shiftTypes, final SortedMap<String, Skill> skills, final SortedMap<String, NurseHistory> history) {
        final SortedMap<String, Nurse> result = new TreeMap<String, Nurse>();
        for (final JsonNode node : withArray) {
            final String name = node.get("id").asText();
            final Contract contract = contracts.get(node.get("contract").asText());
            final Set<Skill> hasSkills = new LinkedHashSet<Skill>();
            for (final JsonNode node2 : (ArrayNode) (node.withArray("skills"))) {
                hasSkills.add(skills.get(node2.asText()));
            }
            final NurseHistory nurseHistory = history.get(name);
            final ShiftType previousAssignedShiftType = shiftTypes.get(nurseHistory.getShiftTypeId());
            final int numPreviousAssignments = nurseHistory.getNumAssignments();
            final int numPreviousConsecutiveAssignments = nurseHistory.getNumConsecutiveAssignments();
            final int numPreviousConsecutiveDaysOff = nurseHistory.getNumConsecutiveDaysOff();
            final int numPreviousConsecutiveDaysOn = nurseHistory.getNumConsecutiveDaysOn();
            final int numPreviousWorkingWeekends = nurseHistory.getNumWorkingWeekends();
            result.put(name, new Nurse(name, contract, hasSkills, previousAssignedShiftType, numPreviousAssignments, numPreviousConsecutiveAssignments, numPreviousConsecutiveDaysOn, numPreviousConsecutiveDaysOff, numPreviousWorkingWeekends));
        }
        return Collections.unmodifiableSortedMap(result);
    }

    private static List<Requirement> parseRequirements(final Collection<WeekData> requirements, final SortedMap<String, Skill> skills, final SortedMap<String, ShiftType> shiftTypes) {
        final List<Requirement> result = new ArrayList<Requirement>();
        for (final WeekData r : requirements) {
            final ShiftType st = shiftTypes.get(r.getShiftTypeId());
            final Skill s = skills.get(r.getSkillId());
            result.add(new Requirement(st, s, r.getRequirementForDay(0), r.getRequirementForDay(1), r.getRequirementForDay(2), r.getRequirementForDay(3), r.getRequirementForDay(4), r.getRequirementForDay(5), r.getRequirementForDay(6)));
        }
        return Collections.unmodifiableList(result);
    }

    private static SortedMap<String, ShiftType> parseShiftTypes(final ArrayNode withArray, final Map<String, SortedSet<String>> forbiddenShiftSuccessions) {
        // read the JSON for shift types
        final SortedMap<String, ShiftTypePrototype> shiftTypePrototypes = new TreeMap<String, ShiftTypePrototype>();
        for (final JsonNode node : withArray) {
            final String name = node.get("id").asText();
            final int minConsecutiveAssignments = node.get("minimumNumberOfConsecutiveAssignments").asInt();
            final int maxConsecutiveAssignments = node.get("maximumNumberOfConsecutiveAssignments").asInt();
            shiftTypePrototypes.put(name, new ShiftTypePrototype(name, minConsecutiveAssignments, maxConsecutiveAssignments));
        }
        /**
         * In order to maintain immutability for the ShiftTypes, we will attempt to sort forbidden shift successions so
         * that the ones with fewer dependencies come first. This code will assume that there are no circular
         * dependencies.
         */
        final SortedMap<String, ShiftType> alreadyKnownShiftTypes = new TreeMap<String, ShiftType>();
        final Random random = new Random();
        while (!shiftTypePrototypes.isEmpty()) {
            // choose one available shift type at random
            final List<String> ids = new LinkedList<String>(shiftTypePrototypes.keySet());
            final String shiftTypeId = ids.get(random.nextInt(ids.size()));
            // check if all the pre-requisities are there to create the shift type
            boolean isReady = true;
            final Set<ShiftType> prerequisities = new LinkedHashSet<ShiftType>();
            for (final String forbiddenSucceedingShiftTypeId : forbiddenShiftSuccessions.get(shiftTypeId)) {
                if (!alreadyKnownShiftTypes.containsKey(forbiddenSucceedingShiftTypeId)) {
                    isReady = false;
                    break;
                }
                prerequisities.add(alreadyKnownShiftTypes.get(forbiddenSucceedingShiftTypeId));
            }
            if (!isReady) {
                // we cannot yet create this shift type
                continue;
            }
            final ShiftTypePrototype stp = shiftTypePrototypes.get(shiftTypeId);
            final ShiftType st = new ShiftType(stp.getId(), stp.getMinConsecutiveAssignments(), stp.getMaxConsecutiveAssignments(), prerequisities);
            alreadyKnownShiftTypes.put(shiftTypeId, st);
            shiftTypePrototypes.remove(shiftTypeId);
        }
        return Collections.unmodifiableSortedMap(alreadyKnownShiftTypes);
    }

    private static SortedMap<String, Skill> parseSkills(final JsonNode withArray) {
        final SortedMap<String, Skill> result = new TreeMap<String, Skill>();
        for (final JsonNode node : withArray) {
            final String name = node.asText();
            result.put(name, new Skill(name));
        }
        return Collections.unmodifiableSortedMap(result);
    }

}

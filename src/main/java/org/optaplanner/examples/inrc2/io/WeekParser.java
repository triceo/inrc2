package org.optaplanner.examples.inrc2.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class WeekParser {

    private static Pair<Integer, Integer> getRequirement(final JsonNode day) {
        return new Pair<Integer, Integer>(day.get("minimum").asInt(), day.get("optimal").asInt());
    }

    @SuppressWarnings("unchecked")
    public static Pair<List<ShiftOffRequest>, List<WeekData>> parse(final File json) throws JsonProcessingException, IOException {
        final ObjectMapper om = new ObjectMapper();
        final JsonNode node = om.readTree(json);
        final List<WeekData> requirements = new ArrayList<WeekData>();
        for (final JsonNode node2 : (ArrayNode) node.withArray("requirements")) {
            final String shiftTypeId = node2.get("shiftType").asText();
            final String skillId = node2.get("skill").asText();
            final Pair<Integer, Integer> monday = WeekParser.getRequirement(node2.get("requirementOnMonday"));
            final Pair<Integer, Integer> tuesday = WeekParser.getRequirement(node2.get("requirementOnTuesday"));
            final Pair<Integer, Integer> wednesday = WeekParser.getRequirement(node2.get("requirementOnWednesday"));
            final Pair<Integer, Integer> thursday = WeekParser.getRequirement(node2.get("requirementOnThursday"));
            final Pair<Integer, Integer> friday = WeekParser.getRequirement(node2.get("requirementOnFriday"));
            final Pair<Integer, Integer> saturday = WeekParser.getRequirement(node2.get("requirementOnSaturday"));
            final Pair<Integer, Integer> sunday = WeekParser.getRequirement(node2.get("requirementOnSunday"));
            requirements.add(new WeekData(shiftTypeId, skillId, monday, tuesday, wednesday, thursday, friday, saturday, sunday));
        }
        final List<ShiftOffRequest> requests = new ArrayList<ShiftOffRequest>();
        for (final JsonNode node2 : (ArrayNode) node.withArray("shiftOffRequests")) {
            final String shiftTypeId = node2.get("shiftType").asText();
            final String nurseId = node2.get("nurse").asText();
            final String day = node2.get("day").asText();
            requests.add(new ShiftOffRequest(shiftTypeId, nurseId, day));
        }
        return new Pair<List<ShiftOffRequest>, List<WeekData>>(Collections.unmodifiableList(requests), Collections.unmodifiableList(requirements));
    }

}

package org.optaplanner.examples.inrc2.io;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class HistoryParser {

    public static Pair<Integer, SortedMap<String, NurseHistory>> parse(final File json) throws JsonProcessingException, IOException {
        final ObjectMapper om = new ObjectMapper();
        final JsonNode node = om.readTree(json);
        final int weekNumber = node.get("week").asInt();
        final SortedMap<String, NurseHistory> histories = new TreeMap<String, NurseHistory>();
        for (final JsonNode node2 : (ArrayNode) node.withArray("nurseHistory")) {
            final String id = node2.get("nurse").asText();
            final int numAssignments = node2.get("numberOfAssignments").asInt();
            final int numWorkingWeekends = node2.get("numberOfWorkingWeekends").asInt();
            final String shiftTypeId = node2.get("lastAssignedShiftType").asText();
            final int numConsecutiveAssignments = node2.get("numberOfConsecutiveAssignments").asInt();
            final int numConsecutiveDaysOn = node2.get("numberOfConsecutiveWorkingDays").asInt();
            final int numConsecutiveDaysOff = node2.get("numberOfConsecutiveDaysOff").asInt();
            histories.put(id, new NurseHistory(id, shiftTypeId, numAssignments, numWorkingWeekends, numConsecutiveAssignments, numConsecutiveDaysOn, numConsecutiveDaysOff));
        }
        return new Pair<Integer, SortedMap<String, NurseHistory>>(weekNumber, Collections.unmodifiableSortedMap(histories));
    }

}

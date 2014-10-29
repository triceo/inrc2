package org.optaplanner.examples.inrc2.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.optaplanner.examples.inrc2.domain.Scenario;

import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(Parameterized.class)
public class ScenarioParserTest {

    @Parameters(name = "S: {0}, W: {1}, H: {2}")
    public static Iterable<Object[]> getScenarios() {
        final List<File> scenarios = new ArrayList<File>();
        final Iterator<File> files = FileUtils.iterateFiles(new File("data/inrc2/input"), new String[]{"json"}, true);
        // find all scenarios
        while (files.hasNext()) {
            final File next = files.next();
            if (next.isDirectory()) {
                continue;
            } else if (!next.canRead()) {
                continue;
            } else if (!next.getName().startsWith("S")) {
                continue;
            }
            scenarios.add(next);
        }
        // for all scenarios, cross-reference all histories and weeks
        final List<Object[]> result = new ArrayList<Object[]>();
        for (final File scenario : scenarios) {
            final File folder = scenario.getParentFile();
            final Iterator<File> otherFiles = FileUtils.iterateFiles(folder, new String[]{"json"}, false);
            // find all weeks and histories for that scenario
            final List<File> weeks = new ArrayList<File>();
            final List<File> histories = new ArrayList<File>();
            while (otherFiles.hasNext()) {
                final File next = otherFiles.next();
                if (next.isDirectory()) {
                    continue;
                } else if (!next.canRead()) {
                    continue;
                } else if (next.getName().startsWith("W")) {
                    weeks.add(next);
                } else if (next.getName().startsWith("H")) {
                    histories.add(next);
                }
            }
            // and create every possible triple
            for (final File week : weeks) {
                for (final File history : histories) {
                    result.add(new Object[]{scenario, week, history});
                }
            }
        }
        return result;
    }

    private final File scenario, week, nurseHistory;

    public ScenarioParserTest(final File scenario, final File week, final File nurseHistory) {
        this.scenario = scenario;
        this.week = week;
        this.nurseHistory = nurseHistory;
    }

    @Test
    public void testParsing() throws JsonProcessingException, IOException {
        final Pair<Integer, SortedMap<String, NurseHistory>> h = HistoryParser.parse(this.nurseHistory);
        final Collection<Requirement> requirements = WeekParser.parse(this.week);
        final Scenario result = ScenarioParser.parse(this.scenario, h.getSecond(), h.getFirst(), requirements);
        Assert.assertNotNull(result);
    }

}

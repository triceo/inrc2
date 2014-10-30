package org.optaplanner.examples.inrc2.io;

import java.io.File;
import java.util.Collection;
import java.util.SortedMap;

import org.apache.commons.math3.util.Pair;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class Inrc2SolutionFileIO implements SolutionFileIO {

    public static final String CUSTOM_INPUT_FILENAME = "input.json";
    public static final String CUSTOM_OUTPUT_FILENAME = "output.json";
    public static final String HISTORY_FILENAME = "history.json";
    public static final String SCENARIO_FILENAME = "scenario.json";
    public static final String SOLUTION_FILENAME = "solution.json";
    public static final String WEEK_DATA_FILENAME = "week.json";

    @Override
    public String getInputFileExtension() {
        return ".json";
    }

    @Override
    public String getOutputFileExtension() {
        return ".json";
    }

    @Override
    public Solution read(final File arg0) {
        final File scenario = new File(arg0, Inrc2SolutionFileIO.SCENARIO_FILENAME);
        final File history = new File(arg0, Inrc2SolutionFileIO.HISTORY_FILENAME);
        final File week = new File(arg0, Inrc2SolutionFileIO.WEEK_DATA_FILENAME);
        try {
            final Pair<Integer, SortedMap<String, NurseHistory>> histories = HistoryParser.parse(history);
            final Collection<WeekData> requirements = WeekParser.parse(week);
            return ScenarioParser.parse(scenario, histories.getSecond(), histories.getFirst(), requirements);
        } catch (final Exception e) {
            throw new IllegalStateException("Parser failure.", e);
        }
    }

    @Override
    public void write(final Solution arg0, final File arg1) {
        // TODO Auto-generated method stub

    }

}

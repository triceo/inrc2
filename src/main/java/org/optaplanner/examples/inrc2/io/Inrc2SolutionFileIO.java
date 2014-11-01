package org.optaplanner.examples.inrc2.io;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;
import org.optaplanner.examples.inrc2.domain.Skill;
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
    public Solution<HardMediumSoftScore> read(final File arg0) {
        final File scenario = new File(arg0, Inrc2SolutionFileIO.SCENARIO_FILENAME);
        final File history = new File(arg0, Inrc2SolutionFileIO.HISTORY_FILENAME);
        final File week = new File(arg0, Inrc2SolutionFileIO.WEEK_DATA_FILENAME);
        try {
            final Pair<Integer, SortedMap<String, NurseHistory>> histories = HistoryParser.parse(history);
            final Pair<List<ShiftOffRequest>, List<WeekData>> weekData = WeekParser.parse(week);
            return ScenarioParser.parse(scenario, histories.getSecond(), histories.getFirst(), weekData.getSecond(), weekData.getFirst());
        } catch (final Exception e) {
            throw new IllegalStateException("Parser failure.", e);
        }
    }

    @Override
    public void write(final Solution arg0, final File arg1) {
        final Roster r = (Roster) arg0;
        final List<String> lines = new LinkedList<String>();
        lines.add("{");
        lines.add("  \"scenario\" : \"" + r.getId() + "\",");
        lines.add("  \"week\" : " + r.getCurrentWeekNum() + ",");
        lines.add("  \"assignments\" : [ {");
        final Map<ShiftType, Map<Skill, Set<Shift>>> ordered = new LinkedHashMap<ShiftType, Map<Skill, Set<Shift>>>();
        int total = 0;
        for (final Shift s : r.getShifts()) { // sort the assignments so that the solutions are human-readable
            final Skill skill = s.getSkill();
            if (skill == null) { // nurse not assigned to anything
                continue;
            }
            final ShiftType st = s.getShiftType();
            if (!ordered.containsKey(st)) {
                ordered.put(st, new LinkedHashMap<Skill, Set<Shift>>());
            }
            final Map<Skill, Set<Shift>> subordered = ordered.get(st);
            if (!subordered.containsKey(skill)) {
                subordered.put(skill, new LinkedHashSet<Shift>());
            }
            subordered.get(skill).add(s);
            total++;
        }
        int i = 1;
        for (final Map<Skill, Set<Shift>> perSkill : ordered.values()) {
            for (final Set<Shift> shifts : perSkill.values()) {
                for (final Shift s : shifts) {
                    lines.add("    \"nurse\" : \"" + s.getNurse().getId() + "\",");
                    lines.add("    \"day\" : \"" + s.getDay().getAbbreviation() + "\",");
                    lines.add("    \"shiftType\" : \"" + s.getShiftType().getId() + "\",");
                    lines.add("    \"skill\" : \"" + s.getSkill().getId() + "\"");
                    if (i < total) {
                        lines.add("  }, {");
                    } else {
                        lines.add("  } ]");
                    }
                    i++;
                }
            }
        }
        lines.add("}");
        try {
            FileUtils.writeLines(arg1, lines);
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed writing solution.", ex);
        }
    }
}
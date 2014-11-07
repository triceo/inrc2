package org.optaplanner.examples.inrc2.solver.score;

import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.optaplanner.examples.inrc2.domain.Contract;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;
import org.optaplanner.examples.inrc2.domain.ShiftType;

public class ConsecutiveDayOffCounter extends Counter {

    public static int countConsecutiveDayOffViolations(final NurseWorkWeek week) {
        final Nurse nurse = week.getNurse();
        final int consecutiveBefore = nurse.getNumPreviousConsecutiveDaysOff();
        final Contract contract = nurse.getContract();
        final int minConsecutive = contract.getMinConsecutiveDaysOff();
        final int maxConsecutive = contract.getMaxConsecutiveDaysOff();
        int totalViolations = 0;
        final List<Pair<ShiftType, Integer>> chunks = SuccessionEvaluator.tokenize(week, week.getNurse().getPreviousAssignedShiftType(), consecutiveBefore);
        for (int i = 0; i < chunks.size(); i++) {
            final Pair<ShiftType, Integer> chunk = chunks.get(i);
            final ShiftType type = chunk.getFirst();
            if (type != null) { // not a day off
                continue;
            }
            final int consecutives = chunk.getSecond();
            if (i == 0) {
                totalViolations += ConsecutiveDayOffCounter.countViolations(consecutiveBefore, consecutives, minConsecutive, maxConsecutive);
            } else {
                totalViolations += ConsecutiveDayOffCounter.countViolations(consecutives, minConsecutive, maxConsecutive);
            }
        }
        return totalViolations;
    }

    private static int countViolations(final int consecutive, final int minAllowedConsecutive, final int maxAllowedConsecutive) {
        if (consecutive > maxAllowedConsecutive) {
            return consecutive - maxAllowedConsecutive;
        } else if (consecutive < minAllowedConsecutive) {
            return minAllowedConsecutive - consecutive;
        }
        return 0;
    }

    private static int countViolations(final int consecutiveBeforeMonday, final int consecutiveIncludingBeforeMonday, final int minAllowedConsecutive, final int maxAllowedConsecutive) {
        if (consecutiveIncludingBeforeMonday < minAllowedConsecutive) {
            return Math.min(consecutiveIncludingBeforeMonday - consecutiveBeforeMonday, minAllowedConsecutive - consecutiveIncludingBeforeMonday);
        } else if (consecutiveIncludingBeforeMonday > maxAllowedConsecutive) {
            return Math.min(consecutiveIncludingBeforeMonday - consecutiveBeforeMonday, consecutiveIncludingBeforeMonday - maxAllowedConsecutive);
        }
        return 0;
    }

    private void after(final NurseWorkWeek week) {
        this.count += ConsecutiveDayOffCounter.countConsecutiveDayOffViolations(week);
    }

    @Override
    public void afterAdded(final Shift shift, final NurseWorkWeek week) {
        this.after(week);
    }

    @Override
    public void afterRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.after(week);
    }

    private void before(final NurseWorkWeek week) {
        this.count -= ConsecutiveDayOffCounter.countConsecutiveDayOffViolations(week);
    }

    @Override
    public void beforeAdded(final Shift shift, final NurseWorkWeek week) {
        this.before(week);
    }

    @Override
    public void beforeRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.before(week);
    }

    @Override
    int retrieve(final ScoreKeeper tracker) {
        return tracker.getConsecutiveDayOffViolationsCount();
    }

    @Override
    public void store(final ScoreKeeper tracker) {
        tracker.setConsecutiveDayOffViolationsCount(this.count);
    }

}

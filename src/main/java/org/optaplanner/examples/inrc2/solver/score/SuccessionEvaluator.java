package org.optaplanner.examples.inrc2.solver.score;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.optaplanner.examples.inrc2.domain.Contract;
import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.ShiftType;

public class SuccessionEvaluator {

    interface ConsecutionBreakingCriteria {

        boolean breaks(ShiftType current, ShiftType previous);

    }

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
                totalViolations += SuccessionEvaluator.countViolations(consecutiveBefore, consecutives, minConsecutive, maxConsecutive);
            } else {
                totalViolations += SuccessionEvaluator.countViolations(consecutives, minConsecutive, maxConsecutive);
            }
        }
        return totalViolations;
    }

    public static int countConsecutiveShiftTypeViolations(final NurseWorkWeek week) {
        final int consecutiveBefore = week.getNurse().getNumPreviousConsecutiveAssignmentsOfSameShiftType();
        int totalViolations = 0;
        final List<Pair<ShiftType, Integer>> chunks = SuccessionEvaluator.tokenize(week, week.getNurse().getPreviousAssignedShiftType(), consecutiveBefore);
        for (int i = 0; i < chunks.size(); i++) {
            final Pair<ShiftType, Integer> chunk = chunks.get(i);
            final ShiftType type = chunk.getFirst();
            if (type == null) {
                continue;
            }
            final int consecutives = chunk.getSecond();
            final int minConsecutive = type.getMinConsecutiveAssignments();
            final int maxConsecutive = type.getMaxConsecutiveAssignments();
            if (i == 0) {
                totalViolations += SuccessionEvaluator.countViolations(consecutiveBefore, consecutives, minConsecutive, maxConsecutive);
            } else {
                totalViolations += SuccessionEvaluator.countViolations(consecutives, minConsecutive, maxConsecutive);
            }
        }
        return totalViolations;
    }

    public static int countConsecutiveWorkingDayViolations(final NurseWorkWeek week) {
        final Nurse nurse = week.getNurse();
        final int consecutiveBefore = nurse.getNumPreviousConsecutiveDaysOn();
        final Contract contract = nurse.getContract();
        final int minConsecutive = contract.getMinConsecutiveDaysOn();
        final int maxConsecutive = contract.getMaxConsecutiveDaysOn();
        int consecutives = 0;
        int totalViolations = 0;
        final List<Pair<ShiftType, Integer>> chunks = SuccessionEvaluator.tokenize(week, week.getNurse().getPreviousAssignedShiftType(), consecutiveBefore);
        chunks.add(new Pair<ShiftType, Integer>(null, 0)); // just so that all chunks are processed
        boolean isStarting = true;
        for (int i = 0; i < chunks.size(); i++) {
            final Pair<ShiftType, Integer> currentChunk = chunks.get(i);
            final ShiftType currentShiftType = currentChunk.getFirst();
            if (currentShiftType == null) { // consecutive working days are over, evaluate
                if (isStarting) {
                    totalViolations += SuccessionEvaluator.countViolations(consecutiveBefore, consecutives, minConsecutive, maxConsecutive);
                    isStarting = false;
                } else {
                    totalViolations += SuccessionEvaluator.countViolations(consecutives, minConsecutive, maxConsecutive);
                }
                consecutives = 0;
            } else {
                consecutives += currentChunk.getSecond();
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

    private static final List<Pair<ShiftType, Integer>> tokenize(final NurseWorkWeek week, final ShiftType previousShiftType, final int previousCount) {
        int unbrokenCount = previousCount;
        ShiftType previousShift = previousShiftType;
        final List<Pair<ShiftType, Integer>> chunks = new ArrayList<Pair<ShiftType, Integer>>();
        DayOfWeek currentDay = DayOfWeek.MONDAY;
        do {
            final ShiftType currentShift = week.isOverbooked(currentDay) ? previousShift : week.getShiftType(currentDay);
            if (currentShift == previousShift) {
                unbrokenCount++;
            } else {
                // shift type has just changed; add chunk
                final Pair<ShiftType, Integer> chunk = new Pair<ShiftType, Integer>(previousShift, unbrokenCount);
                chunks.add(chunk);
                unbrokenCount = 1;
            }
            previousShift = currentShift;
            currentDay = currentDay.getNext();
        } while (currentDay != null);
        if (unbrokenCount > 0) {
            final Pair<ShiftType, Integer> chunk = new Pair<ShiftType, Integer>(previousShift, unbrokenCount);
            chunks.add(chunk);
        }
        return chunks;
    }

}

package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.ShiftType;

public class SuccessionEvaluator {

    public static int countConsecutiveWorkingDayViolations(final ShiftType[] successions, final int previousConsecutiveAssignments, final int minAllowedConsecutiveWorkingDays, final int maxAllowedConsecutiveWorkingDays) {
        int totalViolations = 0;
        // left boundary
        int consecutiveOnTheLeft = previousConsecutiveAssignments;
        int firstUnseen = SuccessionTracker.getDayIndex(DayOfWeek.MONDAY);
        ShiftType unseen = successions[firstUnseen];
        if (unseen == null) {
            // cancel all the previous days
            consecutiveOnTheLeft = 0;
            firstUnseen += 1;
        } else {
            // go on for as long as there are assigned shifts
            while (unseen != null) {
                consecutiveOnTheLeft += 1;
                firstUnseen += 1;
                unseen = successions[firstUnseen];
            }
        }
        if (consecutiveOnTheLeft < minAllowedConsecutiveWorkingDays) {
            if (consecutiveOnTheLeft > 0) {
                totalViolations += Math.min(consecutiveOnTheLeft - previousConsecutiveAssignments, minAllowedConsecutiveWorkingDays - consecutiveOnTheLeft);
            }
        } else if (consecutiveOnTheLeft > maxAllowedConsecutiveWorkingDays) {
            totalViolations += Math.min(consecutiveOnTheLeft - previousConsecutiveAssignments, consecutiveOnTheLeft - maxAllowedConsecutiveWorkingDays);
        }
        // and the rest of the week
        int consecutive = 0;
        for (int i = firstUnseen; i <= SuccessionTracker.getDayIndex(DayOfWeek.SUNDAY); i++) {
            final ShiftType st = successions[i];
            if (st == null) {
                // consecutive chain is broken
                if (consecutive > 0) {
                    if (consecutive > maxAllowedConsecutiveWorkingDays) {
                        totalViolations += consecutive - maxAllowedConsecutiveWorkingDays;
                    } else if (consecutive < minAllowedConsecutiveWorkingDays) {
                        totalViolations += minAllowedConsecutiveWorkingDays - consecutive;
                    }
                    consecutive = 0;
                } else {
                    // 0 consecutive means that both this and previous were null; nothing to do
                }
            } else {
                consecutive++;
            }
        }
        return totalViolations;
    }

}

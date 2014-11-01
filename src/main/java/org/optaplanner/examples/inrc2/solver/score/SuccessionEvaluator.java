package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.ShiftType;

public class SuccessionEvaluator {

    interface ConsecutionBreakingCriteria {

        boolean breaks(ShiftType current, ShiftType previous);

    }

    public static int countConsecutiveShiftTypeViolations(final ShiftType[] successions, final int historical, final int minAllowed, final int maxAllowed) {
        return SuccessionEvaluator.countConsecutiveViolations(new ConsecutionBreakingCriteria() {

            @Override
            public boolean breaks(final ShiftType current, final ShiftType previous) {
                return current == null || current != previous;
            }
        }, successions, historical, minAllowed, maxAllowed);
    }

    private static int countConsecutiveViolations(final ConsecutionBreakingCriteria criteria, final ShiftType[] successions, final int historical, final int minAllowed, final int maxAllowed) {
        int totalViolations = 0;
        // left boundary
        int consecutiveOnTheLeft = historical;
        int firstUnseen = SuccessionTracker.getDayIndex(DayOfWeek.MONDAY);
        ShiftType previous = successions[firstUnseen - 1];
        ShiftType unseen = successions[firstUnseen];
        if (criteria.breaks(unseen, previous)) {
            // cancel all the previous days
            consecutiveOnTheLeft = 0;
            firstUnseen += 1;
        } else {
            // go on for as long as there are assigned shifts
            while (!criteria.breaks(unseen, previous)) {
                consecutiveOnTheLeft += 1;
                firstUnseen += 1;
                previous = unseen;
                unseen = successions[firstUnseen];
            }
        }
        if (consecutiveOnTheLeft < minAllowed) {
            if (consecutiveOnTheLeft > 0) {
                totalViolations += Math.min(consecutiveOnTheLeft - historical, minAllowed - consecutiveOnTheLeft);
            }
        } else if (consecutiveOnTheLeft > maxAllowed) {
            totalViolations += Math.min(consecutiveOnTheLeft - historical, consecutiveOnTheLeft - maxAllowed);
        }
        // and the rest of the week
        int consecutive = 0;
        for (int i = firstUnseen; i <= SuccessionTracker.getDayIndex(DayOfWeek.SUNDAY); i++) {
            final ShiftType prev = successions[i - 1];
            final ShiftType current = successions[i];
            if (criteria.breaks(current, prev)) {
                // consecutive chain is broken
                if (consecutive > 0) {
                    if (consecutive > maxAllowed) {
                        totalViolations += consecutive - maxAllowed;
                    } else if (consecutive < minAllowed) {
                        totalViolations += minAllowed - consecutive;
                    }
                    consecutive = 0;
                } else {
                    // 0 consecutive means that both this and previous were null; nothing to do
                }
            } else {
                consecutive++;
            }
        }
        if (totalViolations < 0) {
            throw new IllegalStateException("This should not be happening.");
        }
        return totalViolations;
    }

    public static int countConsecutiveWorkingDayViolations(final ShiftType[] successions, final int historical, final int minAllowed, final int maxAllowed) {
        return SuccessionEvaluator.countConsecutiveViolations(new ConsecutionBreakingCriteria() {

            @Override
            public boolean breaks(final ShiftType current, final ShiftType previous) {
                return current == null;
            }
        }, successions, historical, minAllowed, maxAllowed);
    }
}

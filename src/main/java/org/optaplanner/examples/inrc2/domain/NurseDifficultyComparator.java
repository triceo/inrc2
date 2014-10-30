package org.optaplanner.examples.inrc2.domain;

import java.util.Comparator;

public class NurseDifficultyComparator implements Comparator<Shift> {

    private static long getUtilization(final Nurse n) {
        long i = (n.getNumPreviousAssignments() + 1);
        i *= (n.getNumPreviousConsecutiveAssignments() + 1);
        i *= (n.getNumPreviousWorkingWeekends() + 1);
        i *= (n.getNumPreviousConsecutiveDaysOn() + 1);
        i /= (n.getNumPreviousConsecutiveDaysOff() + 1);
        return i;
    }

    public int compare(final Nurse n1, final Nurse n2) {
        final int numSkills1 = n1.getSkills().size();
        final int numSkills2 = n2.getSkills().size();
        // nurse with more skills is easier to assign to a shift
        if (numSkills1 > numSkills2) {
            return -1;
        } else if (numSkills1 < numSkills2) {
            return 1;
        } else {
            // more utilized nurses should be more difficult to assign
            final long utilization1 = NurseDifficultyComparator.getUtilization(n1);
            final long utilization2 = NurseDifficultyComparator.getUtilization(n2);
            if (utilization1 > utilization2) {
                return 1;
            } else if (utilization1 == utilization2) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    @Override
    public int compare(final Shift o1, final Shift o2) {
        return this.compare(o1.getNurse(), o2.getNurse());
    }

}

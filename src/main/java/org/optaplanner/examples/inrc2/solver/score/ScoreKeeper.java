package org.optaplanner.examples.inrc2.solver.score;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.examples.inrc2.domain.DayOfWeek;
import org.optaplanner.examples.inrc2.domain.MandatoryShift;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;

class ScoreKeeper {

    private int assignmentsOutOfBoundsCount = 0;

    private int consecutiveDayOffViolationsCount = 0;

    private int consecutiveShiftTypeViolationsCount = 0;

    private int consecutiveWorkingDayViolationsCount = 0;

    private final Counter[] counters;

    private int ignoredShiftOffRequestCount = 0;

    private int incompleteWeekendsCount = 0;

    private int invalidShiftSuccessionCount = 0;

    private int nurseMissingFromMinimalCount;

    private int nurseMissingFromOptimalCount;

    private int overbookedNurseCount = 0;

    private int weekendsOverLimitCount = 0;

    private final Map<Nurse, NurseWorkWeek> workWeeks;

    public ScoreKeeper(final Roster r) {
        this.workWeeks = new HashMap<Nurse, NurseWorkWeek>(r.getNurses().size());
        this.nurseMissingFromMinimalCount = r.getTotalMinimalRequirements();
        this.nurseMissingFromOptimalCount = r.getTotalOptimalRequirements();
        this.weekendsOverLimitCount = r.getTotalPreviousWorkingWeekendsOverLimit();
        this.assignmentsOutOfBoundsCount = r.getTotalPreviousAssignmentsOutsideBounds();
        /*
         * some nurses may never be assigned to any shift and will therefore never pass through this class. for this
         * case, we're doing the following. otherwise, days off for such nurses would not be penalized.
         */
        this.consecutiveDayOffViolationsCount = r.getMaximumPenaltyForConsecutiveDaysOff();
        this.counters = new Counter[]{
                new OverbookedNurseCounter(),
                new InvalidSuccessionCounter(),
                new IncompleteWeekendCounter(),
                new OutOfBoundsWeekendCounter(),
                new OutOfBoundsAssignmentCounter(),
                new ConsecutiveShiftCounter(),
                new ConsecutiveWorkingDayCounter(),
                new ConsecutiveDayOffCounter()
        };
    }

    public void add(final Shift shift) {
        final Nurse nurse = shift.getNurse();
        if (nurse == null) {
            return;
        }
        final NurseWorkWeek week = this.getWorkWeek(nurse);
        final DayOfWeek day = shift.getDay();
        for (final Counter c : this.counters) {
            c.reset(this);
            c.beforeAdded(shift, week);
        }
        week.addShiftType(day, shift.getShiftType());
        for (final Counter c : this.counters) {
            c.afterAdded(shift, week);
            c.store(this);
        }
        if (nurse.shiftOffRequested(day, shift.getShiftType())) {
            this.ignoredShiftOffRequestCount++;
        }
        this.nurseMissingFromOptimalCount--;
        if (shift instanceof MandatoryShift) {
            this.nurseMissingFromMinimalCount--;
        }
    }

    public int countIgnoredShiftPreferences() {
        return this.ignoredShiftOffRequestCount;
    }

    public int getAssignmentsOutOfBoundsCount() {
        return this.assignmentsOutOfBoundsCount;
    }

    public int getConsecutiveDayOffViolationsCount() {
        return this.consecutiveDayOffViolationsCount;
    }

    public int getConsecutiveShiftTypeViolationsCount() {
        return this.consecutiveShiftTypeViolationsCount;
    }

    public int getConsecutiveWorkingDayViolationsCount() {
        return this.consecutiveWorkingDayViolationsCount;
    }

    public int getIncompleteWeekendsCount() {
        return this.incompleteWeekendsCount;
    }

    public int getInvalidShiftSuccessionCount() {
        return this.invalidShiftSuccessionCount;
    }

    public int getNurseMissingFromMinimalCount() {
        return this.nurseMissingFromMinimalCount;
    }

    public int getNurseMissingFromOptimalCount() {
        return this.nurseMissingFromOptimalCount;
    }

    public int getOverbookedNurseCount() {
        return this.overbookedNurseCount;
    }

    public int getWeekendsOverLimitCount() {
        return this.weekendsOverLimitCount;
    }

    private NurseWorkWeek getWorkWeek(final Nurse n) {
        if (n == null) { // defensive programming
            throw new IllegalArgumentException("Cannot operate on null nurses.");
        }
        NurseWorkWeek w = this.workWeeks.get(n);
        if (w == null) {
            w = new NurseWorkWeek(n);
            this.workWeeks.put(n, w);
        }
        return w;
    }

    public void remove(final Shift shift) {
        this.remove(shift, shift.getNurse());
    }

    public void remove(final Shift shift, final Nurse nurse) {
        if (nurse == null) {
            return;
        }
        final NurseWorkWeek week = this.getWorkWeek(nurse);
        final DayOfWeek day = shift.getDay();
        for (final Counter c : this.counters) {
            c.reset(this);
            c.beforeRemoved(shift, nurse, week);
        }
        week.removeShiftType(day, shift.getShiftType());
        for (final Counter c : this.counters) {
            c.afterRemoved(shift, nurse, week);
            c.store(this);
        }
        if (nurse.shiftOffRequested(day, shift.getShiftType())) {
            this.ignoredShiftOffRequestCount--;
        }
        this.nurseMissingFromOptimalCount++;
        if (shift instanceof MandatoryShift) {
            this.nurseMissingFromMinimalCount++;
        }
    }

    protected void setAssignmentsOutOfBoundsCount(final int assignmentsOutOfBoundsCount) {
        this.assignmentsOutOfBoundsCount = assignmentsOutOfBoundsCount;
    }

    protected void setConsecutiveDayOffViolationsCount(final int consecutiveDayOffViolationsCount) {
        this.consecutiveDayOffViolationsCount = consecutiveDayOffViolationsCount;
    }

    protected void setConsecutiveShiftTypeViolationsCount(final int consecutiveShiftTypeViolationsCount) {
        this.consecutiveShiftTypeViolationsCount = consecutiveShiftTypeViolationsCount;
    }

    protected void setConsecutiveWorkingDayViolationsCount(final int consecutiveWorkingDayViolationsCount) {
        this.consecutiveWorkingDayViolationsCount = consecutiveWorkingDayViolationsCount;
    }

    protected void setIncompleteWeekendsCount(final int incompleteWeekendsCount) {
        this.incompleteWeekendsCount = incompleteWeekendsCount;
    }

    protected void setInvalidShiftSuccessionCount(final int invalidShiftSuccessionCount) {
        this.invalidShiftSuccessionCount = invalidShiftSuccessionCount;
    }

    protected void setOverbookedNurseCount(final int overbookedNurseCount) {
        this.overbookedNurseCount = overbookedNurseCount;
    }

    protected void setWeekendsOverLimitCount(final int weekendsOverLimitCount) {
        this.weekendsOverLimitCount = weekendsOverLimitCount;
    }

}

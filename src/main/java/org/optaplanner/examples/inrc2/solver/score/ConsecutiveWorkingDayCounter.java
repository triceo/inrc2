package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;

public class ConsecutiveWorkingDayCounter extends Counter {

    private void after(final NurseWorkWeek week) {
        this.count += SuccessionEvaluator.countConsecutiveWorkingDayViolations(week);
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
        this.count -= SuccessionEvaluator.countConsecutiveWorkingDayViolations(week);
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
        return tracker.getConsecutiveWorkingDayViolationsCount();
    }

    @Override
    public void store(final ScoreKeeper tracker) {
        tracker.setConsecutiveWorkingDayViolationsCount(this.count);
    }

}

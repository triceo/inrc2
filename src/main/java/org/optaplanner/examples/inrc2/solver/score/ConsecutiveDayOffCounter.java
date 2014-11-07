package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;

public class ConsecutiveDayOffCounter extends Counter {

    @Override
    public void afterAdded(final Shift shift, final NurseWorkWeek week) {
        this.count += SuccessionEvaluator.countConsecutiveDayOffViolations(week);
    }

    @Override
    public void afterRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.count += SuccessionEvaluator.countConsecutiveDayOffViolations(week);
    }

    @Override
    public void beforeAdded(final Shift shift, final NurseWorkWeek week) {
        this.count -= SuccessionEvaluator.countConsecutiveDayOffViolations(week);
    }

    @Override
    public void beforeRemoved(final Shift shift, final Nurse nurse, final NurseWorkWeek week) {
        this.count -= SuccessionEvaluator.countConsecutiveDayOffViolations(week);
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

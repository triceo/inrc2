package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.Shift;

abstract class Counter {

    protected int count;

    abstract void afterAdded(Shift shift, NurseWorkWeek week);

    abstract void afterRemoved(Shift shift, Nurse nurse, NurseWorkWeek week);

    abstract void beforeAdded(Shift shift, NurseWorkWeek week);

    abstract void beforeRemoved(Shift shift, Nurse nurse, NurseWorkWeek week);

    void reset(final ScoreKeeper tracker) {
        this.count = this.retrieve(tracker);
    }

    abstract int retrieve(ScoreKeeper tracker);

    abstract void store(ScoreKeeper tracker);

}
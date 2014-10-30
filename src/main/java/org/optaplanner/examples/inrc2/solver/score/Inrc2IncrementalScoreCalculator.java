package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator;
import org.optaplanner.examples.inrc2.domain.Roster;

public class Inrc2IncrementalScoreCalculator implements IncrementalScoreCalculator<Roster> {

    @Override
    public void afterEntityAdded(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterEntityRemoved(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterVariableChanged(final Object entity, final String variableName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeEntityAdded(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeEntityRemoved(final Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeVariableChanged(final Object entity, final String variableName) {
        // TODO Auto-generated method stub

    }

    @Override
    public Score calculateScore() {
        return BendableScore.valueOf(new int[]{0, 0}, new int[]{0, 0, 0});
    }

    @Override
    public void resetWorkingSolution(final Roster workingSolution) {
        // TODO Auto-generated method stub

    }

}

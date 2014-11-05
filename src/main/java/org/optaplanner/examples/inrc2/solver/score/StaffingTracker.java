package org.optaplanner.examples.inrc2.solver.score;

import org.optaplanner.examples.inrc2.domain.MandatoryShift;
import org.optaplanner.examples.inrc2.domain.Nurse;
import org.optaplanner.examples.inrc2.domain.OptionalShift;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.domain.Shift;

class StaffingTracker {

    private int nursesMissingFromMinimal;
    private int nursesMissingFromOptimal;

    public StaffingTracker(final Roster r) {
        this.nursesMissingFromMinimal = r.getTotalMinimalRequirements();
        this.nursesMissingFromOptimal = r.getTotalOptimalRequirements();
    }

    private void add(final MandatoryShift shift) {
        this.nursesMissingFromMinimal--;
        this.nursesMissingFromOptimal--;
    }

    private void add(final OptionalShift shift) {
        this.nursesMissingFromOptimal--;
    }

    public void add(final Shift shift) {
        if (shift.getNurse() == null) {
            return;
        }
        if (shift instanceof OptionalShift) {
            this.add((OptionalShift) shift);
        } else if (shift instanceof MandatoryShift) {
            this.add((MandatoryShift) shift);
        } else {
            throw new IllegalArgumentException("Unexpected shift: " + shift);
        }
    }

    public int countNursesMissingFromMinimal() {
        return this.nursesMissingFromMinimal;
    }

    public int countNursesMissingFromOptimal() {
        return this.nursesMissingFromOptimal;
    }

    private void remove(final MandatoryShift shift, final Nurse nurse) {
        this.nursesMissingFromMinimal++;
        this.nursesMissingFromOptimal++;
    }

    private void remove(final OptionalShift shift, final Nurse nurse) {
        this.nursesMissingFromOptimal++;
    }

    public void remove(final Shift shift) {
        this.remove(shift, shift.getNurse());
    }

    public void remove(final Shift shift, final Nurse nurse) {
        if (nurse == null) {
            return;
        }
        if (shift instanceof OptionalShift) {
            this.remove((OptionalShift) shift, nurse);
        } else if (shift instanceof MandatoryShift) {
            this.remove((MandatoryShift) shift, nurse);
        } else {
            throw new IllegalArgumentException("Unexpected shift: " + shift);
        }
    }

}

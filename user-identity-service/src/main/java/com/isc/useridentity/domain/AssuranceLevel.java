package com.isc.useridentity.domain;

public enum AssuranceLevel {
    IAL_0(0), IAL_1(1), IAL_2(2), IAL_3(3), IAL_4(4);
    private final int rank;
    AssuranceLevel(int rank) { this.rank = rank; }
    public int rank() { return rank; }
    public boolean satisfies(AssuranceLevel required) { return rank >= required.rank; }
}

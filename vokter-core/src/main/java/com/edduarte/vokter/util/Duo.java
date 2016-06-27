package com.edduarte.vokter.util;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class Duo<T> {

    private final T mA;

    private final T mB;


    public Duo(T a, T b) {
        this.mA = a;
        this.mB = b;
    }


    public T a() {
        return this.mA;
    }


    public T b() {
        return this.mB;
    }


    public String toString() {
        return "(" + this.a() + "," + this.b() + ")";
    }


    public boolean equals(Object that) {
        if (!(that instanceof Duo)) {
            return false;
        } else {
            Duo thatPair = (Duo) that;
            return this.mA.equals(thatPair.mA) && this.mB.equals(thatPair.mB);
        }
    }


    public int hashCode() {
        return 31 * this.mA.hashCode() + this.mB.hashCode();
    }
}


package cyclops.companion;

import cyclops.function.Group;

import java.math.BigInteger;


public interface Groups {

    /**
     * Combine two Integers by summing them
     */
    static Group<Integer> intSum =  Group.of(a->-a, Monoids.intSum);
    /**
     * Combine two Longs by summing them
     */
    static Group<Long> longSum =  Group.of(a->-a, Monoids.longSum);
    /**
     * Combine two Doubles by summing them
     */
    static Group<Double> doubleSum =  Group.of(a->-a, Monoids.doubleSum);
    /**
     * Combine two BigIngegers by summing them
     */
    static Group<BigInteger> bigIntSum =  Group.of(a->BigInteger.ZERO.subtract(a), Monoids.bigIntSum);


}

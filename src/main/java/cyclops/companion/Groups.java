package cyclops.companion;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.futurestream.SimpleReactStream;
import cyclops.async.Future;
import cyclops.async.SimpleReact;
import cyclops.collections.box.Mutable;
import cyclops.collections.immutable.*;
import cyclops.collections.mutable.*;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Xor;
import cyclops.function.Group;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import cyclops.typeclasses.NaturalTransformation;
import org.jooq.lambda.Seq;
import org.reactivestreams.Publisher;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;


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

package cyclops.companion;

import com.oath.cyclops.types.futurestream.SimpleReactStream;
import cyclops.function.Monoid;
import cyclops.futurestream.FutureStream;
import cyclops.futurestream.SimpleReact;

/**
 *
 * A static class with a large number of Monoids  or Combiners with identity elements.
 *
 * A Monoid is an Object that can be used to combine objects of the same type inconjunction with it's
 * identity element which leaves any element it is combined with unchanged.
 *
 * @author johnmcclean
 */
public interface FutureStreamMonoids {

    static <T> Monoid<SimpleReactStream<T>> firstOfSimpleReact() {
      return Monoid.of(new SimpleReact().of(), FutureStreamSemigroups.firstOfSimpleReact());
    }
    /**
     * @return Combination of two FutureStreams Streams b is appended to a
     */
    static <T> Monoid<FutureStream<T>> combineFutureStream() {
      return Monoid.of(FutureStream.builder().of(), FutureStreamSemigroups.combineFutureStream());
    }
}

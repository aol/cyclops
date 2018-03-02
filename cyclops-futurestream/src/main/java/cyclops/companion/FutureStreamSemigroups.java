package cyclops.companion;

import com.oath.cyclops.types.futurestream.EagerFutureStreamFunctions;
import com.oath.cyclops.types.futurestream.SimpleReactStream;
import cyclops.function.Semigroup;
import cyclops.futurestream.FutureStream;

/**
 *
 * A static class with a large number of SemigroupK  or Combiners.
 *
 * A semigroup is an Object that can be used to combine objects of the same type.
 *
 * Using raw Semigroups with container types
 * <pre>
 *     {@code
 *       Semigroup<Maybe<Integer>> m = Semigroups.combineZippables(Semigroups.intMax);
 *       Semigroup<ReactiveSeq<Integer>> m = Semigroups.combineZippables(Semigroups.intSum);
 *     }
 * </pre>
 *
 *
 *  @author johnmcclean
 */
public interface FutureStreamSemigroups {


    static <T> Semigroup<SimpleReactStream<T>> firstOfSimpleReact() {
      return (a, b) -> EagerFutureStreamFunctions.firstOf(a,b);
    }
    /**
     * @return Combination of two LazyFutureStreams Streams b is appended to a
     */
    static <T> Semigroup<FutureStream<T>> combineFutureStream() {
      return (a, b) -> a.appendStream(b);
    }


}

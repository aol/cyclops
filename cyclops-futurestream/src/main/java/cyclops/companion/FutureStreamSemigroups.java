package cyclops.companion;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.futurestream.EagerFutureStreamFunctions;
import com.oath.cyclops.types.futurestream.SimpleReactStream;
import cyclops.control.Future;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Either;
import cyclops.function.Semigroup;
import cyclops.futurestream.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.NaturalTransformation;
import com.oath.cyclops.types.persistent.PersistentCollection;
import cyclops.reactive.collections.immutable.*;
import cyclops.reactive.collections.mutable.*;
import org.reactivestreams.Publisher;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

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

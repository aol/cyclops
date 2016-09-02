package com.aol.cyclops.internal.monads;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.extensability.Comprehender;

public class AnyMonads {

    /**
     * Convert a Collection of Monads to a Monad with a List applying the supplied function in the process
     * 
     * <pre>
     * {@code 
       List<CompletableFuture<Integer>> futures = createFutures();
       AnyM<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
        </pre>
     * 
     * @param seq Collection of Monads
     * @param fn Function to apply 
     * @return Monad with a list
     */
    public <T, R> AnyMValue<ListX<R>> traverse(Collection<? extends AnyM<T>> seq, Function<? super T, ? extends R> fn) {
        if (seq.size() == 0)
            return AnyM.ofValue(Optional.empty());
        return new MonadWrapper<>(
                                  comprehender2(seq).of(1)).bind(in -> new MonadWrapper<>(
                                                                                          seq.stream()
                                                                                             .map(it -> it.unwrap())).flatten()
                                                                                                                     .bind((Function) fn)
                                                                                                                     .unwrap())
                                                           .anyMValue();
    }

    private <T> Comprehender<T> comprehender2(Collection<? extends AnyM<T>> seq) {
        return new ComprehenderSelector().selectComprehender(seq.iterator()
                                                                .next()
                                                                .unwrap()
                                                                .getClass());
    }

    /**
     * Convert a Stream of Monads to a Monad with a List applying the supplied function in the process
     * 
    <pre>{@code 
       Stream<CompletableFuture<Integer>> futures = createFutures();
       AnyM<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
        </pre>
     * 
     * @param seq Stream of Monads
     * @param fn Function to apply 
     * @return Monad with a list
     */
    public <T, R> AnyMValue<ListX<R>> traverse(Stream<? extends AnyM<T>> seq, Function<? super T, ? extends R> fn) {

        return traverse(seq.collect(Collectors.toList()), fn);
    }

    /**
     * Convert a Collection of Monads to a Monad with a List
     * 
     * <pre>
     * {@code
        List<CompletableFuture<Integer>> futures = createFutures();
        AnyM<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));
       //where AnyM wraps  CompletableFuture<List<Integer>>
      }</pre>
     * 
     * @param seq Collection of monads to convert
     * @return Monad with a List
     */
    public <T1> AnyMValue<ListX<T1>> sequence(Collection<? extends AnyM<T1>> seq) {
        if (seq.size() == 0)
            return AnyM.ofValue(ListX.empty());
        else
            return new MonadWrapper<>(
                                      comprehender(seq).of(1)).bind(in -> new MonadWrapper<>(
                                                                                             seq.stream()
                                                                                                .map(it -> it.unwrap())).flatten()
                                                                                                                        .unwrap())
                                                              .anyMValue();
    }

    private <T1> Comprehender comprehender(Collection<? extends AnyM<T1>> seq) {
        Object o = seq.iterator()
                      .next();
        return new ComprehenderSelector().selectComprehender(seq.iterator()
                                                                .next()
                                                                .unwrap()
                                                                .getClass());
    }

    /**
     * Convert a Stream of Monads to a Monad with a List
     * 
     * <pre>{@code
        Stream<CompletableFuture<Integer>> futures = createFutures();
        AnyM<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));
       //where AnyM wraps  CompletableFuture<List<Integer>>
      }</pre>
     * 
     * @param seq Stream of monads to convert
     * @return Monad with a List
     */
    public <T1> AnyMValue<ListX<T1>> sequence(Stream<? extends AnyM<T1>> seq) {
        return sequence(seq.collect(Collectors.toList()));
    }

}

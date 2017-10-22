package cyclops.data;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.anym.DataWitness.differenceList;
import cyclops.control.Trampoline;
import cyclops.function.Function0;
import cyclops.control.anym.Witness.supplier;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.free.Free;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DifferenceList<T> implements Folds<T>, Transformable<T>,Higher<differenceList,T> {

    private final Function<LazySeq<T>,Free<supplier, LazySeq<T>>> appending;


    public <R> DifferenceList<R> map(Function<? super T, ? extends R> fn){
        return new DifferenceList<>(l-> Free.done(run().map(fn)));
    }

    @Override
    public DifferenceList<T> peek(Consumer<? super T> c) {
        return (DifferenceList<T>)Transformable.super.peek(c);
    }

    @Override
    public <R> DifferenceList<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (DifferenceList<R>)Transformable.super.trampoline(mapper);
    }

    @Override
    public <R> DifferenceList<R> retry(Function<? super T, ? extends R> fn) {
        return (DifferenceList<R>)Transformable.super.retry(fn);
    }

    @Override
    public <R> DifferenceList<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (DifferenceList<R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }

    public <R> DifferenceList<R> flatMap(Function<? super T, ? extends DifferenceList<? extends R>> fn){
        return new DifferenceList<>(l-> Free.done(run().flatMap(fn.andThen(DifferenceList::run))));
    }
    public LazySeq<T> run(){
        return Function0.run(appending.apply(LazySeq.empty()));
    }
    public static <T> DifferenceList<T> of(LazySeq<T> list){
        return new DifferenceList<>(l-> Free.done(list.appendAll(l)));
    }
    public static <T> DifferenceList<T> of(T... values){
        return  of(LazySeq.of(values));
    }
    public static <T> DifferenceList<T> empty(){
        return new DifferenceList<>(l-> Free.done(l));
    }
    public DifferenceList<T> prepend(DifferenceList<T> prepend) {
        return prepend.append(this);
    }
    public DifferenceList<T> append(DifferenceList<T> append) {
        Function<LazySeq<T>, Free<supplier, LazySeq<T>>> appending2 = append.appending;

        return new DifferenceList<T>(l-> appending2.apply(l).flatMap(l2->{
                                    Function0.SupplierKind<Free<supplier, LazySeq<T>>> s = ()->appending.apply(l2);
                                    Free<supplier, LazySeq<T>> x = Function0.suspend(s);
                                    return x;
                                     }));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(run());
    }

    @Override
    public Iterator<T> iterator() {
        return run().iterator();
    }
}

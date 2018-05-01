package cyclops.monads.transformers.rx2;


import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.companion.rx2.Functions;
import cyclops.companion.rx2.Singles;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.data.tuple.Tuple;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.Single;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.*;


public final class SingleT<W extends WitnessType<W>,T> implements To<SingleT<W,T>>,  Transformable<T>, Filters<T>, Folds<T> {

    private final AnyM<W,Single<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().map(Single::blockingGet);
    }



    /**
     * @return The wrapped AnyM
     */
    public AnyM<W,Single<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Single<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private SingleT(final AnyM<W,Single<T>> run) {
        this.run = run;
    }



    @Override
    public SingleT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                .filter( f->f.blockingGet()._2() )
                .map( f->f.map(in->in._1())));
    }


    @Override
    public SingleT<W,T> peek(final Consumer<? super T> peek) {
        return map(e->{
            peek.accept(e);
            return e;
        });

    }


    @Override
    public <B> SingleT<W,B> map(final Function<? super T, ? extends B> f) {
        return new SingleT<W,B>(
                run.map(o -> o.map(Functions.rxFunction(f))));
    }



    public <B> SingleT<W,B> flatMapT(final Function<? super T, SingleT<W,B>> f) {
        SingleT<W, B> r = of(run.map(future -> future.flatMap(a -> {
            Single<B> m = f.apply(a).run.stream()
                    .toList()
                    .get(0);
            return m;
        })));
        return r;
    }

    private static <W extends WitnessType<W>,B> AnyM<W,Single<B>> narrow(final AnyM<W,Single<? extends B>> run) {
        return (AnyM) run;
    }


    public <B> SingleT<W,B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        final AnyM<W,Single<? extends B>> mapped = run.map(o -> o.flatMap(in->{
            MonadicValue<? extends B> r = f.apply(in);
            Single<B> r2 = Singles.fromValue((MonadicValue<B>) r);
            return r2;
        }));
        return of(narrow(mapped));

    }


    public static <W extends WitnessType<W>,U, R> Function<SingleT<W,U>, SingleT<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }


    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<SingleT<W,U1>, SingleT<W,U2>, SingleT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }


    public static <W extends WitnessType<W>,A> SingleT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Single::just));
    }


    public static <W extends WitnessType<W>,A> SingleT<W,A> of(final AnyM<W,Single<A>> monads) {
        return new SingleT<>(
                monads);
    }


    @Override
    public String toString() {
        return String.format("SingleT[%s]", run.unwrap().toString());
    }




    public <R> SingleT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                .map(i -> Single.just(i)));
    }



    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof SingleT) {
            return run.equals(((SingleT) o).run);
        }
        return false;
    }



    public <T2, R1, R2, R3, R> SingleT<W,R> forEach4M(Function<? super T, ? extends SingleT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends SingleT<W,R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends SingleT<W,R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> SingleT<W,R> forEach4M(Function<? super T, ? extends SingleT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends SingleT<W,R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends SingleT<W,R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> SingleT<W,R> forEach3M(Function<? super T, ? extends SingleT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends SingleT<W,R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> SingleT<W,R> forEach3M(Function<? super T, ? extends SingleT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends SingleT<W,R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> SingleT<W,R> forEach2M(Function<? super T, ? extends SingleT<W,R1>> value1,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> SingleT<W,R> forEach2M(Function<? super T, ? extends SingleT<W,R1>> value1,
                                          BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .filter(in2->filterFunction.apply(in,in2))
                .map(in2->yieldingFunction.apply(in,in2)));
    }

    public String mkString(){
        return toString();
    }



    @Override
    public <U> SingleT<W,U> ofType(Class<? extends U> type) {
        return (SingleT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public SingleT<W,T> filterNot(Predicate<? super T> predicate) {
        return (SingleT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public SingleT<W,T> notNull() {
        return (SingleT<W,T>)Filters.super.notNull();
    }

     public Option<T> get(){
    return stream().takeOne();
  }

     public T orElse(T value){
    return stream().findAny().orElse(value);
  }
     public T orElseGet(Supplier<? super T> s){
    return stream().findAny().orElseGet((Supplier<T>)s);
  }


     @Override
     public <R> SingleT<W,R> retry(Function<? super T, ? extends R> fn) {
        return (SingleT<W,R>)Transformable.super.retry(fn);
     }

     @Override
     public <R> SingleT<W,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (SingleT<W,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
     }
}

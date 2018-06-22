package cyclops.monads.transformers;

import com.oath.cyclops.anym.transformers.ValueTransformer;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.function.*;


public final class OptionT<W extends WitnessType<W>,T> extends ValueTransformer<W,T>
                                                       implements To<OptionT<W,T>>,
                                                                  Transformable<T>,
                                                                  Filters<T> {

    private final AnyM<W,Option<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

                                                                         @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMap(Option::stream);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Option<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Option<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private OptionT(final AnyM<W,Option<T>> run) {
        this.run = run;
    }


    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> OptionT<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run;
    }

    @Override
    public OptionT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.fold(t->t._2(),()->false) )
                     .map( f->f.map(in->in._1())));
    }


    @Override
    public OptionT<W,T> peek(final Consumer<? super T> peek) {
        return map(e->{
            peek.accept(e);
            return e;
        });
    }


    @Override
    public <B> OptionT<W,B> map(final Function<? super T, ? extends B> f) {
        return new OptionT<W,B>(
                                  run.map(o -> o.map(f)));
    }


    public <B> OptionT<W,B> flatMapT(final Function<? super T, OptionT<W,B>> f) {
        return of(run.map(o -> o.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,B> AnyM<W,Option<B>> narrow(final AnyM<W,Option<? extends B>> run) {
        return (AnyM) run;
    }


    public <B> OptionT<W,B> flatMap(final Function<? super T, ? extends Option<? extends B>> f) {

        final AnyM<W,Option<? extends B>> mapped = run.map(o -> o.flatMap(f));
        return of(narrow(mapped));

    }


    public static <W extends WitnessType<W>,U, R> Function<OptionT<W,U>, OptionT<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }


    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<OptionT<W,U1>, OptionT<W,U2>, OptionT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }


    public static <W extends WitnessType<W>,A> OptionT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Option::ofNullable));
    }


    public static <W extends WitnessType<W>,A> OptionT<W,A> of(final AnyM<W,Option<A>> monads) {
        return new OptionT<>(
                                 monads);
    }


    @Override
    public String toString() {
        return String.format("OptionT[%s]", run.unwrap().toString());
    }




    public <R> OptionT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Option.ofNullable(i)));
    }

    @Override
    public <R> OptionT<W,R> unit(final R value) {
        return of(run.unit(Option.ofNullable(value)));
    }

    @Override
    public <R> OptionT<W,R> empty() {
        return of(run.unit(Option.<R>none()));
    }




    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof OptionT) {
            return run.equals(((OptionT) o).run);
        }
        return false;
    }



    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn, T alt) {

        return super.iterate(fn,alt);
    }


    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> generate(T alt) {

        return super.generate(alt);
    }

    @Override
    public <T2, R> OptionT<W, R> zip(Iterable<? extends T2> iterable,
                                     BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (OptionT<W, R>)super.zip(iterable, fn);
    }


    @Override
    public <T2, R> OptionT<W, R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {

        return (OptionT<W, R>)super.zip(fn, publisher);
    }



    @Override
    public <U> OptionT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (OptionT)super.zip(other);
    }


    @Override
    public <T2, R1, R2, R3, R> OptionT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (OptionT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }


    @Override
    public <T2, R1, R2, R3, R> OptionT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (OptionT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }


    @Override
    public <T2, R1, R2, R> OptionT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (OptionT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }


    @Override
    public <T2, R1, R2, R> OptionT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (OptionT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }


    @Override
    public <R1, R> OptionT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (OptionT<W, R>)super.forEach2(value1, yieldingFunction);
    }


    @Override
    public <R1, R> OptionT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                          BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (OptionT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    @Override
    public <R> OptionT<W, R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (OptionT<W, R>)super.concatMap(mapper);
    }


    @Override
    public <R> OptionT<W, R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {

        return (OptionT<W, R>)super.mergeMap(mapper);
    }
    public <T2, R1, R2, R3, R> OptionT<W,R> forEach4M(Function<? super T, ? extends OptionT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends OptionT<W,R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends OptionT<W,R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> OptionT<W,R> forEach4M(Function<? super T, ? extends OptionT<W,R1>> value1,
                                                      BiFunction<? super T, ? super R1, ? extends OptionT<W,R2>> value2,
                                                      Function3<? super T, ? super R1, ? super R2, ? extends OptionT<W,R3>> value3,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> OptionT<W,R> forEach3M(Function<? super T, ? extends OptionT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends OptionT<W,R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> OptionT<W,R> forEach3M(Function<? super T, ? extends OptionT<W,R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends OptionT<W,R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> OptionT<W,R> forEach2M(Function<? super T, ? extends OptionT<W,R1>> value1,
                                          BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> OptionT<W,R> forEach2M(Function<? super T, ? extends OptionT<W,R1>> value1,
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
    public <U> OptionT<W,U> ofType(Class<? extends U> type) {
        return (OptionT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public OptionT<W,T> filterNot(Predicate<? super T> predicate) {
        return (OptionT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public OptionT<W,T> notNull() {
        return (OptionT<W,T>)Filters.super.notNull();
    }


  @Override
    public <U> OptionT<W,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (OptionT)super.zipWithPublisher(other);
    }

    @Override
    public <S, U> OptionT<W,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (OptionT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> OptionT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (OptionT<W,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> OptionT<W,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (OptionT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> OptionT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (OptionT<W,R>)super.zip4(second,third,fourth,fn);
    }
}

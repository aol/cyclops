package cyclops.monads.transformers;

import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.anym.transformers.ValueTransformer;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Maybe;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.function.*;


public final class MaybeT<W extends WitnessType<W>,T> extends ValueTransformer<W,T>
                                                       implements To<MaybeT<W,T>>,
                                                                  Transformable<T>,
                                                                  Filters<T> {

    private final AnyM<W,Maybe<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

                                                                         @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMap(Maybe::stream);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Maybe<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Maybe<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private MaybeT(final AnyM<W,Maybe<T>> run) {
        this.run = run;
    }


    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> MaybeT<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run;
    }

    @Override
    public MaybeT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.fold(t->t._2(),()->false) )
                     .map( f->f.map(in->in._1())));
    }


    @Override
    public MaybeT<W,T> peek(final Consumer<? super T> peek) {
        return map(e->{
            peek.accept(e);
            return e;
        });
    }


    @Override
    public <B> MaybeT<W,B> map(final Function<? super T, ? extends B> f) {
        return new MaybeT<W,B>(
                                  run.map(o -> o.map(f)));
    }


    public <B> MaybeT<W,B> flatMapT(final Function<? super T, MaybeT<W,B>> f) {
        return of(run.map(Maybe -> Maybe.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,B> AnyM<W,Maybe<B>> narrow(final AnyM<W,Maybe<? extends B>> run) {
        return (AnyM) run;
    }


    public <B> MaybeT<W,B> flatMap(final Function<? super T, ? extends Maybe<? extends B>> f) {

        final AnyM<W,Maybe<? extends B>> mapped = run.map(o -> o.flatMap(f));
        return of(narrow(mapped));

    }


    public static <W extends WitnessType<W>,U, R> Function<MaybeT<W,U>, MaybeT<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }


    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<MaybeT<W,U1>, MaybeT<W,U2>, MaybeT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }


    public static <W extends WitnessType<W>,A> MaybeT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Maybe::ofNullable));
    }


    public static <W extends WitnessType<W>,A> MaybeT<W,A> of(final AnyM<W,Maybe<A>> monads) {
        return new MaybeT<>(
                                 monads);
    }


    @Override
    public String toString() {
        return String.format("MaybeT[%s]", run.unwrap().toString());
    }




    public <R> MaybeT<W,R> unitIterable(final Iterable<R> it) {
        return of(run.unitIterable(it)
                     .map(i -> Maybe.ofNullable(i)));
    }

    @Override
    public <R> MaybeT<W,R> unit(final R value) {
        return of(run.unit(Maybe.ofNullable(value)));
    }

    @Override
    public <R> MaybeT<W,R> empty() {
        return of(run.unit(Maybe.<R>nothing()));
    }




    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof MaybeT) {
            return run.equals(((MaybeT) o).run);
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
    public <T2, R> MaybeT<W, R> zip(Iterable<? extends T2> iterable,
                                    BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (MaybeT<W, R>)super.zip(iterable, fn);
    }


    @Override
    public <T2, R> MaybeT<W, R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {

        return (MaybeT<W, R>)super.zip(fn, publisher);
    }



    @Override
    public <U> MaybeT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (MaybeT)super.zip(other);
    }


    @Override
    public <T2, R1, R2, R3, R> MaybeT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (MaybeT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }


    @Override
    public <T2, R1, R2, R3, R> MaybeT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (MaybeT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }


    @Override
    public <T2, R1, R2, R> MaybeT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (MaybeT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }


    @Override
    public <T2, R1, R2, R> MaybeT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (MaybeT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }


    @Override
    public <R1, R> MaybeT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                         BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (MaybeT<W, R>)super.forEach2(value1, yieldingFunction);
    }


    @Override
    public <R1, R> MaybeT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                         BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                         BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (MaybeT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    @Override
    public <R> MaybeT<W, R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (MaybeT<W, R>)super.concatMap(mapper);
    }


    @Override
    public <R> MaybeT<W, R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {

        return (MaybeT<W, R>)super.mergeMap(mapper);
    }
    public <T2, R1, R2, R3, R> MaybeT<W,R> forEach4M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MaybeT<W,R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> MaybeT<W,R> forEach4M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                     BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends MaybeT<W,R3>> value3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> MaybeT<W,R> forEach3M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> MaybeT<W,R> forEach3M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends MaybeT<W,R2>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> MaybeT<W,R> forEach2M(Function<? super T, ? extends MaybeT<W,R1>> value1,
                                         BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> MaybeT<W,R> forEach2M(Function<? super T, ? extends MaybeT<W,R1>> value1,
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
    public <U> MaybeT<W,U> ofType(Class<? extends U> type) {
        return (MaybeT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public MaybeT<W,T> filterNot(Predicate<? super T> predicate) {
        return (MaybeT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public MaybeT<W,T> notNull() {
        return (MaybeT<W,T>)Filters.super.notNull();
    }


  @Override
    public <U> MaybeT<W,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (MaybeT)super.zipWithPublisher(other);
    }

    @Override
    public <S, U> MaybeT<W,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (MaybeT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> MaybeT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (MaybeT<W,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> MaybeT<W,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (MaybeT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> MaybeT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (MaybeT<W,R>)super.zip4(second,third,fourth,fn);
    }
}

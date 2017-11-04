package cyclops.monads.transformers;

import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.Zippable;
import com.oath.anym.transformers.ValueTransformer;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Eval;
import cyclops.control.Trampoline;
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
import java.util.stream.Stream;

/**
* Monad Transformer for Eval's

 *
 * EvalT allows the deeply wrapped Eval to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Eval(s)
 */
public final class EvalT<W extends WitnessType<W>,T> extends ValueTransformer<W,T>
                                                       implements To<EvalT<W,T>>,
        Transformable<T>,
  Filters<T> {

    private final AnyM<W,Eval<T>> run;

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

     @Override
    public ReactiveSeq<T> stream() {
        return run.stream().map(Eval::get);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Eval<T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Eval<T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private EvalT(final AnyM<W,Eval<T>> run) {
        this.run = run;
    }


    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> EvalT<W,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends MonadicValue<T>> transformerStream() {

        return run;
    }

    @Override
    public EvalT<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.get()._2() )
                     .map( f->f.map(in->in._1())));
    }

    /**
     * Peek at the current value of the Eval
     * <pre>
     * {@code
     *    EvalWT.of(AnyM.fromStream(Arrays.asEvalW(10))
     *             .peek(System.out::println);
     *
     *     //prints 10
     * }
     * </pre>
     *
     * @param peek  Consumer to accept current value of Eval
     * @return EvalWT with peek call
     */
    @Override
    public EvalT<W,T> peek(final Consumer<? super T> peek) {
        return map(e->{
            peek.accept(e);
            return e;
        });
    }

    /**
     * Map the wrapped Eval
     *
     * <pre>
     * {@code
     *  EvalWT.of(AnyM.fromStream(Arrays.asEvalW(10))
     *             .map(t->t=t+1);
     *
     *
     *  //EvalWT<AnyMSeq<Stream<Eval[11]>>>
     * }
     * </pre>
     *
     * @param f Mapping function for the wrapped Eval
     * @return EvalWT that applies the transform function to the wrapped Eval
     */
    @Override
    public <B> EvalT<W,B> map(final Function<? super T, ? extends B> f) {
        return new EvalT<W,B>(
                                  run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Eval
      * <pre>
     * {@code
     *  EvalWT.of(AnyM.fromStream(Arrays.asEvalW(10))
     *             .flatMap(t->Eval.completedEval(20));
     *
     *
     *  //EvalWT<AnyMSeq<Stream<Eval[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return EvalWT that applies the flatMap function to the wrapped Eval
     */

    public <B> EvalT<W,B> flatMapT(final Function<? super T, EvalT<W,B>> f) {
        return of(run.map(Eval -> Eval.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,B> AnyM<W,Eval<B>> narrow(final AnyM<W,Eval<? extends B>> run) {
        return (AnyM) run;
    }


    public <B> EvalT<W,B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        final AnyM<W,Eval<? extends B>> mapped = run.map(o -> o.flatMap(f));
        return of(narrow(mapped));

    }

    /**
     * Lift a function into one that accepts and returns an EvalWT
     * This allows multiple monad types to add functionality to existing function and methods
     *
     * e.g. to add list handling  / iteration (via Eval) and iteration (via Stream) to an existing function
     * <pre>
     * {@code
        Function<Integer,Integer> add2 = i -> i+2;
    	Function<EvalWT<Integer>, EvalWT<Integer>> optTAdd2 = EvalWT.lift(add2);

    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.fromStream(withNulls);
    	AnyMSeq<Eval<Integer>> streamOpt = reactiveStream.map(Eval::completedEval);
    	List<Integer> results = optTAdd2.applyHKT(EvalWT.of(streamOpt))
    									.unwrap()
    									.<Stream<Eval<Integer>>>unwrap()
    									.map(Eval::join)
    									.collect(CyclopsCollectors.toList());


    	//Eval.completedEval(List[3,4]);
     *
     *
     * }</pre>
     *
     *
     * @param fn Function to enhance with functionality from Eval and another monad type
     * @return Function that accepts and returns an EvalWT
     */
    public static <W extends WitnessType<W>,U, R> Function<EvalT<W,U>, EvalT<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  EvalWTs
     * This allows multiple monad types to add functionality to existing function and methods
     *
     * e.g. to add list handling / iteration (via Eval), iteration (via Stream)  and asynchronous execution (Eval)
     * to an existing function
     *
     * <pre>
     * {@code
    	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<EvalWT<Integer>,EvalWT<Integer>,EvalWT<Integer>> optTAdd2 = EvalWT.lift2(add);

    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Eval<Integer>> streamOpt = reactiveStream.map(Eval::completedEval);

    	Eval<Eval<Integer>> two = Eval.completedEval(Eval.completedEval(2));
    	AnyMSeq<Eval<Integer>> Eval=  AnyM.fromEvalW(two);
    	List<Integer> results = optTAdd2.applyHKT(EvalWT.of(streamOpt),EvalWT.of(Eval))
    									.unwrap()
    									.<Stream<Eval<Integer>>>unwrap()
    									.map(Eval::join)
    									.collect(CyclopsCollectors.toList());

    		//Eval.completedEval(List[3,4,5]);
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Eval and another monad type
     * @return Function that accepts and returns an EvalWT
     */
    public static <W extends WitnessType<W>, U1,  U2, R> BiFunction<EvalT<W,U1>, EvalT<W,U2>, EvalT<W,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an EvalWT from an AnyM that contains a monad type that contains type other than Eval
     * The values in the underlying monad will be mapped to Eval<A>
     *
     * @param anyM AnyM that doesn't contain a monad wrapping an Eval
     * @return EvalWT
     */
    public static <W extends WitnessType<W>,A> EvalT<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Eval::now));
    }

    /**
     * Construct an EvalWT from an AnyM that wraps a monad containing  EvalWs
     *
     * @param monads AnyM that contains a monad wrapping an Eval
     * @return EvalWT
     */
    public static <W extends WitnessType<W>,A> EvalT<W,A> of(final AnyM<W,Eval<A>> monads) {
        return new EvalT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("EvalT[%s]", run.unwrap().toString());
    }




    public <R> EvalT<W,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Eval.now(i)));
    }

    @Override
    public <R> EvalT<W,R> unit(final R value) {
        return of(run.unit(Eval.now(value)));
    }

    @Override
    public <R> EvalT<W,R> empty() {
        return of(run.unit(Eval.<R>eval()));
    }




    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof EvalT) {
            return run.equals(((EvalT) o).run);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(com.oath.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> EvalT<W,R> combine(Value<? extends T2> app,
                                      BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (EvalT<W,R>)super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#combine(java.util.function.BinaryOperator, com.oath.cyclops.types.Combiner)
     */
    @Override
    public EvalT<W, T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {

        return (EvalT<W, T>)super.zip(combiner, app);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#iterate(java.util.function.UnaryOperator)
     */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> iterate(UnaryOperator<T> fn, T alt) {

        return super.iterate(fn,alt);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#generate()
     */
    @Override
    public AnyM<W, ? extends ReactiveSeq<T>> generate(T alt) {

        return super.generate(alt);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> EvalT<W, R> zip(Iterable<? extends T2> iterable,
                                   BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (EvalT<W, R>)super.zip(iterable, fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> EvalT<W, R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (EvalT<W, R>)super.zipP(publisher,fn);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.util.stream.Stream)
     */
    @Override
    public <U> EvalT<W, Tuple2<T, U>> zipS(Stream<? extends U> other) {

        return (EvalT)super.zipS(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#zip(java.lang.Iterable)
     */
    @Override
    public <U> EvalT<W, Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (EvalT)super.zip(other);
    }


    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> EvalT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (EvalT<W, R>)super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> EvalT<W, R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (EvalT<W, R>)super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> EvalT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (EvalT<W, R>)super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> EvalT<W, R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (EvalT<W, R>)super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> EvalT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (EvalT<W, R>)super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> EvalT<W, R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                        BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (EvalT<W, R>)super.forEach2(value1, filterFunction, yieldingFunction);
    }



    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapI(java.util.function.Function)
     */
    @Override
    public <R> EvalT<W, R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (EvalT<W, R>)super.flatMapIterable(mapper);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> EvalT<W, R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper) {

        return (EvalT<W, R>)super.flatMapPublisher(mapper);
    }
    public <T2, R1, R2, R3, R> EvalT<W,R> forEach4M(Function<? super T, ? extends EvalT<W,R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends EvalT<W,R2>> value2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends EvalT<W,R3>> value3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }
    public <T2, R1, R2, R3, R> EvalT<W,R> forEach4M(Function<? super T, ? extends EvalT<W,R1>> value1,
                                                    BiFunction<? super T, ? super R1, ? extends EvalT<W,R2>> value2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends EvalT<W,R3>> value3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                    .flatMapT(in2-> value2.apply(in,in2)
                            .flatMapT(in3->value3.apply(in,in2,in3)
                                                 .filter(in4->filterFunction.apply(in,in2,in3,in4))
                                                 .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }

    public <T2, R1, R2, R> EvalT<W,R> forEach3M(Function<? super T, ? extends EvalT<W,R1>> value1,
                                                BiFunction<? super T, ? super R1, ? extends EvalT<W,R2>> value2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                                                 .map(in3->yieldingFunction.apply(in,in2,in3))));

    }

    public <T2, R1, R2, R> EvalT<W,R> forEach3M(Function<? super T, ? extends EvalT<W,R1>> value1,
                                                BiFunction<? super T, ? super R1, ? extends EvalT<W,R2>> value2,
                                                Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2).filter(in3->filterFunction.apply(in,in2,in3))
                                                                                     .map(in3->yieldingFunction.apply(in,in2,in3))));

    }
    public <R1, R> EvalT<W,R> forEach2M(Function<? super T, ? extends EvalT<W,R1>> value1,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                    .map(in2->yieldingFunction.apply(in,in2)));
    }

    public <R1, R> EvalT<W,R> forEach2M(Function<? super T, ? extends EvalT<W,R1>> value1,
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
    public <U> EvalT<W,U> ofType(Class<? extends U> type) {
        return (EvalT<W,U>)Filters.super.ofType(type);
    }

    @Override
    public EvalT<W,T> filterNot(Predicate<? super T> predicate) {
        return (EvalT<W,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public EvalT<W,T> notNull() {
        return (EvalT<W,T>)Filters.super.notNull();
    }

    @Override
    public <R> EvalT<W,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (EvalT<W,R>)super.zipWith(fn);
    }

    @Override
    public <R> EvalT<W,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (EvalT<W,R>)super.zipWithS(fn);
    }

    @Override
    public <R> EvalT<W,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (EvalT<W,R>)super.zipWithP(fn);
    }

    @Override
    public <R> EvalT<W,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (EvalT<W,R>)super.trampoline(mapper);
    }

    @Override
    public <U, R> EvalT<W,R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (EvalT<W,R>)super.zipS(other,zipper);
    }

    @Override
    public <U> EvalT<W,Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (EvalT)super.zipP(other);
    }

    @Override
    public <S, U> EvalT<W,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (EvalT)super.zip3(second,third);
    }

    @Override
    public <S, U, R> EvalT<W,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (EvalT<W,R>)super.zip3(second,third, fn3);
    }

    @Override
    public <T2, T3, T4> EvalT<W,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (EvalT)super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> EvalT<W,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (EvalT<W,R>)super.zip4(second,third,fourth,fn);
    }
}

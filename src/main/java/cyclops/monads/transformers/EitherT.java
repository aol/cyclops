package cyclops.monads.transformers;

import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.anyM.transformers.NonEmptyTransformer;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.lazy.Trampoline;
import cyclops.control.Either;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import cyclops.data.tuple.Tuple;

import java.util.Iterator;
import java.util.function.*;

/**
* Monad Transformer for Xor's

 * 
 * MaybeT allows the deeply wrapped Maybe to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Maybe(s)
 */
public final class EitherT<W extends WitnessType<W>, ST,T> extends NonEmptyTransformer<W, T> implements  To<EitherT<W, ST,T>>,
                                                                                                    Transformable<T>,
                                                                                                     Filters<T> {

    private final AnyM<W,Either<ST,T>> run;


    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMap(Either::stream);
    }



    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyM<W,Either<ST,T>> unwrap() {
        return run;
    }

    public <R> R unwrapTo(Function<? super AnyM<W,Either<ST,T>>, ? extends R> fn) {
        return unwrap().to(fn);
    }

    private EitherT(final AnyM<W,Either<ST,T>> run) {
        this.run = run;
    }

    
    @Override @Deprecated (/*DO NOT USE INTERNAL USE ONLY*/)
    protected <R> EitherT<W,ST,R> unitAnyM(AnyM<W,? super MonadicValue<R>> traversable) {

        return of((AnyM) traversable);
    }

    @Override
    public AnyM<W,? extends Either<ST,T>> transformerStream() {

        return run;
    }

    @Override
    public EitherT<W,ST,T> filter(final Predicate<? super T> test) {
        return of(run.map(f->f.map(in->Tuple.tuple(in,test.test(in))))
                     .filter( f->f.visit(t->t._2(),()->false) )
                     .map( f->f.map(in->in._1())));
    }

    /**
     * Peek at the current value of the Maybe
     * <pre>
     * {@code 
     *    MaybeWT.of(AnyM.fromStream(Arrays.asMaybeW(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Maybe
     * @return MaybeWT with peek call
     */
    @Override
    public EitherT<W,ST,T> peek(final Consumer<? super T> peek) {
        return map(e->{
            peek.accept(e);
            return e;
        });
    }

    /**
     * Map the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *  MaybeWT.of(AnyM.fromStream(Arrays.asMaybeW(10))
     *             .transform(t->t=t+1);
     *  
     *  
     *  //MaybeWT<AnyMSeq<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Maybe
     * @return MaybeWT that applies the transform function to the wrapped Maybe
     */
    @Override
    public <B> EitherT<W,ST,B> map(final Function<? super T, ? extends B> f) {
        return new EitherT<W,ST,B>(
                                  run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Maybe
      * <pre>
     * {@code 
     *  MaybeWT.of(AnyM.fromStream(Arrays.asMaybeW(10))
     *             .flatMap(t->Maybe.completedMaybe(20));
     *  
     *  
     *  //MaybeWT<AnyMSeq<Stream<Maybe[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return MaybeWT that applies the flatMap function to the wrapped Maybe
     */

    public <B> EitherT<W,ST,B> flatMapT(final Function<? super T, EitherT<W,ST,B>> f) {
        return of(run.map(Maybe -> Maybe.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <W extends WitnessType<W>,ST,B> AnyM<W,Either<ST,B>> narrow(final AnyM<W,Either<ST,? extends B>> run) {
        return (AnyM) run;
    }


    public <B> EitherT<W,ST,B> flatMap(final Function<? super T, ? extends Either<ST,? extends B>> f) {

        final AnyM<W,Either<ST,? extends B>> mapped = run.map(o -> o.flatMap(f));
        return of(narrow(mapped));

    }

    /**
     * Lift a function into one that accepts and returns an MaybeWT
     * This allows multiple monad types to add functionality to existing function and methods
     * 
     * e.g. to add list handling  / iteration (via Maybe) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
        Function<Integer,Integer> add2 = i -> i+2;
    	Function<MaybeWT<Integer>, MaybeWT<Integer>> optTAdd2 = MaybeWT.lift(add2);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.fromStream(withNulls);
    	AnyMSeq<Maybe<Integer>> streamOpt = reactiveStream.transform(Maybe::completedMaybe);
    	List<Integer> results = optTAdd2.applyHKT(MaybeWT.of(streamOpt))
    									.unwrap()
    									.<Stream<Maybe<Integer>>>unwrap()
    									.transform(Maybe::join)
    									.collect(CyclopsCollectors.toList());
    	
    	
    	//Maybe.completedMaybe(List[3,4]);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Maybe and another monad type
     * @return Function that accepts and returns an MaybeWT
     */
    public static <W extends WitnessType<W>,U,ST, R> Function<EitherT<W,ST,U>, EitherT<W,ST,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  MaybeWTs
     * This allows multiple monad types to add functionality to existing function and methods
     * 
     * e.g. to add list handling / iteration (via Maybe), iteration (via Stream)  and asynchronous execution (Maybe)
     * to an existing function
     * 
     * <pre>
     * {@code 
    	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<MaybeWT<Integer>,MaybeWT<Integer>,MaybeWT<Integer>> optTAdd2 = MaybeWT.lift2(add);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> reactiveStream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Maybe<Integer>> streamOpt = reactiveStream.transform(Maybe::completedMaybe);
    	
    	Maybe<Maybe<Integer>> two = Maybe.completedMaybe(Maybe.completedMaybe(2));
    	AnyMSeq<Maybe<Integer>> Maybe=  AnyM.fromMaybeW(two);
    	List<Integer> results = optTAdd2.applyHKT(MaybeWT.of(streamOpt),MaybeWT.of(Maybe))
    									.unwrap()
    									.<Stream<Maybe<Integer>>>unwrap()
    									.transform(Maybe::join)
    									.collect(CyclopsCollectors.toList());
    									
    		//Maybe.completedMaybe(List[3,4,5]);
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Maybe and another monad type
     * @return Function that accepts and returns an MaybeWT
     */
    public static <W extends WitnessType<W>, ST,U1,  U2, R> BiFunction<EitherT<W,ST,U1>, EitherT<W,ST,U2>, EitherT<W,ST,R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an MaybeWT from an AnyM that contains a monad type that contains type other than Maybe
     * The values in the underlying monad will be mapped to Maybe<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Maybe
     * @return MaybeWT
     */
    public static <W extends WitnessType<W>,ST,A> EitherT<W,ST,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Either::right));
    }

    /**
     * Construct an MaybeWT from an AnyM that wraps a monad containing  MaybeWs
     * 
     * @param monads AnyM that contains a monad wrapping an Maybe
     * @return MaybeWT
     */
    public static <W extends WitnessType<W>,ST,A> EitherT<W,ST,A> of(final AnyM<W,Either<ST,A>> monads) {
        return new EitherT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("XorT[%s]", run.unwrap().toString());
    }

    


    public <R> EitherT<W,ST,R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Either.right(i)));
    }

    @Override
    public <R> EitherT<W,ST,R> unit(final R value) {
        return of(run.unit(Either.right(value)));
    }



    

   
    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof EitherT) {
            return run.equals(((EitherT) o).run);
        }
        return false;
    }






    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#iterate(java.util.function.UnaryOperator)
     */
    @Override
    public StreamT<W,T> iterate(UnaryOperator<T> fn, T alt) {
        
        return super.iterate(fn,alt);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.ValueTransformer#generate()
     */
    @Override
    public StreamT<W,T> generate(T alt) {
        
        return super.generate(alt);
    }





    public String mkString(){
        return toString();
    }

    @Override
    public <U> EitherT<W,ST,U> cast(Class<? extends U> type) {
        return (EitherT<W,ST,U>)super.cast(type);
    }

    @Override
    public <U> EitherT<W,ST,U> ofType(Class<? extends U> type) {
        return (EitherT<W,ST,U>)Filters.super.ofType(type);
    }

    @Override
    public EitherT<W,ST,T> filterNot(Predicate<? super T> predicate) {
        return (EitherT<W,ST,T>)Filters.super.filterNot(predicate);
    }

    @Override
    public EitherT<W,ST,T> notNull() {
        return (EitherT<W,ST,T>)Filters.super.notNull();
    }


    @Override
    public <R> EitherT<W,ST,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (EitherT<W,ST,R>)super.trampoline(mapper);
    }



}
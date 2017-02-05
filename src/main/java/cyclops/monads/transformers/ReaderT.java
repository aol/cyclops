package cyclops.monads.transformers;

import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.anyM.transformers.ValueTransformer;
import cyclops.async.Future;
import cyclops.control.Trampoline;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Reader;
import cyclops.monads.AnyM;
import cyclops.monads.AnyM2;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.function.*;
import java.util.stream.Stream;

/**
* Monad Transformer for Future's nested within another monadic type

 * 
 * FutureT allows the deeply wrapped Future to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Future(s)
 */
public final class ReaderT<W extends WitnessType<W>,T,R>  implements  To<ReaderT<W,T,R>>,
        Transformable<R>{

    private final AnyM2<W,Reader<T,R>,T> run;





    /**
     * @return The wrapped AnyM
     */

    public AnyM2<W,Reader<T,R>,T> unwrap() {
        return run;
    }

    public <R2> R2 unwrapTo(Function<? super AnyM2<W,Reader<T,R>,T>, ? extends R2> fn) {
        return unwrap().to2(fn);
    }

    private ReaderT(final AnyM2<W,Reader<T,R>,T> run) {
        this.run = run;
    }

    



    /**
     * Peek at the current value of the Future
     * <pre>
     * {@code 
     *    FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Future
     * @return FutureWT with peek call
     */
    @Override
    public ReaderT<W,T,R> peek(final Consumer<? super R> peek) {
        return of(run.peek(reader -> reader.map(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Map the wrapped Future
     * 
     * <pre>
     * {@code 
     *  FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //FutureWT<AnyMSeq<Stream<Future[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Future
     * @return FutureWT that applies the map function to the wrapped Future
     */

    public <B> ReaderT<W,T,B> map(final Function<? super R, ? extends B> f) {
        return new ReaderT<W,T,B>(
                                  run.map(o -> o.map(f)));
    }


    public <B> ReaderT<W, T, B> flatMap(final Function<? super R, ? extends Reader<T, B>> f) {

        return new ReaderT<W,T, B>(
                run.map(o -> o.flatMap(f)));

    }

    public <B> ReaderT<W,T,B> flatMapT(final Function<? super R, ReaderT<W,T,B>> f) {
        return of(run.map(reader-> reader.flatMap(a -> f.apply(a).run.unwrap())));
    }








    /**
     * Construct an FutureWT from an AnyM that wraps a monad containing  FutureWs
     * 
     * @param monads AnyM that contains a monad wrapping an Future
     * @return FutureWT
     */
    public static <W extends WitnessType<W>,T,R> ReaderT<W,T,R> of(final AnyM2<W,Reader<T,R>,T> monads) {
        return new ReaderT<>(
                                 monads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("ReaderT[%s]", run.unwrap().toString());
    }

    




   
    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ReaderT) {
            return run.equals(((ReaderT) o).run);
        }
        return false;
    }


    public String mkString(){
        return toString();
    }

    @Override
    public <U> ReaderT<W,T,U> cast(Class<? extends U> type) {
        return (ReaderT<W,T,U>)Transformable.super.cast(type);
    }



    @Override
    public <R2> ReaderT<W,T,R2> trampoline(Function<? super R, ? extends Trampoline<? extends R2>> mapper) {
        return (ReaderT<W,T,R2>)Transformable.super.trampoline(mapper);
    }



}
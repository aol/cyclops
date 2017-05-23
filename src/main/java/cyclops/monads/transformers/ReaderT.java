package cyclops.monads.transformers;

import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Reader;
import cyclops.control.Trampoline;
import cyclops.function.*;
import cyclops.monads.AnyM2;
import cyclops.monads.WitnessType;

import java.util.function.*;

/**
* Monad Transformer for Future's nested within another monadic type

 * 
 * FutureT allows the deeply wrapped Future to be manipulating within it's nested /contained context
 *
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Future(s)
 */
public final class ReaderT<W extends WitnessType<W>,T,R>  implements To<ReaderT<W,T,R>>,
        Transformable<R>, Fn1<T,R> {

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


    public <T2, R1, R2, R3, B> ReaderT<W,T,B> forEach4M(Function<? super R, ? extends ReaderT<W,T,R1>> value1,
                                                      BiFunction<? super R, ? super R1, ? extends ReaderT<W,T,R2>> value2,
                                                      Fn3<? super R, ? super R1, ? super R2, ? extends ReaderT<W,T,R3>> value3,
                                                      Fn4<? super R, ? super R1, ? super R2, ? super R3, ? extends B> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .map(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }


    public <T2, R1, R2, B> ReaderT<W,T,B> forEach3M(Function<? super R, ? extends ReaderT<W,T,R1>> value1,
                                                  BiFunction<? super R, ? super R1, ? extends ReaderT<W,T,R2>> value2,
                                                  Fn3<? super R, ? super R1, ? super R2, ? extends B> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .map(in3->yieldingFunction.apply(in,in2,in3))));

    }


    public <R1, B> ReaderT<W,T,B> forEach2M(Function<? super R, ? extends ReaderT<W,T,R1>> value1,
                                          BiFunction<? super R, ? super R1, ? extends B> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .map(in2->yieldingFunction.apply(in,in2)));
    }


    public <R1, R2, R3, R4> ReaderT<W,T,R4> forEach4(Function<? super R, ? extends Function<T,R1>> value2,
                                                   BiFunction<? super R, ? super R1, ? extends Function<T,R2>> value3,
                                                   Fn3<? super R, ? super R1, ? super R2, ? extends Function<T,R3>> value4,
                                                   Fn4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {


        return this.flatMap(in -> {

            Reader<T,R1> a = FluentFunctions.of(value2.apply(in));
            return a.flatMap(ina -> {
                Reader<T,R2> b = FluentFunctions.of(value3.apply(in,ina));
                return b.flatMap(inb -> {

                    Reader<T,R3> c = FluentFunctions.of(value4.apply(in,ina,inb));

                    return c.map(in2 -> {

                        return yieldingFunction.apply(in, ina, inb, in2);

                    });

                });


            });


        });

    }




    public <R1, R2, R4> ReaderT<W,T,R4> forEach3(Function<? super R, ? extends Function<T,R1>> value2,
                                               BiFunction<? super R, ? super R1, ? extends Function<T,R2>> value3,
                                               Fn3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = FluentFunctions.of(value2.apply(in));
            return a.flatMap(ina -> {
                Reader<T,R2> b = FluentFunctions.of(value3.apply(in,ina));
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }



    public <R1, R4> ReaderT<W,T,R4> forEach2(Function<? super R, Function<T,R1>> value2,
                                           BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = FluentFunctions.of(value2.apply(in));
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });




        });


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


    @Override
    public R apply(T a) {
        return run.firstValue().apply(a);
    }
}
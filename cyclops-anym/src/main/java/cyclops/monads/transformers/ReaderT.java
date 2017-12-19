package cyclops.monads.transformers;

import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Reader;
import cyclops.control.Trampoline;
import cyclops.function.*;
import cyclops.monads.AnyM;
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
        Transformable<R>, Function1<T,R> {

    private final AnyM<W,Reader<T,R>> run;





    /**
     * @return The wrapped AnyM
     */

    public AnyM<W,Reader<T,R>> unwrap() {
        return run;
    }

    public <R2> R2 unwrapTo(Function<? super AnyM<W,Reader<T,R>>, ? extends R2> fn) {
        return unwrap().to(fn);
    }

    private ReaderT(final AnyM<W,Reader<T,R>> run) {
        this.run = run;
    }





    /**
     * Peek at the current value of the Future
     * <pre>
     * {@code
     *    FutureT.of(AnyM.fromStream(Arrays.asFuture(10))
     *             .peek(System.out::println);
     *
     *     //prints 10
     * }
     * </pre>
     *
     * @param peek  Consumer to accept current value of Future
     * @return FutureT with peek call
     */
    @Override
    public ReaderT<W,T,R> peek(final Consumer<? super R> peek) {
        return of(run.peek(reader -> reader.mapFn(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Map the wrapped Future
     *
     * <pre>
     * {@code
     *  FutureT.of(AnyM.fromStream(Arrays.asFuture(10))
     *             .map(t->t=t+1);
     *
     *
     *  //FutureT<AnyMSeq<Stream<Future[11]>>>
     * }
     * </pre>
     *
     * @param f Mapping function for the wrapped Future
     * @return FutureT that applies the transform function to the wrapped Future
     */

    public <B> ReaderT<W,T,B> mapFn(final Function<? super R, ? extends B> f) {
        return new ReaderT<W,T,B>(
                                  run.map(o -> o.mapFn(f)));
    }

    public <B> ReaderT<W, T, B> flatMap(final Function<? super R, ? extends Reader<T, B>> f) {

        return new ReaderT<W,T, B>(
                run.map(o -> o.flatMap(f)));

    }

    public <B> ReaderT<W,T,B> flatMapT(final Function<? super R, ReaderT<W,T,B>> f) {
        return of(run.map(reader-> reader.flatMap(a -> f.apply(a).run.unwrap())));
    }








    /**
     * Construct an FutureT from an AnyM that wraps a monad containing  Futures
     *
     * @param monads AnyM that contains a monad wrapping an Future
     * @return FutureT
     */
    public static <W extends WitnessType<W>,T,R> ReaderT<W,T,R> of(final AnyM<W,Reader<T,R>> monads) {
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
                                                      Function3<? super R, ? super R1, ? super R2, ? extends ReaderT<W,T,R3>> value3,
                                                      Function4<? super R, ? super R1, ? super R2, ? super R3, ? extends B> yieldingFunction) {
        return this.flatMapT(in->value1.apply(in)
                .flatMapT(in2-> value2.apply(in,in2)
                        .flatMapT(in3->value3.apply(in,in2,in3)
                                .mapFn(in4->yieldingFunction.apply(in,in2,in3,in4)))));

    }


    public <T2, R1, R2, B> ReaderT<W,T,B> forEach3M(Function<? super R, ? extends ReaderT<W,T,R1>> value1,
                                                  BiFunction<? super R, ? super R1, ? extends ReaderT<W,T,R2>> value2,
                                                  Function3<? super R, ? super R1, ? super R2, ? extends B> yieldingFunction) {

        return this.flatMapT(in->value1.apply(in).flatMapT(in2-> value2.apply(in,in2)
                .mapFn(in3->yieldingFunction.apply(in,in2,in3))));

    }


    public <R1, B> ReaderT<W,T,B> forEach2M(Function<? super R, ? extends ReaderT<W,T,R1>> value1,
                                          BiFunction<? super R, ? super R1, ? extends B> yieldingFunction) {


        return this.flatMapT(in->value1.apply(in)
                .mapFn(in2->yieldingFunction.apply(in,in2)));
    }


    public <R1, R2, R3, R4> ReaderT<W,T,R4> forEach4(Function<? super R, ? extends Function<T,R1>> value2,
                                                   BiFunction<? super R, ? super R1, ? extends Function<T,R2>> value3,
                                                   Function3<? super R, ? super R1, ? super R2, ? extends Function<T,R3>> value4,
                                                   Function4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {


        return this.flatMap(in -> {

            Reader<T,R1> a = FluentFunctions.of(value2.apply(in));
            return a.flatMap(ina -> {
                Reader<T,R2> b = FluentFunctions.of(value3.apply(in,ina));
                return b.flatMap(inb -> {

                    Reader<T,R3> c = FluentFunctions.of(value4.apply(in,ina,inb));

                    return c.mapFn(in2 -> {

                        return yieldingFunction.apply(in, ina, inb, in2);

                    });

                });


            });


        });

    }




    public <R1, R2, R4> ReaderT<W,T,R4> forEach3(Function<? super R, ? extends Function<T,R1>> value2,
                                               BiFunction<? super R, ? super R1, ? extends Function<T,R2>> value3,
                                               Function3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = FluentFunctions.of(value2.apply(in));
            return a.flatMap(ina -> {
                Reader<T,R2> b = FluentFunctions.of(value3.apply(in,ina));
                return b.mapFn(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }



    public <R1, R4> ReaderT<W,T,R4> forEach2(Function<? super R, Function<T,R1>> value2,
                                           BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = FluentFunctions.of(value2.apply(in));
            return a.mapFn(in2 -> {
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
    public <R2> ReaderT<W,T,R2> trampoline(Function<? super R, ? extends Trampoline<? extends R2>> mapper) {
        return (ReaderT<W,T,R2>)Transformable.super.trampoline(mapper);
    }


    @Override
    public R apply(T a) {
        return run.firstValue(null).apply(a);
    }
}

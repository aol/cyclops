package cyclops.control;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops2.types.functor.Transformable;
import cyclops.function.FluentFunctions;
import cyclops.function.Fn1;
import cyclops.function.Fn3;
import cyclops.function.Fn4;

/**
 * An interface that represents the Reader monad
 * 
 * A technique for functional dependency injection. Functions rather than values
 * are manipulated and dependencies injected into Functions to execute them.
 * 
 * {@see <a href="https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75">Dependency injection using the Reader Monad in Java 8</a>}
 * 
 *
 * 
 * @author johnmcclean
 *
 * @param <T> Current input type of Function
 * @param <R> Current return type of Function
 */
public interface Reader<T, R> extends Fn1<T, R>, Transformable<R> {

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.Transformable#map(java.util.function.Function)
     */
    @Override
    default <R1> Reader<T, R1> map(final Function<? super R, ? extends R1> f2) {
        return FluentFunctions.of(this.andThen(f2));
    }

    /**
     * FlatMap this Reader by applying the prodived function and unnesting to a singleUnsafe Reader
     * 
     * @param f Transformation function to be flattened
     * @return Transformed Reader
     */
    default <R1> Reader<T, R1> flatMap(final Function<? super R, ? extends Reader<T, R1>> f) {
        return FluentFunctions.of(a -> f.apply(apply(a))
                                        .apply(a));
    }


    default <R1, R2, R3, R4> Reader<T,R4> forEach4(Function<? super R, Function<? super T,? extends R1>> value2,
                                                   BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                   Fn3<? super R, ? super R1, ? super R2,Function<? super T, ? extends R3>> value4,
                                                   Fn4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {


        Reader<? super T, ? extends R4> res =  this.flatMap(in -> {

            Reader<T,R1> a = narrow(FluentFunctions.of(value2.apply(in)));
            return a.flatMap(ina -> {
                Reader<T,R2> b = narrow(FluentFunctions.of(value3.apply(in,ina)));
                return b.flatMap(inb -> {

                    Reader<T,R3> c = narrow(FluentFunctions.of(value4.apply(in,ina,inb)));

                    return c.map(in2 -> {

                         return yieldingFunction.apply(in, ina, inb, in2);


                    });

                });


            });


        });
        return (Reader<T,R4>)res;

    }

    static <T,R>  Reader<T,R> narrow(Reader<? super T,? extends R> broad){
        return  (Reader<T,R>)broad;
    }


    default <R1, R2, R4> Reader<T,R4> forEach3(Function<? super R, Function<? super T,? extends R1>> value2,
                                                         BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                         Fn3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = narrow(FluentFunctions.of(value2.apply(in)));
            return a.flatMap(ina -> {
                Reader<T,R2> b = narrow(FluentFunctions.of(value3.apply(in,ina)));
                return b.map(in2 -> {
                        return yieldingFunction.apply(in, ina, in2);

                    });



            });

        });

    }



    default <R1, R4> Reader<T,R4> forEach2(Function<? super R, Function<? super T,? extends R1>> value2,
                                                BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = narrow(FluentFunctions.of(value2.apply(in)));
            return a.map(in2 -> {
                    return yieldingFunction.apply(in, in2);

                });




        });


    }
}

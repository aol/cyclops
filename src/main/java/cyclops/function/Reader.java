package cyclops.function;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops2.types.Transformable;

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
     * @see com.aol.cyclops2.types.Transformable#map(java.util.function.Function)
     */
    @Override
    default <R1> Reader<T, R1> map(final Function<? super R, ? extends R1> f2) {
        return FluentFunctions.of(this.andThen(f2));
    }

    /**
     * FlatMap this Reader by applying the prodived function and unnesting to a single Reader
     * 
     * @param f Transformation function to be flattened
     * @return Transformed Reader
     */
    default <R1> Reader<T, R1> flatMap(final Function<? super R, ? extends Reader<T, R1>> f) {
        return FluentFunctions.of(a -> f.apply(apply(a))
                                        .apply(a));
    }


    default <R1, R2, R3, R4> Reader<T,R4> forEach4(Function<? super R, ? extends Function<T,R1>> value2,
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




    default <R1, R2, R4> Reader<T,R4> forEach3(Function<? super R, ? extends Function<T,R1>> value2,
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



    default <R1, R4> Reader<T,R4> forEach2(Function<? super R, Function<T,R1>> value2,
                                                BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = FluentFunctions.of(value2.apply(in));
            return a.map(in2 -> {
                    return yieldingFunction.apply(in, in2);

                });




        });


    }
}

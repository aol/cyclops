package cyclops.control;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.function.*;
import com.oath.cyclops.hkt.DataWitness.reader;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

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
@FunctionalInterface
public interface Reader<T, R> extends Function1<T, R>, Transformable<R>,Higher<Higher<reader,T>,R> {

    public static <T,R> Reader<T,R> of(Reader<T,R> i){
        return i;
    }

    public static <T,R> Higher<Higher<reader,T>, R> widen(Reader<T,R> narrow) {
    return narrow;
  }

  @Override
  default <V> Reader<T, V> andThen(Function<? super R, ? extends V> after) {
    return t -> after.apply(apply(t));
  }

  default  <R2> Reader<T, Tuple2<R,R2>> zip(Reader<T, R2> o){
        return zip(o, Tuple::tuple);
    }
    default  <R2,B> Reader<T, B> zip(Reader<T, R2> o,BiFunction<? super R,? super R2,? extends B> fn){
        return flatMap(a -> o.map(b -> fn.apply(a,b)));
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.functor.Transformable#transform(java.util.function.Function)
     */
    @Override
    default <R1> Reader<T, R1> map(final Function<? super R, ? extends R1> f2) {
        return this.andThen(f2);
    }

    /**
     * FlatMap this Reader by applying the prodived function and unnesting to a single Reader
     *
     * @param f Transformation function to be flattened
     * @return Transformed Reader
     */
    default <R1> Reader<T, R1> flatMap(final Function<? super R, ? extends Reader<T, R1>> f) {
        return a -> f.apply(apply(a)).apply(a);
    }


    default <R1, R2, R3, R4> Reader<T,R4> forEach4(Function<? super R, Function<? super T,? extends R1>> value2,
                                                   BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                   Function3<? super R, ? super R1, ? super R2,Function<? super T, ? extends R3>> value4,
                                                   Function4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {


        Reader<? super T, ? extends R4> res =  this.flatMap(in -> {

            Reader<T,R1> a = functionToReader(value2.apply(in));
            return a.flatMap(ina -> {
                Reader<T,R2> b = functionToReader(value3.apply(in,ina));
                return b.flatMap(inb -> {

                    Reader<T,R3> c = functionToReader(value4.apply(in,ina,inb));

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
    static <T,R>  Reader<T,R> functionToReader(Function<? super T,? extends R> broad){
      return  a->broad.apply(a);
    }


    default <R1, R2, R4> Reader<T,R4> forEach3(Function<? super R, Function<? super T,? extends R1>> value2,
                                                         BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                         Function3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = functionToReader(value2.apply(in));
            return a.flatMap(ina -> {
                Reader<T,R2> b = functionToReader(value3.apply(in,ina));
                return b.map(in2 -> {
                        return yieldingFunction.apply(in, ina, in2);

                    });



            });

        });

    }



    default <R1, R4> Reader<T,R4> forEach2(Function<? super R, Function<? super T,? extends R1>> value2,
                                                BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Reader<T,R1> a = functionToReader(value2.apply(in));
            return a.map(in2 -> {
                    return yieldingFunction.apply(in, in2);

                });




        });


    }
    public static <T,R> Reader<T,  R> narrowK(Higher<Higher<reader,T>,R> hkt){
        return (Reader<T,R>)hkt;
    }



    default R foldLeft(T t, Monoid<R> monoid){
        Function1<T, R> x = this.andThen(v -> monoid.apply(monoid.zero(), v));
        return x.apply(t);
    }


}

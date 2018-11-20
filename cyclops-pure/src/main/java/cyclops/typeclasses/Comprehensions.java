package cyclops.typeclasses;

import com.oath.cyclops.hkt.Higher;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Deprecated//use Do instead
public class Comprehensions<W1> {


    public static <W1> Comprehensions<W1> of(Monad<W1>  monad){
        return new Comprehensions<>(monad);
    }
    public static <W1> Guarded<W1> of(MonadZero<W1>  monad){
        return new Comprehensions.Guarded<>(monad);
    }

    private final Monad<W1> monad;
    public  <T1, T2, T3, R1, R2, R3, R> Higher<W1,R> forEach4(Higher<W1,T1> value1,
                                                                   Function<? super T1, ? extends Higher<W1,R1>> value2,
                                                                   BiFunction<? super T1, ? super R1, ? extends Higher<W1,R2>> value3,
                                                                   Function3<? super T1, ? super R1, ? super R2, ? extends Higher<W1,R3>> value4,
                                                                   Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return monad.flatMap_(value1,in -> {

            Higher<W1,R1> a = value2.apply(in);
            return monad.flatMap_(a,ina -> {
                Higher<W1,R2> b = value3.apply(in,ina);
                return monad.flatMap_(b,inb -> {
                    Higher<W1,R3> c = value4.apply(in,ina,inb);
                    return monad.map_(c, in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }





    public  <T1, T2, R1, R2, R> Higher<W1,R> forEach3(Higher<W1,T1> value1,
                                                           Function<? super T1, ? extends Higher<W1,R1>> value2,
                                                           BiFunction<? super T1, ? super R1, ? extends Higher<W1,R2>> value3,
                                                           Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return monad.flatMap_(value1,in -> {

            Higher<W1,R1> a = value2.apply(in);
            return monad.flatMap_(a,ina -> {
                Higher<W1,R2> b = value3.apply(in,ina);
                return monad.map_(b, in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });

    }



    public  <T, R1, R> Higher<W1,R> forEach2(Higher<W1, T> value1, Function<? super T, ? extends Higher<W1,R1>> value2,
                                                  BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return monad.flatMap_(value1,in -> {

            Higher<W1,R1> a = value2.apply(in);
            return monad.map_(a, in2 -> yieldingFunction.apply(in,  in2));
        });



    }
    @AllArgsConstructor(access= AccessLevel.PRIVATE)
    public static class Guarded<W1> {

        private final MonadZero<W1> monadZero;
        public  <T1, T2, T3, R1, R2, R3, R> Higher<W1,R> forEach4(Higher<W1,? extends T1> value1,
                                                                  Function<? super T1, ? extends Higher<W1,R1>> value2,
                                                                  BiFunction<? super T1, ? super R1, ? extends Higher<W1,R2>> value3,
                                                                  Function3<? super T1, ? super R1, ? super R2, ? extends Higher<W1,R3>> value4,
                                                                  Function4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                  Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

            return monadZero.flatMap_(value1,in -> {

                Higher<W1,R1> a = value2.apply(in);
                return monadZero.flatMap_(a,ina -> {
                    Higher<W1,R2> b = value3.apply(in,ina);
                    return monadZero.flatMap_(b,inb -> {
                        Higher<W1,R3> c = value4.apply(in,ina,inb);
                        Higher<W1, R3> x = monadZero.filter_(c, in2 -> filterFunction.apply(in, ina, inb, in2));
                        return monadZero.map_(x, in2 -> yieldingFunction.apply(in, ina, inb, in2));
                    });

                });

            });

        }
        public  <T1, T2, R1, R2, R> Higher<W1,R> forEach3(Higher<W1,? extends T1> value1,
                                                          Function<? super T1, ? extends Higher<W1,R1>> value2,
                                                          BiFunction<? super T1, ? super R1, ? extends Higher<W1,R2>> value3,
                                                          Function3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                          Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

            return monadZero.flatMap_(value1,in -> {

                Higher<W1,R1> a = value2.apply(in);
                return monadZero.flatMap_(a,ina -> {
                    Higher<W1,R2> b = value3.apply(in,ina);
                    Higher<W1, R2> x = monadZero.filter_(b, in2 -> filterFunction.apply(in, ina, in2));
                    return   monadZero.map_(x,in2 -> yieldingFunction.apply(in, ina, in2));
                });



            });

        }
        public  <T, R1, R> Higher<W1,R> forEach2(Higher<W1,? extends T> value1, Function<? super T, ? extends Higher<W1,R1>> value2,
                                                 BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                                 BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

            return monadZero.flatMap_(value1,in -> {

                Higher<W1,R1> a = value2.apply(in);
                Higher<W1, R1> x = monadZero.filter_(a, in2 -> filterFunction.apply(in, in2));
                return    monadZero.map_(x,in2 -> yieldingFunction.apply(in,  in2));
            });




        }


    }


}

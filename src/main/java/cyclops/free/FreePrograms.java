package cyclops.free;

import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Fn5;
import org.jooq.lambda.tuple.*;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Static for comprehensions for working with Free
 */
public interface FreePrograms {
    public static <T,F,R1, R2, R3,R4,R5,R6,R7> Free<F,R7> forEach(Free<F,T> free,
                                                              Function<? super T, ? extends Free<F,R1>> value2,
                                                              Function<? super Tuple2<? super T,? super R1>, ? extends Free<F,R2>> value3,
                                                              Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Free<F,R3>> value4,
                                                              Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Free<F,R4>> value5,
                                                              Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Free<F,R5>> value6,
                                                              Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Free<F,R6>> value7,
                                                              Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends Free<F,R7>> value8
    ) {

        return free.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Free<F,R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d.flatMap(ind->{
                            Free<F,R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                            return e.flatMap(ine->{
                                Free<F,R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                return f.flatMap(inf->{
                                    Free<F,R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                                    return g;

                                });

                            });
                        });

                    });

                });


            });


        });

    }
    public static <T,F,R1, R2, R3,R4,R5,R6> Free<F,R6> forEach(Free<F,T> free,
                                                           Function<? super T, ? extends Free<F,R1>> value2,
                                                           Function<? super Tuple2<? super T,? super R1>, ? extends Free<F,R2>> value3,
                                                           Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Free<F,R3>> value4,
                                                           Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Free<F,R4>> value5,
                                                           Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Free<F,R5>> value6,
                                                              Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Free<F,R6>> value7
    ) {

        return free.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Free<F,R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d.flatMap(ind->{
                            Free<F,R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                            return e.flatMap(ine->{
                                Free<F,R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                return f;
                            });
                        });

                    });

                });


            });


        });

    }

    public static <T,F,R1, R2, R3,R4,R5> Free<F,R5> forEach(Free<F,T> free,
                                                        Function<? super T, ? extends Free<F,R1>> value2,
                                                        Function<? super Tuple2<? super T,? super R1>, ? extends Free<F,R2>> value3,
                                                        Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Free<F,R3>> value4,
                                                        Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Free<F,R4>> value5,
                                                        Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Free<F,R5>> value6
    ) {

        return free.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Free<F,R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d.flatMap(ind->{
                            Free<F,R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                            return e;
                        });
                    });

                });


            });


        });

    }
    public static <T,F,R1, R2, R3,R4> Free<F,R4> forEach(Free<F,T> free,
                                                            Function<? super T, ? extends Free<F,R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, ? extends Free<F,R2>> value3,
                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Free<F,R3>> value4,
                                                            Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Free<F,R4>> value5

    ) {

        return free.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Free<F,R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d;
                    });

                });


            });


        });

    }
    public static <T,F,R1, R2, R3> Free<F,R3> forEach(Free<F,T> free,
                                                         Function<? super T, ? extends Free<F,R1>> value2,
                                                         Function<? super Tuple2<? super T,? super R1>, ? extends Free<F,R2>> value3,
                                                         Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Free<F,R3>> value4

    ) {

        return free.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c;

                });


            });


        });

    }
    public static <T,F,R1, R2> Free<F,R2> forEach(Free<F,T> free,
                                                      Function<? super T, ? extends Free<F,R1>> value2,
                                                      Function<? super Tuple2<? super T,? super R1>, ? extends Free<F,R2>> value3

    ) {

        return free.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(Tuple.tuple(in,ina));
                return b;


            });


        });

    }
    public static <T,F,R1> Free<F,R1> forEach(Free<F,T> free,
                                                  Function<? super T, ? extends Free<F,R1>> value2


    ) {

        return free.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a;


        });

    }



}

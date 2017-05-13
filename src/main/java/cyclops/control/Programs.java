package cyclops.control;

import cyclops.typeclasses.free.Free;
import org.jooq.lambda.tuple.*;

import java.util.function.Function;

/**
 * Created by johnmcclean on 11/05/2017.
 */
public class Programs {

    public static <T,F,R1, R2, R3,R4,R5,R6,R7> Computations<R7> forEach(Computations<T> free,
                                                                  Function<? super T, ? extends Computations<R1>> value2,
                                                                  Function<? super Tuple2<? super T,? super R1>, ? extends Computations<R2>> value3,
                                                                  Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Computations<R3>> value4,
                                                                  Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Computations<R4>> value5,
                                                                  Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Computations<R5>> value6,
                                                                  Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Computations<R6>> value7,
                                                                  Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends Computations<R7>> value8
    ) {

        return free.flatMap(in -> {

            Computations<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Computations<R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Computations<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Computations<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d.flatMap(ind->{
                            Computations<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                            return e.flatMap(ine->{
                                Computations<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                return f.flatMap(inf->{
                                    Computations<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                                    return g;

                                });

                            });
                        });

                    });

                });


            });


        });

    }
    public static <T,F,R1, R2, R3,R4,R5,R6> Computations<R6> forEach(Computations<T> free,
                                                               Function<? super T, ? extends Computations<R1>> value2,
                                                               Function<? super Tuple2<? super T,? super R1>, ? extends Computations<R2>> value3,
                                                               Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Computations<R3>> value4,
                                                               Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Computations<R4>> value5,
                                                               Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Computations<R5>> value6,
                                                               Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Computations<R6>> value7
    ) {

        return free.flatMap(in -> {

            Computations<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Computations<R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Computations<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Computations<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d.flatMap(ind->{
                            Computations<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                            return e.flatMap(ine->{
                                Computations<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                return f;
                            });
                        });

                    });

                });


            });


        });

    }

    public static <T,F,R1, R2, R3,R4,R5> Computations<R5> forEach(Computations<T> free,
                                                            Function<? super T, ? extends Computations<R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, ? extends Computations<R2>> value3,
                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Computations<R3>> value4,
                                                            Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Computations<R4>> value5,
                                                            Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Computations<R5>> value6
    ) {

        return free.flatMap(in -> {

            Computations<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Computations<R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Computations<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Computations<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d.flatMap(ind->{
                            Computations<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                            return e;
                        });
                    });

                });


            });


        });

    }
    public static <T,F,R1, R2, R3,R4> Computations<R4> forEach(Computations<T> free,
                                                         Function<? super T, ? extends Computations<R1>> value2,
                                                         Function<? super Tuple2<? super T,? super R1>, ? extends Computations<R2>> value3,
                                                         Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Computations<R3>> value4,
                                                         Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Computations<R4>> value5

    ) {

        return free.flatMap(in -> {

            Computations<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Computations<R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Computations<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c.flatMap(inc->{
                        Computations<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                        return d;
                    });

                });


            });


        });

    }
    public static <T,F,R1, R2, R3> Computations<R3> forEach(Computations<T> free,
                                                      Function<? super T, ? extends Computations<R1>> value2,
                                                      Function<? super Tuple2<? super T,? super R1>, ? extends Computations<R2>> value3,
                                                      Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Computations<R3>> value4

    ) {

        return free.flatMap(in -> {

            Computations<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Computations<R2> b = value3.apply(Tuple.tuple(in,ina));
                return b.flatMap(inb -> {

                    Computations<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                    return c;

                });


            });


        });

    }
    public static <T,F,R1, R2> Computations<R2> forEach(Computations<T> free,
                                                  Function<? super T, ? extends Computations<R1>> value2,
                                                  Function<? super Tuple2<? super T,? super R1>, ? extends Computations<R2>> value3

    ) {

        return free.flatMap(in -> {

            Computations<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Computations<R2> b = value3.apply(Tuple.tuple(in,ina));
                return b;


            });


        });

    }
    public static <T,F,R1> Computations<R1> forEach(Computations<T> free,
                                              Function<? super T, ? extends Computations<R1>> value2


    ) {

        return free.flatMap(in -> {

            Computations<R1> a = value2.apply(in);
            return a;


        });

    }


}

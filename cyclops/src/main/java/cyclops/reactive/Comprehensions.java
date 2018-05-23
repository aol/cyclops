package cyclops.reactive;

import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.data.tuple.Tuple5;
import cyclops.data.tuple.Tuple6;
import cyclops.data.tuple.Tuple7;

import java.util.function.Function;

public class Comprehensions {


        public static <T,F,R1, R2, R3,R4,R5,R6,R7> Managed<R7> forEach(Managed<T> io,
                                                                  Function<? super T, Managed<R1>> value2,
                                                                  Function<? super Tuple2<? super T,? super R1>, Managed<R2>> value3,
                                                                  Function<? super Tuple3<? super T,? super R1,? super R2>, Managed<R3>> value4,
                                                                  Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, Managed<R4>> value5,
                                                                  Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, Managed<R5>> value6,
                                                                  Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, Managed<R6>> value7,
                                                                  Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, Managed<R7>> value8
        ) {

            return io.flatMap(in -> {

                Managed<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Managed<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Managed<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Managed<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d.flatMap(ind->{
                                Managed<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                                return e.flatMap(ine->{
                                    Managed<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                    return f.flatMap(inf->{
                                        Managed<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                                        return g;

                                    });

                                });
                            });

                        });

                    });


                });


            });

        }
        public static <T,F,R1, R2, R3,R4,R5,R6> Managed<R6> forEach(Managed<T> io,
                                                               Function<? super T, Managed<R1>> value2,
                                                               Function<? super Tuple2<? super T,? super R1>, Managed<R2>> value3,
                                                               Function<? super Tuple3<? super T,? super R1,? super R2>, Managed<R3>> value4,
                                                               Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, Managed<R4>> value5,
                                                               Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, Managed<R5>> value6,
                                                               Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, Managed<R6>> value7
        ) {

            return io.flatMap(in -> {

                Managed<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Managed<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Managed<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Managed<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d.flatMap(ind->{
                                Managed<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                                return e.flatMap(ine->{
                                    Managed<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                    return f;
                                });
                            });

                        });

                    });


                });


            });

        }

        public static <T,F,R1, R2, R3,R4,R5> Managed<R5> forEach(Managed<T> io,
                                                            Function<? super T, Managed<R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, Managed<R2>> value3,
                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, Managed<R3>> value4,
                                                            Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, Managed<R4>> value5,
                                                            Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, Managed<R5>> value6
        ) {

            return io.flatMap(in -> {

                Managed<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Managed<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Managed<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Managed<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d.flatMap(ind->{
                                Managed<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                                return e;
                            });
                        });

                    });


                });


            });

        }
        public static <T,F,R1, R2, R3,R4> Managed<R4> forEach(Managed<T> io,
                                                         Function<? super T, Managed<R1>> value2,
                                                         Function<? super Tuple2<? super T,? super R1>, Managed<R2>> value3,
                                                         Function<? super Tuple3<? super T,? super R1,? super R2>, Managed<R3>> value4,
                                                         Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, Managed<R4>> value5

        ) {

            return io.flatMap(in -> {

                Managed<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Managed<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Managed<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Managed<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d;
                        });

                    });


                });


            });

        }
        public static <T,F,R1, R2, R3> Managed<R3> forEach(Managed<T> io,
                                                      Function<? super T, Managed<R1>> value2,
                                                      Function<? super Tuple2<? super T,? super R1>, Managed<R2>> value3,
                                                      Function<? super Tuple3<? super T,? super R1,? super R2>, Managed<R3>> value4

        ) {

            return io.flatMap(in -> {

                Managed<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Managed<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Managed<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c;

                    });


                });


            });

        }
        public static <T,F,R1, R2> Managed<R2> forEach(Managed<T> io,
                                                  Function<? super T, Managed<R1>> value2,
                                                  Function<? super Tuple2<T,R1>, Managed<R2>> value3

        ) {

            return io.flatMap(in -> {

                Managed<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Managed<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b;


                });


            });

        }
        public static <T,F,R1> Managed<R1> forEach(Managed<T> io,
                                              Function<? super T, Managed<R1>> value2) {

            return io.flatMap(in -> {

                Managed<R1> a = value2.apply(in);
                return a;


            });

        }



}

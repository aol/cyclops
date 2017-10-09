package cyclops.typeclasses.free;

import com.aol.cyclops2.hkt.Higher;

import com.aol.cyclops2.hkt.Higher2;
import cyclops.control.Xor;
import cyclops.control.lazy.Either3;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Function5;
import cyclops.monads.Witness.free;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cyclops.collections.tuple.*;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Free monad for cyclops2
 *
 * Inspiration and heavily influenced by https://github.com/xuwei-k/free-monad-java/blob/master/src/main/java/free/Free.java
 * Other influences incl :- http://www.slideshare.net/kenbot/running-free-with-the-monads
 * and https://github.com/scalaz/scalaz/blob/series/7.2.x/core/src/main/scala/scalaz/Free.scala
 * and https://github.com/typelevel/cats/blob/master/free/src/main/scala/cats/free/Free.scala
 *
 * Org recover : https://github.com/aol/cyclops/blob/v4.0.1/cyclops-free-monad/src/main/java/com/aol/cyclops/monad/Free.java
 *
 * @param <F> Transformable type
 * @param <T> Data type of Transformable
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class Free<F, T> implements Higher2<free,F,T> {

    /**
     * Static for comprehensions for working with Free
     */
    public static interface Comprehensions {


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

    public <R1, R2, R3,R4,R5> Free<F,R5> forEach6(Function<? super T, ? extends Free<F,R1>> value2,
                                               BiFunction<? super T, ? super R1, ? extends Free<F,R2>> value3,
                                               Function3<? super T, ? super R1, ? super R2, ? extends Free<F,R3>> value4,
                                               Function4<? super T, ? super R1, ? super R2,? super R3, ? extends Free<F,R4>> value5,
                                                  Function5<? super T, ? super R1, ? super R2,? super R3, ? super R4, ? extends Free<F,R5>> value6
    ) {

        return this.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(in,ina,inb);

                    return c.flatMap(inc->{
                        Free<F,R4> d = value5.apply(in,ina,inb,inc);
                        return d.flatMap(ind->{
                            Free<F,R5> e = value6.apply(in,ina,inb,inc,ind);
                            return e;
                        });
                    });

                });


            });


        });

    }


    public <R1, R2, R3,R4> Free<F,R4> forEach5(Function<? super T, ? extends Free<F,R1>> value2,
                                            BiFunction<? super T, ? super R1, ? extends Free<F,R2>> value3,
                                            Function3<? super T, ? super R1, ? super R2, ? extends Free<F,R3>> value4,
                                               Function4<? super T, ? super R1, ? super R2,? super R3, ? extends Free<F,R4>> value5
    ) {

        return this.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(in,ina,inb);

                    return c.flatMap(inc->{
                        Free<F,R4> d = value5.apply(in,ina,inb,inc);
                        return d;
                    });

                });


            });


        });

    }

    public <R1, R2, R3> Free<F,R3> forEach4(Function<? super T, ? extends Free<F,R1>> value2,
                                                   BiFunction<? super T, ? super R1, ? extends Free<F,R2>> value3,
                                                   Function3<? super T, ? super R1, ? super R2, ? extends Free<F,R3>> value4
                                                   ) {

        return this.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {

                    Free<F,R3> c = value4.apply(in,ina,inb);

                    return c;

                });


            });


        });

    }

    public final <R1, R2> Free<F,R2> forEach3(Function<? super T, ? extends Free<F,R1>> value2,
                                               BiFunction<? super T, ? super R1, ? extends Free<F,R2>> value3
                                               ) {

        return this.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Free<F,R2> b = value3.apply(in,ina);
                return b;



            });

        });

    }
    public final <R1> Free<F,R1> forEach2(Function<? super T, Free<F,R1>> value2) {

        return this.flatMap(in -> {

            Free<F,R1> a = value2.apply(in);
            return a;




        });


    }
    public static <F, T> Free<F, T> liftF(final Higher<F, T> value, final Functor<F> functor){

        return new Suspend<F, T>(functor.map(Free::done, value));
    }



    public static <F, T> Free<F, T> done(final T t){
        return new Pure<>(t);
    }


    public static <F, B> Free<F, B> suspend(final Higher<F, Free<F, B>> b) {
        return new Suspend<>(b);
    }


    public final T go(final Function<? super Higher<F, Free<F, T>>,? extends Free<F,T>> fn, final Functor<F> functor){
        Free<F,T> toUse = this;
        for(;;) {
            Xor<Higher<F, Free<F, T>>, T> xor = (Xor)toUse.resume(functor);
            if (xor.isPrimary())
                return xor.orElse(null);
            toUse =  fn.apply(xor.secondaryOrElse(null));
        }
    }


    public abstract <R> R visit(Function<? super Pure<F,T>, ? extends R> done,
                            Function<? super Suspend<F,T>, ? extends R> suspend,
                            Function<? super FlatMapped<F,?,T>,? extends R> flatMapped);



    public abstract <R> Free<F, R> flatMap(final Function<? super T,? extends Free<F, ? extends R>> f);

    public final <R> Xor<R, T> resume(final Functor<F> functor, Function<Higher<F,Free<F,T>>,R> decoder) {
        return resume(functor).secondaryMap(decoder);
    }
    public  <B> Free<F,Tuple2<T,B>> zip(Functor<F> f,Free<F,B> b){
        return zip(f,b,Tuple::tuple);
    }
    public  <B,R> Free<F,R> zip(Functor<F> f,Free<F,B> b,BiFunction<? super T,? super B,? extends R> zipper){

        Xor<Higher<F, Free<F, T>>, T> first = resume(f);
        Xor<Higher<F, Free<F, B>>, B> second = b.resume(f);

        if(first.isSecondary() && second.isSecondary()) {
            return suspend(f.map_(first.secondaryOrElse(null), a1->{
                return suspend(f.map_(second.secondaryOrElse(null), b1->a1.zip(f,b1,zipper)));
            }));
        }
        if(first.isPrimary() && second.isPrimary()){
            return done(zipper.apply(first.orElse(null),second.orElse(null)));
        }
        if(first.isSecondary() && second.isPrimary()){
            return suspend(f.map_(first.secondaryOrElse(null), a1->a1.zip(f,b,zipper)));

        }
        if(first.isPrimary() && second.isSecondary()){
            return suspend(f.map_(second.secondaryOrElse(null), a1->Free.<F,T>done(first.orElse(null)).zip(f,b,zipper)));
        }
        return null;
    }

    public  <B,C> Free<F,Tuple3<T,B,C>> zip(Functor<F> f,Free<F,B> b, Free<F,C> c){
        return zip(f,b,c,(x,y,z)->Tuple.tuple(x,y,z));

    }
    public  <B,C,R> Free<F,R> zip(Functor<F> f,Free<F,B> b, Free<F,C> c, Function3<? super T, ? super B, ? super C,? extends R> fn){

        Xor<Higher<F, Free<F, T>>, T> first = resume(f);
        Xor<Higher<F, Free<F, B>>, B> second = b.resume(f);
        Xor<Higher<F, Free<F, C>>, C> third = c.resume(f);

        if(first.isSecondary() && second.isSecondary() && third.isSecondary()) {
            return suspend(f.map_(first.secondaryOrElse(null), a1->{
                return suspend(f.map_(second.secondaryOrElse(null), b1->{
                    return suspend(f.map_(third.secondaryOrElse(null), c1->a1.zip(f,b1,c1,fn)));
                }));
            }));
        }

        if(first.isPrimary() && second.isPrimary() && third.isPrimary()){
            return done(fn.apply(first.orElse(null),second.orElse(null),third.orElse(null)));
        }

        if(first.isSecondary() && second.isPrimary() && third.isPrimary()){
            return suspend(f.map_(first.secondaryOrElse(null), a1->a1.zip(f,b,c,fn)));
        }
        if(first.isPrimary() && second.isSecondary() && third.isPrimary()){

            return suspend(f.map_(second.secondaryOrElse(null), b1->this.zip(f,b1,c,fn)));



        }
        if(first.isPrimary() && second.isPrimary() && third.isSecondary()){
            return suspend(f.map_(third.secondaryOrElse(null), c1->this.zip(f,b,c1,fn)));
        }


        if(first.isPrimary() && second.isSecondary() && third.isSecondary()){
            return suspend(f.map_(second.secondaryOrElse(null), b1->{
                return suspend(f.map_(third.secondaryOrElse(null), c1->this.zip(f,b1,c1,fn)));
            }));

        }
        if(first.isSecondary() && second.isPrimary() && third.isSecondary()){
            return suspend(f.map_(first.secondaryOrElse(null), a1->{

                return suspend(f.map_(third.secondaryOrElse(null), c1->a1.zip(f,b,c1,fn)));

            }));
        }
        if(first.isSecondary() && second.isSecondary() && third.isPrimary()){
            return suspend(f.map_(first.secondaryOrElse(null), a1->{
                return suspend(f.map_(second.secondaryOrElse(null), b1->a1.zip(f,b1,c,fn)));

            }));
        }
        //unreachable
        return null;
    }
    /*
     * Functor and HKT decoder for Free
     */
    @AllArgsConstructor
    static class FreeF<F,T>{

        Functor<F> functor;
        Function<Higher<F,Free<F,?>>,?> decoder1;

        private <R,X> Function<Higher<F,Free<F,R>>,X> decoder(){
            return (Function)decoder1;
        }
        public final <R1,R2,X1,X2> Tuple2<Xor<X1,R1>,Xor<X2,R2>> product(Free<F,R1> free1, Free<F,R2> free2 ){

            return Tuple.tuple(free1.resume(functor,decoder()),free2.resume(functor,decoder()));

        }

    }


    public static final <F,R1,R2,X1,X2> Tuple2<Xor<X1,R1>,Xor<X2,R2>> product(final Functor<F> functor, Free<F,R1> free1, Function<Higher<F,Free<F,R1>>,X1> decoder1,
                                                                              Free<F,R2> free2, Function<Higher<F,Free<F,R2>>,X2> decoder2 ){

        return Tuple.tuple(free1.resume(functor,decoder1),free2.resume(functor,decoder2));

    }
    public final Xor<Higher<F, Free<F, T>>, T> resume(final Functor<F> functor) {
        return resumeInternal( functor).visit(Xor::secondary,Xor::primary,t->null);

    }
   abstract <T1, U> Either3<Higher<F, Free<F, T>>, T, Free<F, T>> resumeInternal(final Functor<F> functor);

    public final <R> Free<F, R> map(final Function<? super T, ? extends R> mapper) {
        return flatMap(t -> new Pure<>(mapper.apply(t)));
    }

    private static class Pure<F, T> extends Free<F, T>{

        private final T value;

        private Pure(final T value) {
            this.value = value;
        }
        @Override
        public <R> R visit(Function<? super Pure<F, T>, ? extends R> done,
                                    Function<? super Suspend<F, T>, ? extends R> suspend,
                                    Function<? super FlatMapped<F,?, T>,? extends R> flatMapped){
            return done.apply(this);
        }
        <T1, U> Either3<Higher<F, Free<F, T>>, T, Free<F, T>> resumeInternal(final Functor<F> functor){
            return Either3.left2(value);
        }
        @Override
        public <R> Free<F, R> flatMap(Function<? super T, ? extends Free<F, ? extends R>> f) {
            return new FlatMapped<>(this, f);
        }
    }

    private static final class Suspend<F, T> extends Free<F, T>{
        private final Higher<F, Free<F, T>> suspended;

        private Suspend(final Higher<F, Free<F, T>> suspended) {
            this.suspended = suspended;
        }
        @Override
        public <R> R visit(Function<? super Pure<F, T>, ? extends R> done,
                           Function<? super Suspend<F, T>, ? extends R> suspend,
                           Function<? super FlatMapped<F,?, T>,? extends R> flatMapped){
            return suspend.apply(this);
        }
        <T1, U> Either3<Higher<F, Free<F, T>>, T, Free<F, T>> resumeInternal(final Functor<F> functor){
            return Either3.left1(suspended);
        }
        @Override
        public <R> Free<F, R> flatMap(Function<? super T,? extends Free<F, ? extends R>> f) {
            return new FlatMapped<>(this, f);
        }
    }

    private static final class FlatMapped<F, IN, T> extends Free<F, T>{
        private final Free<F, IN> free;
        private final Function<? super IN, ? extends Free<F, ? extends T>> fn;

        private FlatMapped(final Free<F, IN> free, final Function<? super IN,? extends Free<F,? extends T>> fn){
            this.free = free;
            this.fn = fn;
        }

        private Function<IN,  Free<F, T>> narrowFn(){
            return (Function<IN,  Free<F, T>>)fn;
        }
        @Override
        public <R> R visit(Function<? super Pure<F, T>, ? extends R> done,
                           Function<? super Suspend<F, T>, ? extends R> suspend,
                           Function<? super FlatMapped<F,?, T>,? extends R> flatMapped){
            return flatMapped.apply(this);
        }
        @Override
        public <R> Free<F, R> flatMap(final Function<? super T,? extends Free<F, ? extends R>> g) {
            return new FlatMapped<F, IN, R>(free, aa -> new FlatMapped<F,T, R>(narrowFn().apply(aa), g));
        }
        <T1, U> Either3<Higher<F, Free<F, T>>, T, Free<F, T>> resumeInternal(final Functor<F> functor){
            return   resumeNestedFree(functor).flatMap(cur->cur.resumeInternal(functor));
        }
        private  <U> Either3<Higher<F, Free<F, T>>, T, Free<F, T>> resumeNestedFree(Functor<F> functor){
            Function<IN, Free<F, T>> f = narrowFn();
            return free.visit(pure->Either3.right(f.apply(pure.value)),
                    s-> Either3.left1(functor.map(o -> o.flatMap(f), s.suspended)),
                    fm->{
                        final FlatMapped<F, U, IN> flatMapped2 = (FlatMapped<F, U, IN>)fm;
                        return Either3.right(flatMapped2.free.flatMap(o ->
                                flatMapped2.fn.apply(o).flatMap(fn)));
                    });

        }


    }

    public static <F,T> Free<F,T> narrowK(Higher<Higher<free, F>, T> ds){
        return (Free<F,T>)ds;
    }

    static  class Instances {
        public static <F> Applicative<Higher<free, F>> applicative(cyclops.typeclasses.Pure<F> pure,Functor<F> functor) {
            return new Applicative<Higher<free, F>>() {

                @Override
                public <T, R> Higher<Higher<free, F>, R> ap(Higher<Higher<free, F>, ? extends Function<T, R>> fn, Higher<Higher<free, F>, T> apply) {
                    Free<F, ? extends Function<T, R>> f = narrowK(fn);
                    Free<F, T> a = narrowK(apply);
                    return f.flatMap(x->a.map(t->x.apply(t)));
                }

                @Override
                public <T, R> Higher<Higher<free, F>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<free, F>, T> ds) {
                    return narrowK(ds).map(fn);
                }

                @Override
                public <T> Higher<Higher<free, F>, T> unit(T value) {
                    return liftF(pure.unit(value),functor);
                }
            };


        }
    }

}

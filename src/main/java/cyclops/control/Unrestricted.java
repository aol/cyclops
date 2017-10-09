package cyclops.control;

import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.lazy.Either3;
import cyclops.function.Function3;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cyclops.collections.tuple.*;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Java friendly version of Free (as in Unrestricted) monad for cyclops2
 * also see {@link cyclops.typeclasses.free.Free} for a more advanced type safe version
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
public abstract class Unrestricted<T> {

    /**
     * Created by johnmcclean on 11/05/2017.
     */
    public static class Comprehensions {

        public static <T,F,R1, R2, R3,R4,R5,R6,R7> Unrestricted<R7> forEach(Unrestricted<T> free,
                                                                            Function<? super T, ? extends Unrestricted<R1>> value2,
                                                                            Function<? super Tuple2<? super T,? super R1>, ? extends Unrestricted<R2>> value3,
                                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Unrestricted<R3>> value4,
                                                                            Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Unrestricted<R4>> value5,
                                                                            Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Unrestricted<R5>> value6,
                                                                            Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Unrestricted<R6>> value7,
                                                                            Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends Unrestricted<R7>> value8
        ) {

            return free.flatMap(in -> {

                Unrestricted<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Unrestricted<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Unrestricted<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Unrestricted<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d.flatMap(ind->{
                                Unrestricted<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                                return e.flatMap(ine->{
                                    Unrestricted<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                    return f.flatMap(inf->{
                                        Unrestricted<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                                        return g;

                                    });

                                });
                            });

                        });

                    });


                });


            });

        }
        public static <T,F,R1, R2, R3,R4,R5,R6> Unrestricted<R6> forEach(Unrestricted<T> free,
                                                                         Function<? super T, ? extends Unrestricted<R1>> value2,
                                                                         Function<? super Tuple2<? super T,? super R1>, ? extends Unrestricted<R2>> value3,
                                                                         Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Unrestricted<R3>> value4,
                                                                         Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Unrestricted<R4>> value5,
                                                                         Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Unrestricted<R5>> value6,
                                                                         Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Unrestricted<R6>> value7
        ) {

            return free.flatMap(in -> {

                Unrestricted<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Unrestricted<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Unrestricted<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Unrestricted<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d.flatMap(ind->{
                                Unrestricted<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                                return e.flatMap(ine->{
                                    Unrestricted<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                                    return f;
                                });
                            });

                        });

                    });


                });


            });

        }

        public static <T,F,R1, R2, R3,R4,R5> Unrestricted<R5> forEach(Unrestricted<T> free,
                                                                      Function<? super T, ? extends Unrestricted<R1>> value2,
                                                                      Function<? super Tuple2<? super T,? super R1>, ? extends Unrestricted<R2>> value3,
                                                                      Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Unrestricted<R3>> value4,
                                                                      Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Unrestricted<R4>> value5,
                                                                      Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Unrestricted<R5>> value6
        ) {

            return free.flatMap(in -> {

                Unrestricted<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Unrestricted<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Unrestricted<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Unrestricted<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d.flatMap(ind->{
                                Unrestricted<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                                return e;
                            });
                        });

                    });


                });


            });

        }
        public static <T,F,R1, R2, R3,R4> Unrestricted<R4> forEach(Unrestricted<T> free,
                                                                   Function<? super T, ? extends Unrestricted<R1>> value2,
                                                                   Function<? super Tuple2<? super T,? super R1>, ? extends Unrestricted<R2>> value3,
                                                                   Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Unrestricted<R3>> value4,
                                                                   Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Unrestricted<R4>> value5

        ) {

            return free.flatMap(in -> {

                Unrestricted<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Unrestricted<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Unrestricted<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c.flatMap(inc->{
                            Unrestricted<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
                            return d;
                        });

                    });


                });


            });

        }
        public static <T,F,R1, R2, R3> Unrestricted<R3> forEach(Unrestricted<T> free,
                                                                Function<? super T, ? extends Unrestricted<R1>> value2,
                                                                Function<? super Tuple2<? super T,? super R1>, ? extends Unrestricted<R2>> value3,
                                                                Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Unrestricted<R3>> value4

        ) {

            return free.flatMap(in -> {

                Unrestricted<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Unrestricted<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b.flatMap(inb -> {

                        Unrestricted<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

                        return c;

                    });


                });


            });

        }
        public static <T,F,R1, R2> Unrestricted<R2> forEach(Unrestricted<T> free,
                                                            Function<? super T, ? extends Unrestricted<R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, ? extends Unrestricted<R2>> value3

        ) {

            return free.flatMap(in -> {

                Unrestricted<R1> a = value2.apply(in);
                return a.flatMap(ina -> {
                    Unrestricted<R2> b = value3.apply(Tuple.tuple(in,ina));
                    return b;


                });


            });

        }
        public static <T,F,R1> Unrestricted<R1> forEach(Unrestricted<T> free,
                                                        Function<? super T, ? extends Unrestricted<R1>> value2


        ) {

            return free.flatMap(in -> {

                Unrestricted<R1> a = value2.apply(in);
                return a;


            });

        }


    }

    /**
     * Perform a For Comprehension over a Unrestricted, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Computationss.
     *
     *
     * @param value1 top level Unrestricted
     * @param value2 Nested Unrestricted
     * @param value3 Nested Unrestricted
     * @param value4 Nested Unrestricted
     * @return Resulting Unrestricted
     */
    public  < T2, T3, R1, R2, R3> Unrestricted<R3> forEach4(Function<? super T, ? extends Unrestricted<R1>> value2,
                                                            BiFunction<? super T, ? super R1, ? extends Unrestricted<R2>> value3,
                                                            Function3<? super T, ? super R1, ? super R2, ? extends Unrestricted<R3>> value4
                                                                       ) {

        return this.flatMap(in -> {

            Unrestricted<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Unrestricted<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Unrestricted<R3> c = value4.apply(in,ina,inb);
                    return c;
                });

            });

        });

    }


    /**
     * Perform a For Comprehension over a Unrestricted, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Computationss.
     *
     *
     * @param value1 top level Unrestricted
     * @param value2 Nested Unrestricted
     * @param value3 Nested Unrestricted
     * @return Resulting Unrestricted
     */
    public <T2, R1, R2> Unrestricted<R2> forEach3(Function<? super T, ? extends Unrestricted<R1>> value2,
                                                  BiFunction<? super T, ? super R1, ? extends Unrestricted<R2>> value3) {

        return this.flatMap(in -> {

            Unrestricted<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Unrestricted<R2> b = value3.apply(in,ina);
                return b;
            });


        });

    }


    /**
     * Perform a For Comprehension over a Unrestricted, accepting a generating function.
     * This results in a two level nested internal iteration over the provided Computationss.
     *
     *
     * @param value1 top level Unrestricted
     * @param value2 Nested Unrestricted
     * @return Resulting Unrestricted
     */
    public  <R1> Unrestricted<R1> forEach2(Function<? super T, Unrestricted<R1>> value2) {


        return this.flatMap(in -> {

            Unrestricted<R1> a = value2.apply(in);
            return a;
        });



    }


    public static <T> Unrestricted<T> liftF(final Transformable<T> functor){

        return new Suspend<T>(functor.map(Unrestricted::done));
    }



    public static <T> Unrestricted<T> done(final T t){
        return new Pure<>(t);
    }


    public static <B> Unrestricted<B> suspend(final Transformable<Unrestricted<B>> b) {
        return new Suspend<>(b);
    }


    public final T go(final Function<? super Transformable<Unrestricted<T>>,? extends Unrestricted<T>> fn){
        Unrestricted<T> toUse = this;
        for(;;) {
            Xor<Transformable<Unrestricted<T>>, T> xor = (Xor)toUse.resume();
            if (xor.isPrimary())
                return xor.orElse(null);
            toUse =  fn.apply(xor.secondaryOrElse(null));
        }
    }


    public abstract <R> R visit(Function<? super Pure<T>, ? extends R> done,
                            Function<? super Suspend<T>, ? extends R> suspend,
                            Function<? super FlatMapped<?,T>,? extends R> flatMapped);



    public abstract <R> Unrestricted<R> flatMap(final Function<? super T,? extends Unrestricted<? extends R>> f);

    public final <R> Xor<R, T> resume(Function<Transformable<Unrestricted<T>>,R> decoder) {
        return resume().secondaryMap(decoder);
    }

    public  <B> Unrestricted<Tuple2<T,B>> zip(Unrestricted<B> b){
        return zip(b,(x,y)->Tuple.tuple(x,y));
    }
    public  <B,R> Unrestricted<R> zip(Unrestricted<B> b,BiFunction<? super T,? super B,? extends R> zipper){

        Xor<Transformable<Unrestricted<T>>, T> first = resume();
        Xor<Transformable<Unrestricted<B>>, B> second = b.resume();

        if(first.isSecondary() && second.isSecondary()) {
            return suspend(first.secondaryOrElse(null).map(a1->{
               return suspend(second.secondaryOrElse(null).map(b1->a1.zip(b1,zipper)));
            }));
        }
        if(first.isPrimary() && second.isPrimary()){
            return done(zipper.apply(first.orElse(null),second.orElse(null)));
        }
        if(first.isSecondary() && second.isPrimary()){
            return suspend(first.secondaryOrElse(null).map(a1->a1.zip(b,zipper)));

        }
        if(first.isPrimary() && second.isSecondary()){
            return suspend(second.secondaryOrElse(null).map(a1->this.zip(b,zipper)));
        }
        return null;
    }
    public  <B,C> Unrestricted<Tuple3<T,B,C>> zip(Unrestricted<B> b, Unrestricted<C> c){
        return zip(b,c,(x,y,z)->Tuple.tuple(x,y,z));

    }
    public  <B,C,R> Unrestricted<R> zip(Unrestricted<B> b, Unrestricted<C> c, Function3<? super T, ? super B, ? super C,? extends R> fn){

        Xor<Transformable<Unrestricted<T>>,T> first = resume();
        Xor<Transformable<Unrestricted<B>>,B> second = b.resume();
        Xor<Transformable<Unrestricted<C>>,C> third = c.resume();

        if(first.isSecondary() && second.isSecondary() && third.isSecondary()) {
            return suspend(first.secondaryOrElse(null).map(a1->{
                return suspend(second.secondaryOrElse(null).map(b1->{
                    return suspend(third.secondaryOrElse(null).map(c1->a1.zip(b1,c1,fn)));
                }));
            }));
        }

        if(first.isPrimary() && second.isPrimary() && third.isPrimary()){
            return done(fn.apply(first.orElse(null),second.orElse(null),third.orElse(null)));
        }

        if(first.isSecondary() && second.isPrimary() && third.isPrimary()){
            return suspend(first.secondaryOrElse(null).map(a1->a1.zip(b,c,fn)));
        }
        if(first.isPrimary() && second.isSecondary() && third.isPrimary()){

                return suspend(second.secondaryOrElse(null).map(b1->this.zip(b1,c,fn)));



        }
        if(first.isPrimary() && second.isPrimary() && third.isSecondary()){
              return suspend(third.secondaryOrElse(null).map(c1->this.zip(b,c1,fn)));
        }


        if(first.isPrimary() && second.isSecondary() && third.isSecondary()){
            return suspend(second.secondaryOrElse(null).map(b1->{
                return suspend(third.secondaryOrElse(null).map(c1->this.zip(b1,c1,fn)));
            }));

        }
        if(first.isSecondary() && second.isPrimary() && third.isSecondary()){
            return suspend(first.secondaryOrElse(null).map(a1->{

                    return suspend(third.secondaryOrElse(null).map(c1->a1.zip(b,c1,fn)));

            }));
        }
        if(first.isSecondary() && second.isSecondary() && third.isPrimary()){
            return suspend(first.secondaryOrElse(null).map(a1->{
                return suspend(second.secondaryOrElse(null).map(b1->a1.zip(b1,c,fn)));

            }));
        }
        //unreachable
        return null;
    }

    /*
     * Functor and HKT decoder for Free
     */
    @AllArgsConstructor
    static class FreeF<T>{

        Transformable<T> functor;
        Function<Transformable<Unrestricted<?>>,?> decoder1;

        private <R,X> Function<Transformable<Unrestricted<R>>,X> decoder(){
            return (Function)decoder1;
        }
        public final <R1,R2,X1,X2> Tuple2<Xor<X1,R1>,Xor<X2,R2>> product(Unrestricted<R1> free1, Unrestricted<R2> free2 ){

            return Tuple.tuple(free1.resume(decoder()),free2.resume(decoder()));

        }

    }


    public static final <F,R1,R2,X1,X2> Tuple2<Xor<X1,R1>,Xor<X2,R2>> product(Unrestricted<R1> free1, Function<Transformable<Unrestricted<R1>>,X1> decoder1,
                                                                              Unrestricted<R2> free2, Function<Transformable<Unrestricted<R2>>,X2> decoder2 ){

        return Tuple.tuple(free1.resume(decoder1),free2.resume(decoder2));

    }
    public final Xor<Transformable<Unrestricted<T>>, T> resume() {
        return resumeInternal().visit(Xor::secondary,Xor::primary,t->null);

    }
   abstract <T1, U> Either3<Transformable<Unrestricted<T>>, T, Unrestricted<T>> resumeInternal();

    public final <R> Unrestricted<R> map(final Function<? super T, ? extends R> mapper) {
        return flatMap(t -> new Pure<>(mapper.apply(t)));
    }

    private static class Pure<T> extends Unrestricted<T> {

        private final T value;

        private Pure(final T value) {
            this.value = value;
        }
        @Override
        public <R> R visit(Function<? super Pure<T>, ? extends R> done,
                                    Function<? super Suspend<T>, ? extends R> suspend,
                                    Function<? super FlatMapped<?, T>,? extends R> flatMapped){
            return done.apply(this);
        }
        <T1, U> Either3<Transformable<Unrestricted<T>>, T, Unrestricted<T>> resumeInternal(){
            return Either3.left2(value);
        }
        @Override
        public <R> Unrestricted<R> flatMap(Function<? super T, ? extends Unrestricted<? extends R>> f) {
            return new FlatMapped<>(this, f);
        }
    }

    private static final class Suspend<T> extends Unrestricted<T> {
        private final Transformable<Unrestricted<T>> suspended;

        private Suspend(final Transformable<Unrestricted<T>> suspended) {
            this.suspended = suspended;
        }
        @Override
        public <R> R visit(Function<? super Pure<T>, ? extends R> done,
                           Function<? super Suspend<T>, ? extends R> suspend,
                           Function<? super FlatMapped<?, T>,? extends R> flatMapped){
            return suspend.apply(this);
        }
        <T1, U> Either3<Transformable<Unrestricted<T>>, T, Unrestricted<T>> resumeInternal(){
            return Either3.left1(suspended);
        }
        @Override
        public <R> Unrestricted<R> flatMap(Function<? super T,? extends Unrestricted<? extends R>> f) {
            return new FlatMapped<>(this, f);
        }
    }

    private static final class FlatMapped<IN, T> extends Unrestricted<T> {
        private final Unrestricted<IN> free;
        private final Function<? super IN, ? extends Unrestricted<? extends T>> fn;

        private FlatMapped(final Unrestricted<IN> free, final Function<? super IN,? extends Unrestricted<? extends T>> fn){
            this.free = free;
            this.fn = fn;
        }

        private Function<IN, Unrestricted<T>> narrowFn(){
            return (Function<IN, Unrestricted<T>>)fn;
        }
        @Override
        public <R> R visit(Function<? super Pure<T>, ? extends R> done,
                           Function<? super Suspend<T>, ? extends R> suspend,
                           Function<? super FlatMapped<?, T>,? extends R> flatMapped){
            return flatMapped.apply(this);
        }
        @Override
        public <R> Unrestricted<R> flatMap(final Function<? super T,? extends Unrestricted<? extends R>> g) {
            return new FlatMapped<IN, R>(free, aa -> new FlatMapped<T, R>(narrowFn().apply(aa), g));
        }
        <T1, U> Either3<Transformable<Unrestricted<T>>, T, Unrestricted<T>> resumeInternal(){
            return   resumeNestedFree().flatMap(cur->cur.resumeInternal());
        }
        private  <U> Either3<Transformable<Unrestricted<T>>, T, Unrestricted<T>> resumeNestedFree(){
            Function<IN, Unrestricted<T>> f = narrowFn();
            return free.visit(pure->Either3.right(f.apply(pure.value)),
                    s-> Either3.left1(s.suspended.map(o -> o.flatMap(f))),
                    fm->{
                        final FlatMapped<U, IN> flatMapped2 = (FlatMapped<U, IN>)fm;
                        return Either3.right(flatMapped2.free.flatMap(o ->
                                flatMapped2.fn.apply(o).flatMap(fn)));
                    });

        }


    }

}

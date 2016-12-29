package cyclops.free;

import com.aol.cyclops.hkt.Higher;

import com.aol.cyclops.types.mixins.Printable;
import cyclops.control.Xor;
import cyclops.control.either.Either;
import cyclops.control.either.Either3;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Fn5;
import cyclops.function.Reader;
import cyclops.typeclasses.functor.Functor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Free monad for cyclops
 *
 * Inspiration and influences by https://github.com/xuwei-k/free-monad-java/blob/master/src/main/java/free/Free.java
 * and http://www.slideshare.net/kenbot/running-free-with-the-monads
 * and https://github.com/scalaz/scalaz/blob/series/7.2.x/core/src/main/scala/scalaz/Free.scala
 * and https://github.com/typelevel/cats/blob/master/free/src/main/scala/cats/free/Free.scala
 *
 * Org attempt : https://github.com/aol/cyclops/blob/v4.0.1/cyclops-free-monad/src/main/java/com/aol/cyclops/monad/Free.java
 *
 * @param <F> Functor type
 * @param <T> Data type of Functor
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class Free<F, T> implements Printable {

    public <R1, R2, R3,R4,R5> Free<F,R5> forEach6(Function<? super T, ? extends Free<F,R1>> value2,
                                               BiFunction<? super T, ? super R1, ? extends Free<F,R2>> value3,
                                               Fn3<? super T, ? super R1, ? super R2, ? extends Free<F,R3>> value4,
                                               Fn4<? super T, ? super R1, ? super R2,? super R3, ? extends Free<F,R4>> value5,
                                                  Fn5<? super T, ? super R1, ? super R2,? super R3, ? super R4, ? extends Free<F,R5>> value6
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
                                            Fn3<? super T, ? super R1, ? super R2, ? extends Free<F,R3>> value4,
                                               Fn4<? super T, ? super R1, ? super R2,? super R3, ? extends Free<F,R4>> value5
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
                                                   Fn3<? super T, ? super R1, ? super R2, ? extends Free<F,R3>> value4
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
                return xor.get();
            toUse =  fn.apply(xor.secondaryGet());
        }
    }


    public abstract <R> R visit(Function<? super Pure<F,T>, ? extends R> done,
                            Function<? super Suspend<F,T>, ? extends R> suspend,
                            Function<? super FlatMapped<F,?,T>,? extends R> flatMapped);



    public abstract <R> Free<F, R> flatMap(final Function<? super T,? extends Free<F, ? extends R>> f);

    public final <R> Xor<R, T> resume(final Functor<F> functor, Function<Higher<F,Free<F,T>>,R> decoder) {
        return resume(functor).secondaryMap(decoder);
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

}

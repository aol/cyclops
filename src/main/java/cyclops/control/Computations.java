package cyclops.control;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Transformable;
import cyclops.control.either.Either3;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Fn5;
import cyclops.typeclasses.functor.Functor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Java friendly version of Free monad for cyclops2
 * also see {@link cyclops.typeclasses.free.Free} for a more advanced type safe version
 * 
 * Inspiration and heavily influenced by https://github.com/xuwei-k/free-monad-java/blob/master/src/main/java/free/Free.java
 * Other influences incl :- http://www.slideshare.net/kenbot/running-free-with-the-monads
 * and https://github.com/scalaz/scalaz/blob/series/7.2.x/core/src/main/scala/scalaz/Free.scala
 * and https://github.com/typelevel/cats/blob/master/free/src/main/scala/cats/free/Free.scala
 *
 * Org attempt : https://github.com/aol/cyclops/blob/v4.0.1/cyclops-free-monad/src/main/java/com/aol/cyclops/monad/Free.java
 *
 * @param <F> Transformable type
 * @param <T> Data type of Transformable
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class Computations<T> {


    public static <T> Computations<T> liftF(final Transformable<T> functor){

        return new Suspend<T>(functor.map(Computations::done));
    }



    public static <T> Computations<T> done(final T t){
        return new Pure<>(t);
    }


    public static <B> Computations<B> suspend(final Transformable<Computations<B>> b) {
        return new Suspend<>(b);
    }


    public final T go(final Function<? super Transformable<Computations<T>>,? extends Computations<T>> fn){
        Computations<T> toUse = this;
        for(;;) {
            Xor<Transformable<Computations<T>>, T> xor = (Xor)toUse.resume();
            if (xor.isPrimary())
                return xor.get();
            toUse =  fn.apply(xor.secondaryGet());
        }
    }


    public abstract <R> R visit(Function<? super Pure<T>, ? extends R> done,
                            Function<? super Suspend<T>, ? extends R> suspend,
                            Function<? super FlatMapped<?,T>,? extends R> flatMapped);



    public abstract <R> Computations<R> flatMap(final Function<? super T,? extends Computations<? extends R>> f);

    public final <R> Xor<R, T> resume(Function<Transformable<Computations<T>>,R> decoder) {
        return resume().secondaryMap(decoder);
    }



    /*
     * Functor and HKT decoder for Free
     */
    @AllArgsConstructor
    static class FreeF<T>{

        Transformable<T> functor;
        Function<Transformable<Computations<?>>,?> decoder1;

        private <R,X> Function<Transformable<Computations<R>>,X> decoder(){
            return (Function)decoder1;
        }
        public final <R1,R2,X1,X2> Tuple2<Xor<X1,R1>,Xor<X2,R2>> product(Computations<R1> free1, Computations<R2> free2 ){

            return Tuple.tuple(free1.resume(decoder()),free2.resume(decoder()));

        }

    }


    public static final <F,R1,R2,X1,X2> Tuple2<Xor<X1,R1>,Xor<X2,R2>> product(Computations<R1> free1, Function<Transformable<Computations<R1>>,X1> decoder1,
                                                                              Computations<R2> free2, Function<Transformable<Computations<R2>>,X2> decoder2 ){

        return Tuple.tuple(free1.resume(decoder1),free2.resume(decoder2));

    }
    public final Xor<Transformable<Computations<T>>, T> resume() {
        return resumeInternal().visit(Xor::secondary,Xor::primary,t->null);

    }
   abstract <T1, U> Either3<Transformable<Computations<T>>, T, Computations<T>> resumeInternal();

    public final <R> Computations<R> map(final Function<? super T, ? extends R> mapper) {
        return flatMap(t -> new Pure<>(mapper.apply(t)));
    }

    private static class Pure<T> extends Computations<T> {

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
        <T1, U> Either3<Transformable<Computations<T>>, T, Computations<T>> resumeInternal(){
            return Either3.left2(value);
        }
        @Override
        public <R> Computations<R> flatMap(Function<? super T, ? extends Computations<? extends R>> f) {
            return new FlatMapped<>(this, f);
        }
    }

    private static final class Suspend<T> extends Computations<T> {
        private final Transformable<Computations<T>> suspended;

        private Suspend(final Transformable<Computations<T>> suspended) {
            this.suspended = suspended;
        }
        @Override
        public <R> R visit(Function<? super Pure<T>, ? extends R> done,
                           Function<? super Suspend<T>, ? extends R> suspend,
                           Function<? super FlatMapped<?, T>,? extends R> flatMapped){
            return suspend.apply(this);
        }
        <T1, U> Either3<Transformable<Computations<T>>, T, Computations<T>> resumeInternal(){
            return Either3.left1(suspended);
        }
        @Override
        public <R> Computations<R> flatMap(Function<? super T,? extends Computations<? extends R>> f) {
            return new FlatMapped<>(this, f);
        }
    }

    private static final class FlatMapped<IN, T> extends Computations<T> {
        private final Computations<IN> free;
        private final Function<? super IN, ? extends Computations<? extends T>> fn;

        private FlatMapped(final Computations<IN> free, final Function<? super IN,? extends Computations<? extends T>> fn){
            this.free = free;
            this.fn = fn;
        }

        private Function<IN, Computations<T>> narrowFn(){
            return (Function<IN, Computations<T>>)fn;
        }
        @Override
        public <R> R visit(Function<? super Pure<T>, ? extends R> done,
                           Function<? super Suspend<T>, ? extends R> suspend,
                           Function<? super FlatMapped<?, T>,? extends R> flatMapped){
            return flatMapped.apply(this);
        }
        @Override
        public <R> Computations<R> flatMap(final Function<? super T,? extends Computations<? extends R>> g) {
            return new FlatMapped<IN, R>(free, aa -> new FlatMapped<T, R>(narrowFn().apply(aa), g));
        }
        <T1, U> Either3<Transformable<Computations<T>>, T, Computations<T>> resumeInternal(){
            return   resumeNestedFree().flatMap(cur->cur.resumeInternal());
        }
        private  <U> Either3<Transformable<Computations<T>>, T, Computations<T>> resumeNestedFree(){
            Function<IN, Computations<T>> f = narrowFn();
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

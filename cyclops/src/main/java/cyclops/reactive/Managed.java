package cyclops.reactive;

import com.oath.cyclops.hkt.DataWitness.managed;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.data.tuple.Tuple5;
import cyclops.data.tuple.Tuple6;
import cyclops.data.tuple.Tuple7;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/*
 * Resource management monad (see https://www.iravid.com/posts/resource-management.html)
 *
 * <pre>
 *     Managed.of(this::acquireResource)
               .map(r->r.use())
               .run()

   //Acquire and automatically close a resource (in this example the Resource is autoclosable)


   </pre>
 *
 * Working with Hibernate
 *
 * <pre>
 *
  *      SessionFactory factory;
 *
 *       Try<String, Throwable> res = Managed.of(factory::openSession)
                                             .with(Session::beginTransaction)
                                             .map((session, tx) ->

                                                     deleteFromMyTable(session)
                                                              .bipeek(success -> tx.commit(),error -> tx.rollback())


                                                  )
                                             .foldRun(Try::flatten);


          public Try<String,Throwable> deleteFromMyTable(Session s);

     </pre>
 *
 *
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public  abstract class Managed<T> implements Higher<managed,T>,To<Managed<T>>, Publisher<T>{



    public static <T> Managed<T> managed(T acq, Consumer<T> cleanup){
        return of(IO.of(()->acq),cleanup);
    }
    public static <T extends AutoCloseable> Managed<T> managed(T acq){
        return of(IO.of(()->acq), ExceptionSoftener.softenConsumer(c->c.close()));
    }
    public static <T> Managed<T> of(Publisher<T> acq, Consumer<T> cleanup){
        return of(IO.fromPublisher(acq),cleanup);
    }
    public static <T> Managed<T> of(Supplier<T> acq, Consumer<T> cleanup){
        return of(IO.of(acq),cleanup);
    }
    public static <T extends AutoCloseable> Managed<T> of(Supplier<T> acq){
        return of(IO.of(acq),ExceptionSoftener.softenConsumer(c->c.close()));
    }
    public static <T extends AutoCloseable> Managed<T> of(Publisher<T> acq){
        return of(IO.fromPublisher(acq),ExceptionSoftener.softenConsumer(c->c.close()));
    }

    public static <T> Managed<T> of(IO<T> acquire, Consumer<T> cleanup){

        return new Managed<T>(){
            public  <R> IO<R> apply(Function<? super T,? extends IO<R>> fn){
                IO<R> y = IO.Comprehensions.forEach(acquire, t1 -> {
                    IO<? extends Try<? extends IO<R>, Throwable>> res1 = IO.withCatch(() -> fn.apply(t1), Throwable.class);
                    return res1;
                }, t2 -> {

                    Try<? extends IO<R>, Throwable> tr = t2._2();
                    IO<R> res = tr.fold(r -> r, e -> IO.fromPublisher(Future.ofError(e)));
                    cleanup.accept(t2._1());

                    return res;
                });
                return y;
            }
        };
    }

    public static <T extends AutoCloseable> Managed<T> of(IO<T> acquire){
        return of(acquire,ExceptionSoftener.softenConsumer(c->c.close()));
    }

    public static  <T> Managed<Seq<T>> sequence(Iterable<? extends Managed<T>> all) {

        Managed<Seq<T>> acc =null;
        for(Managed<T> n : all){
           if(acc==null)
                acc=n.map(Seq::of);
            else
               acc = acc.zip(n,(a,b)->a.append(b));

        }
        return acc;

    }

    public <R2> Tupled<T,R2> with(Function<? super T, ? extends R2> fn){
        return new Tupled<>(map(i->Tuple.tuple(i,fn.apply(i))));
    }
    public <R1,R2> Tupled<R1,R2> tupled(Function<? super T, ? extends Tuple2<R1,R2>> fn){
        return new Tupled<>(map(fn));
    }
    public <R1,R2,R3> Tupled3<R1,R2,R3> tupled3(Function<? super T, ? extends Tuple3<R1,R2,R3>> fn){
        return new Tupled3<>(map(fn));
    }
    @AllArgsConstructor(access =  AccessLevel.PRIVATE)
    public static class Tupled<T1,T2>{

        private final Managed<Tuple2<T1,T2>> managed;

        public <R> Tupled3<T1,T2,R> with(BiFunction<? super T1,? super T2, ? extends R> fn){
            return new Tupled3(map((a,b)->Tuple.tuple(a,b,fn.apply(a,b))));
        }

        public final <R> Managed<R> flatMap(BiFunction<? super T1,? super T2, Managed<R>> f) {
            return managed.flatMap(t2->f.apply(t2._1(),t2._2()));
        }
        public final <R> Managed<R> map(BiFunction<? super T1,? super T2, ? extends R> f){
            return managed.map(t2->f.apply(t2._1(),t2._2()));
        }

        public <R1,R2> Tupled<R1,R2> mapTupled(BiFunction<? super T1,? super T2, Tuple2<R1,R2>> fn){
            return new Tupled<>(map(fn));
        }


        public Managed<Tuple2<T1,T2>> managed(){
            return managed;
        }
        public final Try<Tuple2<T1,T2>,Throwable> run() {
            return  managed.run();
        }
        public final <R> R foldRun(Function<? super Try<Tuple2<T1,T2>,Throwable>,? extends R> transform) {
            return  managed.foldRun(transform);
        }


    }
    @AllArgsConstructor(access =  AccessLevel.PRIVATE)
    public static class Tupled3<T1,T2,T3>{

        private final Managed<Tuple3<T1,T2,T3>> managed;


        public final <R> Managed<R> flatMap(Function3<? super T1,? super T2,? super T3, Managed<R>> f) {
            return managed.flatMap(t3->f.apply(t3._1(),t3._2(),t3._3()));
        }
        public final <R> Managed<R> map(Function3<? super T1,? super T2,? super T3, ? extends R> f){
            return managed.map(t3->f.apply(t3._1(),t3._2(),t3._3()));
        }

        public <R1,R2,R3> Tupled3<R1,R2,R3> mapTupled(Function3<? super T1,? super T2,? super T3,  Tuple3<R1,R2,R3>> fn){
            return new Tupled3<>(map(fn));
        }


        public Managed<Tuple3<T1,T2,T3>> managed(){
            return managed;
        }
        public final Try<Tuple3<T1,T2,T3>,Throwable> run() {
            return  managed.run();
        }
        public final <R> R foldRun(Function<? super Try<Tuple3<T1,T2,T3>,Throwable>,? extends R> transform) {
            return  managed.foldRun(transform);
        }


    }
    public static <T,R> Managed<Seq<R>> traverse(Iterable<T> stream,Function<? super T,Managed<? extends R>> fn) {
        Seq<Managed<R>> s = Seq.fromIterable(stream).map(fn.andThen(Managed::narrow));
        return sequence(s);
    }

    public static <T,R> Managed<Seq<R>> traverse(Function<? super T,? extends R> fn, Iterable<Managed<T>> stream) {
        Seq<Managed<R>> s = Seq.fromIterable(stream)
                                               .map(j->j.map(fn));
        return sequence(s);
    }
    public static <T> Managed<T> narrow(Managed<? extends T> broad){
        return  (Managed<T>)broad;
    }
    public final  <T2,R> Managed<R> zip(Managed<T2> b, BiFunction<? super T,? super T2,? extends R> fn){
        return flatMap(t1 -> b.map(t2 -> fn.apply(t1, t2)));
    }
    abstract  <R> IO<R> apply(Function<? super T,? extends IO<R>> fn);

    public final void forEach(Consumer<? super T> onNext,Consumer<Throwable> errorHandler){
        stream().forEach(onNext,errorHandler);

    }

    public final Try<T,Throwable> run() {


      return  io().run();
    }
    public final T runAndThrowUnexpected() {
        return run().fold(t->t,e->{
            throw ExceptionSoftener.throwSoftenedException(e);
        });
    }

    public final <R> R foldRun(Function<? super Try<T,Throwable>,? extends R> transform) {

        return  transform.apply(io().run());
    }

    public final Future<T> future(){
        return io().future();
    }

    public final Try<T,Throwable> runAsync(Executor ex) {
        return  io().runAsync(ex);
    }

    public ReactiveSeq<T> stream(){
        return io().stream();
    }

    public final <R> Managed<R> map(Function<? super T, ? extends R> mapper){
        return of(apply(mapper.andThen(IO::of)),__->{});
    }
    public final <R> Managed<R> flatMap(Function<? super T, Managed<R>> f){

       Managed<T> m = this;
        return new Managed<R>(){

            @Override
            public <R1> IO<R1> apply(Function<? super R, ? extends IO<R1>> fn) {
                IO<R1> x = m.apply(r1 -> {
                    IO<R1> r = f.apply(r1).apply(r2 -> fn.apply(r2));
                    return r;
                });
                return x;
            }
        };

    }


    public final static <T> Semigroup<Managed<T>> semigroup(Semigroup<T> s){
        return (a,b) -> a.flatMap(t1 -> b.map(t2 -> s.apply(t1, t2)));
    }
    public final static <T> Monoid<Managed<T>> monoid(Monoid<T> s){
        return Monoid.of(managed(s.zero(),__->{}),semigroup(s));
    }


    @Override
    public void subscribe(Subscriber<? super T> s) {
        stream().subscribe(s);
    }

    public IO<T> io() {
        return apply(IO::of);
    }

    public static class Comprehensions {


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

}

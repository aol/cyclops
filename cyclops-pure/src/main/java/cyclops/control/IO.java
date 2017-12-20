package cyclops.control;

import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.foldable.To;
import cyclops.data.tuple.*;
import cyclops.function.Function3;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IO<T> implements To<IO<T>>, Higher<io,T>, Publisher<T> {
  private final Publisher<T> fn;

  public static <T> IO<T> of(Supplier<? extends T> s){
    return new IO<T>(Eval.narrow(Eval.later(s)));
  }
  public static <T> IO<T> of(Supplier<? extends T> s,Executor ex){
    return new IO<T>(Future.narrow(Future.of(s,ex)));
  }

  public static <T> IO<T> fromPublisher(Publisher<T> p){
    return new IO<T>(p);
  }

  public static <T,X extends Throwable> IO<Try<T,X>> withCatch(final Try.CheckedSupplier<T, X> cf, final Class<? extends X>... classes){
    return of(()-> Try.withCatch(cf));
  }

  public <R> IO<R> map(Function<? super T, ? extends R> s){
    return fromPublisher(Spouts.from(fn).map(t->s.apply(t)));
  }

  public <R> IO<R> flatMap(Function<? super T, ? extends IO<? extends R>> s){
     return fromPublisher(Spouts.from(fn).mergeMap(t->s.apply(t).publisher()));
  }

  public static <T,X extends Throwable> IO<T> recover(IO<Try<T, X>> io, Supplier<? extends T> s){
     return io.map(t->t.visit(i->i,s));
  }

  public <R> IO<Try<R, Throwable>> mapTry(Function<? super T, ? extends R> s){
    return map(t->Try.withCatch(()->s.apply(t)));
  }
  public <R,X extends Throwable> IO<Try<R, X>> mapTry(Function<? super T, ? extends R> s,final Class<? extends X>... classes){
    return map(t->Try.withCatch(()->s.apply(t),classes));
  }
  public void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete){
    Spouts.from(fn).forEach(consumerElement,consumerError,onComplete);
  }
  public Try<T,Throwable> run(){
    return  Future.fromPublisher(fn)
                  .get();
  }

  public Publisher<T> publisher(){
    return fn;
  }
  @Override
  public final void subscribe(final Subscriber<? super T> sub) {
    fn.subscribe(sub);
  }

  public ReactiveSeq<T> stream(){
    return Spouts.from(fn);
  }

  public Try<T,Throwable> runAsync(Executor e){
    return Try.fromPublisher(Future.of(() -> run(), e))
              .flatMap(Function.identity());
  }

  public static <T1,T2,R> IO<R> merge(Publisher<T1> p1, Publisher<T2> p2,BiFunction<? super T1, ? super T2,? extends R> fn2){
    ReactiveSeq<T1> s1 = Spouts.from(p1);
    ReactiveSeq<T2> s2 = Spouts.from(p2);
    return fromPublisher(s1.zip(fn2,s2));
  }
  public static <T1,T2,T3,R> IO<R> merge(Publisher<T1> p1, Publisher<T2> p2,Publisher<T3> p3,Function3<? super T1, ? super T2,? super T3,? extends R> fn3){
     return merge(merge(p1,p2,Tuple::tuple),p3,(t2,c)->fn3.apply(t2._1(),t2._2(),c));
  }
  public static class Comprehensions {

    public static <T,F,R1, R2, R3,R4,R5,R6,R7> IO<R7> forEach(IO<T> io,
                                                                        Function<? super T, ? extends IO<R1>> value2,
                                                                        Function<? super Tuple2<? super T,? super R1>, ? extends IO<R2>> value3,
                                                                        Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends IO<R3>> value4,
                                                                        Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends IO<R4>> value5,
                                                                        Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends IO<R5>> value6,
                                                                        Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends IO<R6>> value7,
                                                                        Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends IO<R7>> value8
    ) {

      return io.flatMap(in -> {

        IO<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          IO<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            IO<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              IO<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                IO<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  IO<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f.flatMap(inf->{
                    IO<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                    return g;

                  });

                });
              });

            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4,R5,R6> IO<R6> forEach(IO<T> io,
                                                                     Function<? super T, ? extends IO<R1>> value2,
                                                                     Function<? super Tuple2<? super T,? super R1>, ? extends IO<R2>> value3,
                                                                     Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends IO<R3>> value4,
                                                                     Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends IO<R4>> value5,
                                                                     Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends IO<R5>> value6,
                                                                     Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends IO<R6>> value7
    ) {

      return io.flatMap(in -> {

        IO<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          IO<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            IO<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              IO<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                IO<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  IO<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f;
                });
              });

            });

          });


        });


      });

    }

    public static <T,F,R1, R2, R3,R4,R5> IO<R5> forEach(IO<T> io,
                                                                  Function<? super T, ? extends IO<R1>> value2,
                                                                  Function<? super Tuple2<? super T,? super R1>, ? extends IO<R2>> value3,
                                                                  Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends IO<R3>> value4,
                                                                  Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends IO<R4>> value5,
                                                                  Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends IO<R5>> value6
    ) {

      return io.flatMap(in -> {

        IO<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          IO<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            IO<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              IO<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                IO<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e;
              });
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4> IO<R4> forEach(IO<T> io,
                                                               Function<? super T, ? extends IO<R1>> value2,
                                                               Function<? super Tuple2<? super T,? super R1>, ? extends IO<R2>> value3,
                                                               Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends IO<R3>> value4,
                                                               Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends IO<R4>> value5

    ) {

      return io.flatMap(in -> {

        IO<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          IO<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            IO<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              IO<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d;
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3> IO<R3> forEach(IO<T> io,
                                                            Function<? super T, ? extends IO<R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, ? extends IO<R2>> value3,
                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends IO<R3>> value4

    ) {

      return io.flatMap(in -> {

        IO<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          IO<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            IO<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c;

          });


        });


      });

    }
    public static <T,F,R1, R2> IO<R2> forEach(IO<T> io,
                                                        Function<? super T, ? extends IO<R1>> value2,
                                                        Function<? super Tuple2<? super T,? super R1>, ? extends IO<R2>> value3

    ) {

      return io.flatMap(in -> {

        IO<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          IO<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b;


        });


      });

    }
    public static <T,F,R1> IO<R1> forEach(IO<T> io,
                                                    Function<? super T, ? extends IO<R1>> value2


    ) {

      return io.flatMap(in -> {

        IO<R1> a = value2.apply(in);
        return a;


      });

    }


  }



}

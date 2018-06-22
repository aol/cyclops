package cyclops.control;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.matching.Deconstruct;
import cyclops.data.tuple.*;
import com.oath.cyclops.hkt.DataWitness.identity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Identity<T> implements Higher<identity,T>, Iterable<T>, Deconstruct.Deconstruct1<T>, Serializable {
    private static final long serialVersionUID = 1L;
    public final T value;


    public static  <T,R> Identity<R> tailRec(T initial, Function<? super T, ? extends Identity<? extends Either<T, R>>> fn){
        Identity<? extends Either<T, R>> next[] = new Identity[1];
        next[0] = Identity.of(Either.left(initial));
        boolean cont = true;
        do {

            cont = next[0].fold(p -> p.fold(s -> {
                next[0] = narrowK(fn.apply(s));
                return true;
            }, __ -> false));
        } while (cont);
        return next[0].map(x->x.fold(l->null, r->r));
    }
    public static <T> Identity<T> of(T value){
         return new Identity<>(value);
     }
    public static <T> Identity<T> fromTuple(Tuple1<T> t1){
      return of(t1._1());
    }

     public T get(){
         return value;
     }
     public T extract(){
         return value;
     }
     public <R> R fold(Function<? super T, ? extends R> fn){
         return fn.apply(value);
     }
     public Identity<Identity<T>> nest(){
         return of(this);
     }
     public <R> Identity<R> coflatMap(Function<? super Identity<? super T>, ? extends R> fn){
         return of(fn.apply(this));
     }
    public <R> Identity<R> map(Function<? super T,? extends R> fn){
         return new Identity<>(fn.apply(value));
     }
    public <R> Identity<R> flatMap(Function<? super T,? extends Identity<? extends R>> fn){
        return narrow(fn.apply(value));
    }

    public <T2,R> Identity<R> zip(Identity<? extends T2> id,BiFunction<? super T,? super T2, ? extends R> fn){
      return flatMap(a->id.map(b->fn.apply(a,b)));
    }
    public Tuple1<T> toTuple(){
        return Tuple1.of(value);
    }

    public Tuple1<T> toLazyTuple(){
        return Tuple1.lazy(()->value);
    }



    public static <T> Identity<T> narrow(Identity<? extends T> id){
        return (Identity<T>)id;
    }
    public static <T> Identity<T> narrowK(Higher<identity,T> ds){
        return (Identity<T>)ds;
    }



    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(value).iterator();
    }

    @Override
    public Tuple1<T> unapply() {
        return Tuple.tuple(value);
    }

    public static <T> Higher<identity, T> widen(Identity<T> narrow) {
      return narrow;
    }


  public static class Comprehensions {

    public static <T,F,R1, R2, R3,R4,R5,R6,R7> Identity<R7> forEach(Identity<T> id,
                                                                        Function<? super T, ? extends Identity<R1>> value2,
                                                                        Function<? super Tuple2<? super T,? super R1>, ? extends Identity<R2>> value3,
                                                                        Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Identity<R3>> value4,
                                                                        Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Identity<R4>> value5,
                                                                        Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Identity<R5>> value6,
                                                                        Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Identity<R6>> value7,
                                                                        Function<? super Tuple7<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5, ? super R6>, ? extends Identity<R7>> value8
    ) {

      return id.flatMap(in -> {

        Identity<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Identity<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Identity<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Identity<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Identity<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Identity<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f.flatMap(inf->{
                    Identity<R7> g = value8.apply(Tuple.tuple(in,ina,inb,inc,ind,ine,inf));
                    return g;

                  });

                });
              });

            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4,R5,R6> Identity<R6> forEach(Identity<T> id,
                                                                     Function<? super T, ? extends Identity<R1>> value2,
                                                                     Function<? super Tuple2<? super T,? super R1>, ? extends Identity<R2>> value3,
                                                                     Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Identity<R3>> value4,
                                                                     Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Identity<R4>> value5,
                                                                     Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Identity<R5>> value6,
                                                                     Function<? super Tuple6<T, ? super R1, ? super R2,? super R3, ? super R4, ? super R5>, ? extends Identity<R6>> value7
    ) {

      return id.flatMap(in -> {

        Identity<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Identity<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Identity<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Identity<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Identity<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e.flatMap(ine->{
                  Identity<R6> f = value7.apply(Tuple.tuple(in,ina,inb,inc,ind,ine));
                  return f;
                });
              });

            });

          });


        });


      });

    }

    public static <T,F,R1, R2, R3,R4,R5> Identity<R5> forEach(Identity<T> id,
                                                                  Function<? super T, ? extends Identity<R1>> value2,
                                                                  Function<? super Tuple2<? super T,? super R1>, ? extends Identity<R2>> value3,
                                                                  Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Identity<R3>> value4,
                                                                  Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Identity<R4>> value5,
                                                                  Function<? super Tuple5<T, ? super R1, ? super R2,? super R3, ? super R4>, ? extends Identity<R5>> value6
    ) {

      return id.flatMap(in -> {

        Identity<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Identity<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Identity<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Identity<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d.flatMap(ind->{
                Identity<R5> e = value6.apply(Tuple.tuple(in,ina,inb,inc,ind));
                return e;
              });
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3,R4> Identity<R4> forEach(Identity<T> id,
                                                               Function<? super T, ? extends Identity<R1>> value2,
                                                               Function<? super Tuple2<? super T,? super R1>, ? extends Identity<R2>> value3,
                                                               Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Identity<R3>> value4,
                                                               Function<? super Tuple4<? super T, ? super R1, ? super R2,? super R3>, ? extends Identity<R4>> value5

    ) {

      return id.flatMap(in -> {

        Identity<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Identity<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Identity<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c.flatMap(inc->{
              Identity<R4> d = value5.apply(Tuple.tuple(in,ina,inb,inc));
              return d;
            });

          });


        });


      });

    }
    public static <T,F,R1, R2, R3> Identity<R3> forEach(Identity<T> id,
                                                            Function<? super T, ? extends Identity<R1>> value2,
                                                            Function<? super Tuple2<? super T,? super R1>, ? extends Identity<R2>> value3,
                                                            Function<? super Tuple3<? super T,? super R1,? super R2>, ? extends Identity<R3>> value4

    ) {

      return id.flatMap(in -> {

        Identity<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Identity<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b.flatMap(inb -> {

            Identity<R3> c = value4.apply(Tuple.tuple(in,ina,inb));

            return c;

          });


        });


      });

    }
    public static <T,F,R1, R2> Identity<R2> forEach(Identity<T> id,
                                                        Function<? super T, ? extends Identity<R1>> value2,
                                                        Function<? super Tuple2<? super T,? super R1>, ? extends Identity<R2>> value3

    ) {

      return id.flatMap(in -> {

        Identity<R1> a = value2.apply(in);
        return a.flatMap(ina -> {
          Identity<R2> b = value3.apply(Tuple.tuple(in,ina));
          return b;


        });


      });

    }
    public static <T,F,R1> Identity<R1> forEach(Identity<T> id,
                                                    Function<? super T, ? extends Identity<R1>> value2


    ) {

      return id.flatMap(in -> {

        Identity<R1> a = value2.apply(in);
        return a;


      });

    }


  }

}

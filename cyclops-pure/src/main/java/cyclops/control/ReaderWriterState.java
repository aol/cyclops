package cyclops.control;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher4;
import com.oath.cyclops.hkt.DataWitness.rws;
import com.oath.cyclops.hkt.DataWitness.supplier;

import cyclops.free.Free;
import cyclops.function.*;
import cyclops.kinds.SupplierKind;
import cyclops.typeclasses.functor.Functor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple3;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static cyclops.data.tuple.Tuple.tuple;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReaderWriterState<R,W,S,T> implements Higher4<rws,R,W,S,T>{

    private final Monoid<W> monoid;
    private final BiFunction<R,S, Free<supplier,Tuple3<W, S, T>>> runState;

    public Tuple3<W,S,T> run(R r,S s) {
        return SupplierKind.run(runState.apply(r,s));
    }

    public static <T,R1, W, S, R> ReaderWriterState<R1, W, S, R> tailRec(Monoid<W> monoid,T initial, Function<? super T, ? extends  ReaderWriterState<R1, W, S,  ? extends Either<T, R>>> fn) {

      Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> x = ReaderWriterState.tailRecInternal(monoid,initial, fn);
        return narrowK(x);
    }
    private static  <T,R1, W, S, R> Higher<Higher<Higher<Higher<rws, R1>, W>, S>, R> tailRecInternal(Monoid<W> monoid,T initial, Function<? super T, ? extends Higher<Higher<Higher<Higher<rws, R1>, W>, S>, ? extends Either<T, R>>> fn) {
      return narrowK(fn.apply(initial)).flatMap( eval ->
        eval.visit(s->narrowK(tailRecInternal(monoid,s,fn)),p->{
          ReaderWriterState<R1, W, S, R> k = narrowK(rws((a,s)-> tuple(monoid.zero(),s,p),monoid));
          return k;
        }));
    }

    public  ReaderWriterState<R,W,S,T> tell(W value) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> fn =
                (r,s)->runState.apply(r,s).map(t3-> tuple(monoid.apply(t3._1(),value),t3._2(),t3._3()));

        return suspended(fn,monoid);
    }

    public ReaderWriterState<R,W,S,T> ask() {
         return suspended((r,s) -> runState.apply(r,s).map(t3 -> Tuple.<W,S,T>tuple(monoid.zero(),s,t3._3())),monoid);
    }


    public ReaderWriterState<R,W,S,T> local(Function<? super  R,? extends R> fn) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runFn = (R r, S s) -> runState.apply(fn.apply(r), s);
        return suspended(runFn,monoid);
    }

    public <R2> ReaderWriterState<R,W,S,R2> map(Function<? super T,? extends R2> mapper) {

        return mapState(t -> tuple(t._1(), t._2(), mapper.apply(t._3())));
    }
    public <R2> ReaderWriterState<R,W,S,R2> mapState(Function<Tuple3<W,S,T>, Tuple3<W,S, R2>> fn) {
        return suspended((r,s) -> runState.apply(r,s).map(t3 -> fn.apply(t3)),monoid);
    }
    private static <R,W,S,T> ReaderWriterState<R,W,S,T> suspended(BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runF,
                                                                  Monoid<W> monoid) {

        return new ReaderWriterState<R, W, S, T>(monoid,(R r ,S s) -> SupplierKind.suspend(SupplierKind.λK(()->runF.apply(r,s))));

    }

    public <R2> ReaderWriterState<R,W,S,R2> flatMap(Function<? super T,? extends  ReaderWriterState<R,W,S,R2>> f) {

        return suspended((r,s) -> runState.apply(r, s)
                .flatMap(result -> Free.done(f.apply(result._3())
                                              .run(r, result._2())
                                              .transform((w2,s2,r2)-> tuple(monoid.apply(w2,result._1()),s2,r2)

                ))),monoid);
    }


    public <R1, R2, R3, R4> ReaderWriterState<R,W,S,R4> forEach4(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                                      BiFunction<? super T, ? super R1, ReaderWriterState<R,W,S,R2>> value3,
                                                      Function3<? super T, ? super R1, ? super R2, ReaderWriterState<R,W,S,R3>> value4,
                                                      Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {




        return this.flatMap(in -> {

            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                ReaderWriterState<R,W,S,R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {

                    ReaderWriterState<R,W,S,R3> c = value4.apply(in,ina,inb);
                    return c.map(inc->yieldingFunction.apply(in, ina, inb, inc));

                });


            });


        });

    }




    public <R1, R2, R4> ReaderWriterState<R,W,S,R4> forEach3(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                                  BiFunction<? super T, ? super R1, ReaderWriterState<R,W,S,R2>> value3,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                ReaderWriterState<R,W,S,R2> b = value3.apply(in,ina);
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }

    public <R1, R4> ReaderWriterState<R,W,S,R4> forEach2(Function<? super T, ReaderWriterState<R,W,S,R1>> value2,
                                              BiFunction<? super T, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            ReaderWriterState<R,W,S,R1> a = value2.apply(in);
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });




        });


    }
    public static <R,W,S,T> ReaderWriterState<R,W,S,T> rws(BiFunction<? super R, ? super S,? extends Tuple3<W,S, T>> runF, Monoid<W> monoid) {

      return new ReaderWriterState<R, W, S, T>(monoid,(r,s) -> Free.done(runF.apply(r,s)));
    }

    public static <R,W,S,T> ReaderWriterState<R,W,S,T> narrowK(Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> hkt){
      return (ReaderWriterState<R,W,S,T>)hkt;
    }
    public static <R,W,S,T> Higher<Higher<Higher<Higher<rws, R>, W>, S>, T> widen(ReaderWriterState<R,W,S,T> narrow) {
      return narrow;
    }

}

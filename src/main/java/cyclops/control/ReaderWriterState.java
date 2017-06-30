package cyclops.control;

import cyclops.monads.Witness;
import cyclops.monads.Witness.supplier;
import cyclops.typeclasses.free.Free;
import cyclops.function.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;

import java.util.function.BiFunction;
import java.util.function.Function;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReaderWriterState<R,W,S,T>  {



    private final Monoid<W> monoid;
    private final BiFunction<R,S, Free<supplier,Tuple3<W, S, T>>> runState;

    public Tuple3<W,S,T> run(R r,S s) {
        return Fn0.run(runState.apply(r,s));
    }

    public  ReaderWriterState<R,W,S,T> tell(W value) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> fn =
                (r,s)->runState.apply(r,s).map(t3->Tuple.tuple(monoid.apply(t3.v1,value),t3.v2,t3.v3));

        return suspended(fn,monoid);
    }

    public ReaderWriterState<R,W,S,T> ask() {
         return suspended((r,s) -> runState.apply(r,s).map(t3 -> Tuple.<W,S,T>tuple(monoid.zero(),s,t3.v3)),monoid);
    }


    public ReaderWriterState<R,W,S,T> local(Function<? super  R,? extends R> fn) {
        BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runFn = (R r, S s) -> runState.apply(fn.apply(r), s);
        return suspended(runFn,monoid);
    }

    public <R2> ReaderWriterState<R,W,S,R2> map(Function<? super T,? extends R2> mapper) {

        return mapState(t -> Tuple.tuple(t.v1, t.v2, mapper.apply(t.v3)));
    }
    public <R2> ReaderWriterState<R,W,S,R2> mapState(Function<Tuple3<W,S,T>, Tuple3<W,S, R2>> fn) {
        return suspended((r,s) -> runState.apply(r,s).map(t3 -> fn.apply(t3)),monoid);
    }
    private static <R,W,S,T> ReaderWriterState<R,W,S,T> suspended(BiFunction<? super R, ? super S, Free<supplier,Tuple3<W,S, T>>> runF,
                                                                  Monoid<W> monoid) {

        return new ReaderWriterState<R, W, S, T>(monoid,(R r ,S s) -> Fn0.suspend(Lambda.λK(()->runF.apply(r,s))));

    }

    public <R2> ReaderWriterState<R,W,S,R2> flatMap(Function<? super T,? extends  ReaderWriterState<R,W,S,R2>> f) {

        return suspended((r,s) -> runState.apply(r, s)
                .flatMap(result -> Free.done(f.apply(result.v3)
                                              .run(r, result.v2)
                                              .map((w2,s2,r2)->Tuple.tuple(monoid.apply(w2,result.v1),s2,r2)

                ))),monoid);
    }



    public static <R,W,S,T> ReaderWriterState<R,W,S,T> rws(BiFunction<? super R, ? super S,? extends Tuple3<W,S, T>> runF, Monoid<W> monoid) {

        return new ReaderWriterState<R, W, S, T>(monoid,(r,s) -> Free.done(runF.apply(r,s)));
    }
}

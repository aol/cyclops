package cyclops.control;

import cyclops.free.Free;
import cyclops.function.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;

/**
 * Created by johnmcclean on 05/02/2017.
 */
public class ReaderWriterState<R,W,S,T>  {


    Reader<S, Free<Fn0.SupplierKind.µ,Tuple2<Writer<W, S>, T>>> runState;

    public Tuple2<Writer<W,S>,T> run(S s) {
        return Fn0.run(runState.apply(s));
    }
    public T eval(S s) {
        return Fn0.run(runState.apply(s)).v2;
    }
    public <R> State<S, R> map(Function<? super T,? extends R> mapper) {
        return mapState(t -> Tuple.tuple(t.v1, mapper.apply(t.v2)));
    }
    public <R> State<S, R> mapState(Function<Tuple2<S,T>, Tuple2<S, R>> fn) {
        return suspended(s -> runState.apply(s).map(t -> fn.apply(t.map((w,r)->w.getValue().map2(__->r)))));
    }
    private static <S, T> State<S, T> suspended(Fn1<? super S, Free<Fn0.SupplierKind.µ,Tuple2<S, T>>> runF) {
        return new State<>(s -> Fn0.suspend(Lambda.λK(()->runF.apply(s))));
    }

    public <R> State<S, R> flatMap(Function<? super T,? extends  State<S, R>> f) {
        return suspended(s -> runState.apply(s).flatMap(result -> Free.done(f.apply(result.v2).run(result.v1))));
    }

    public static <R,W,S,T> ReaderWriterState<R,W,S,T> rws(Function<? super S,? extends Tuple2<S, T>> runF, Monoid<W> monoid) {

        return new State<>(s -> Free.done(runF.apply(s)));
    }
}

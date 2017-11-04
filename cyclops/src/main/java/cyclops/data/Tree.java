package cyclops.data;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.matching.Deconstruct.Deconstruct2;
import cyclops.monads.DataWitness.tree;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.function.Function;
import java.util.function.Supplier;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tree<T> implements Deconstruct2<T,LazySeq<Tree<T>>>,Higher<tree,T> {

    public final T head;
    private final LazySeq<Tree<T>> subForest;



    public static <T> Tree<T> of(T head, LazySeq<Tree<T>> subForest) {
        return new Tree<>(head,subForest);
    }
    public static <T> Tree<T> lazy(T head, Supplier<LazySeq<Tree<T>>> subForest) {
        return new Tree<T>(head,null){

            @Override
            public LazySeq<Tree<T>> subForest() {
                return subForest.get();
            }
        };
    }

    public LazySeq<LazySeq<T>> levels() {
        ReactiveSeq<LazySeq<T>> res = ReactiveSeq.iterate(LazySeq.of(this), sf -> sf.flatMap(a -> a.subForest()))
                                                  .takeWhile(l -> !l.isEmpty())
                                                  .map(xs -> xs.map(x -> x.head));
        return LazySeq.fromStream(res);
    }

    public LazySeq<T> flatten() {
        return LazySeq.cons(head, ()-> subForest().flatMap(t -> t.flatten()));
    }
    public static <T,R> Tree<T> unfold(Function<? super R,Tuple2<T,LazySeq<R>>> fn, R b) {
        Tuple2<T,LazySeq<R>> t2 = fn.apply(b);
        return of(t2._1(), unfoldForest(fn, t2._2()));
    }

    private static <T,R> LazySeq<Tree<T>> unfoldForest(Function<? super R,Tuple2<T,LazySeq<R>>> fn, LazySeq<R> list) {
        return list.map(b -> unfold(fn,b));
    }
    @Override
    public Tuple2<T, LazySeq<Tree<T>>> unapply() {
        return Tuple.tuple(head, subForest());
    }

    public <R> Tree<R> map(Function<? super T, ? extends R> fn){
        return of(fn.apply(head), subForest().map(t->t.map(fn)));
    }

    public <R> Tree<R> flatMap(Function<? super T, ? extends Tree<R>> fn){
        Tree<R> applied = fn.apply(head);
        LazySeq<Tree<R>> children = subForest().map(child -> child.flatMap(fn));
        return of(applied.head, children.prependAll(applied.subForest()));
    }

    public LazySeq<Tree<T>> subForest() {
        return subForest;
    }
}



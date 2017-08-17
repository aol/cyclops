package cyclops.collections.adt;

import cyclops.patterns.CaseClass2;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;
import java.util.function.Supplier;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tree<T> implements CaseClass2<T,LazyList<Tree<T>>> {

    public final T head;
    private final LazyList<Tree<T>> subForest;



    public static <T> Tree<T> of(T head, LazyList<Tree<T>> subForest) {
        return new Tree<>(head,subForest);
    }
    public static <T> Tree<T> lazy(T head, Supplier<LazyList<Tree<T>>> subForest) {
        return new Tree<T>(head,null){

            @Override
            public LazyList<Tree<T>> subForest() {
                return subForest.get();
            }
        };
    }

    public LazyList<LazyList<T>> levels() {
        ReactiveSeq<LazyList<T>> res = ReactiveSeq.iterate(LazyList.of(this), sf -> sf.flatMap(a -> a.subForest()))
                                                  .takeWhile(l -> !l.isEmpty())
                                                  .map(xs -> xs.map(x -> x.head));
        return LazyList.fromStream(res);
    }

    public LazyList<T> flatten() {
        return LazyList.cons(head, ()-> subForest().flatMap(t -> t.flatten()));
    }
    public static <T,R> Tree<T> unfold(Function<? super R,Tuple2<T,LazyList<R>>> fn, R b) {
        Tuple2<T,LazyList<R>> t2 = fn.apply(b);
        return of(t2.v1, unfoldForest(fn, t2.v2));
    }

    private static <T,R> LazyList<Tree<T>> unfoldForest(Function<? super R,Tuple2<T,LazyList<R>>> fn, LazyList<R> list) {
        return list.map(b -> unfold(fn,b));
    }
    @Override
    public Tuple2<T, LazyList<Tree<T>>> unapply() {
        return Tuple.tuple(head, subForest());
    }

    public <R> Tree<R> map(Function<? super T, ? extends R> fn){
        return of(fn.apply(head), subForest().map(t->t.map(fn)));
    }

    public <R> Tree<R> flatMap(Function<? super T, ? extends Tree<R>> fn){
        Tree<R> applied = fn.apply(head);
        LazyList<Tree<R>> children = subForest().map(child -> child.flatMap(fn));
        return of(applied.head, children.prependAll(applied.subForest()));
    }

    public  LazyList<Tree<T>> subForest() {
        return subForest;
    }
}



package cyclops.collections.adt;


import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Trampoline;
import cyclops.patterns.CaseClass2;
import cyclops.patterns.Sealed2;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.collections.adt.List.Cons.cons;
import static cyclops.patterns.Sealed2.matcher;
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NonEmptyList<T> implements CaseClass2<T,List<T>>{

    private final T head;
    private final List<T> tail;

    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(iterable());
    }
    public LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(iterable());
    }
    public static <T> NonEmptyList<T> of(T... value){
        List<T> list = List.of(value);
        List.Cons<T> result = list.match(c -> c, n -> null);
        return cons(result.head,result.tail);
    }


    public Optional<T> get(int pos){
        if(pos==0)
            return Optional.of(head);
        return tail.get(pos);

    }
    public List<T> asList(){
        return List.cons(head,tail);
    }
    public NonEmptyList<T> prepend(T value){
        return cons(value,asList());
    }

    public List<T> prependAll(NonEmptyList<T> value){
        return value.prependAll(this);
    }
    public Iterable<T> iterable(){
        return asList().iterable();
    }
    public List<T> filter(Predicate<? super T> pred){
        return asList().filter(pred);
    }

    public <R> NonEmptyList<R> map(Function<? super T, ? extends R> fn) {
        List<R> list = asList().map(fn);
        return list.asNonEmptyList().get();
    }
    public <R> NonEmptyList<R> flatMap(Function<? super T, ? extends NonEmptyList<R>> fn) {
        List<R> l = asList().flatMap(fn.andThen(a -> a.asList()));
        return l.asNonEmptyList().get();
    }

    public <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
        return asList().foldRight(zero,f);

    }
    public <R> R foldLeft(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
        return asList().foldLeft(zero,f);
    }

    public int size(){
        return 1+tail.size();
    }

    public static <T> NonEmptyList<T> cons(T value,List<T> tail){
        return new NonEmptyList<>(value,tail);
    }

    @Override
    public Tuple2<T, List<T>> unapply() {
        return Tuple.tuple(head,tail);
    }
}

package cyclops.collections.adt;


import cyclops.collections.immutable.LinkedListX;
import cyclops.patterns.CaseClass2;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.collections.adt.SList.Cons.cons;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"head,tail"})
public class NonEmptyList<T> implements CaseClass2<T,SList<T>> {

    private final T head;
    private final SList<T> tail;

    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(iterable());
    }
    public LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(iterable());
    }
    public static <T> NonEmptyList<T> of(T head,T... value){
        SList<T> list = SList.of(value);
        return cons(head,list);
    }


    public Optional<T> get(int pos){
        if(pos==0)
            return Optional.of(head);
        return tail.get(pos);

    }
    public SList<T> asList(){
        return SList.cons(head,tail);
    }
    public NonEmptyList<T> prepend(T value){
        return cons(value,asList());
    }

    public NonEmptyList<T> prependAll(NonEmptyList<T> value){
        return value.prependAll(this);
    }
    public Iterable<T> iterable(){
        return asList().iterable();
    }
    public SList<T> filter(Predicate<? super T> pred){
        return asList().filter(pred);
    }

    public <R> NonEmptyList<R> map(Function<? super T, ? extends R> fn) {
        SList<R> list = asList().map(fn);
        return list.asNonEmptyList().get();
    }
    public <R> NonEmptyList<R> flatMap(Function<? super T, ? extends NonEmptyList<R>> fn) {
        SList<R> l = asList().flatMap(fn.andThen(a -> a.asList()));
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

    public static <T> NonEmptyList<T> cons(T value,SList<T> tail){
        return new NonEmptyList<>(value,tail);
    }

    @Override
    public Tuple2<T, SList<T>> unapply() {
        return Tuple.tuple(head,tail);
    }
}

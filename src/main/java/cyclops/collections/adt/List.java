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

public interface List<T> extends Sealed2<List.Cons<T>,List.Nil> {

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(iterable());
    }
    default LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(iterable());
    }
    static <T> List<T> of(T... value){
        List<T> result = empty();
        for(int i=value.length;i>=0;i++){
            result = result.prepend(value[i-1]);
        }
        return result;
    }
    static <T> List<T> empty(){
        return Nil.Instance;
    }

    default Optional<T> get(int pos){
        T result = null;
        List<T> l = this;
        for(int i=0;i<pos;i++){
           l = l.match(c->c.tail,n->n);
           if(l instanceof Nil){ //short circuit
               return Optional.empty();
           }
        }
        return Optional.ofNullable(l.match(c->c.head,n->null));
    }
    default List<T> prepend(T value){
        return cons(value,this);
    }
    default List<T> prependAll(List<T> value){
        return value.prependAll(this);
    }
    @Override
    default <R> R match(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2){
        return matcher(this,List.Cons.class,List.Nil.class).match(fn1,fn2);
    }

    default Iterable<T> iterable(){
        return ()->new Iterator<T>() {
            List<T> current= List.this;
            @Override
            public boolean hasNext() {
                return current.match(c->true,n->false);
            }

            @Override
            public T next() {
                return current.match(c->{
                    current = c.tail;
                    return c.head;
                },n->null);
            }
        };
    }
    <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f);

    default <R> R foldLeft(R zero,BiFunction<? super T, ? super R, ? extends R> f){
        R acc= zero;
        for(T next : iterable()){
            acc= f.apply(next,acc);
        }
        return acc;
    }
    default List<T> filter(Predicate<? super T> pred){
        return foldRight(empty(),(a,l)->{
            if(pred.test(a)){
                return l.prepend(a);
            }
            return l;
        });
    }
    default <R> List<R> map(Function<? super T, ? extends R> fn) {
        return foldRight(empty(), (a, l) -> l.prepend(fn.apply(a)));
    }
    default <R> List<R> flatMap(Function<? super T, ? extends List<R>> fn) {
        return foldRight(empty(), (a, l) -> fn.apply(a).prependAll(l));
    }


    int size();

    boolean isEmpty();

    default Optional<NonEmptyList<T>> asNonEmptyList(){
        return match(c->Optional.of(NonEmptyList.cons(c.head,c.tail)),n->Optional.empty());
    }

    static <T> List<T> cons(T head, List<T> tail) {
        return Cons.cons(head,tail);
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cons<T> implements CaseClass2<T,List<T>>, List<T>{

        public final T head;
        public final List<T> tail;

        public static <T> Cons<T> cons(T value,List<T> tail){
            return new Cons<>(value,tail);
        }

        @Override
        public Tuple2<T, List<T>> unapply() {
            return Tuple.tuple(head,tail);
        }
        public boolean isEmpty(){
            return false;
        }

        public <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
            class Step{
                public Trampoline<R> loop(List<T> s, Function<? super R, ? extends Trampoline<R>> fn){

                    return s.match(c->Trampoline.more(()->loop(c.tail, rem -> Trampoline.more(() -> fn.apply(f.apply(c.head, rem))))),n->fn.apply(zero));

                }
            }
            return new Step().loop(this,i->Trampoline.done(i)).result();
        }


        public int size(){
            int result =1;
            List<T> current[] = new List[0];
            current[0]=tail;
            while(true){
               int toAdd =current[0].match(c->{
                    current[0]=c;
                    return 1;
                },n->0);
                result+=toAdd;
                if(toAdd==0)
                    break;
            }
            return result;
        }
    }

    public class Nil<T> implements List<T>{
        static Nil Instance = new Nil();
        @Override
        public <R> R foldRight(R zero, BiFunction<? super T, ? super R, ? extends R> f) {
            return zero;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

}

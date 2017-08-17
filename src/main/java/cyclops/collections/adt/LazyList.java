package cyclops.collections.adt;


import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Eval;
import cyclops.control.Trampoline;
import cyclops.patterns.CaseClass2;
import cyclops.patterns.Sealed2;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static cyclops.patterns.Sealed2.matcher;

public interface LazyList<T> extends Sealed2<LazyList.Cons<T>,LazyList.Nil>{

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(iterable());
    }
    default LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(iterable());
    }

    static <T> LazyList<T> fromIterator(Iterator<T> it){
        return it.hasNext() ? cons(it.next(), () -> fromIterator(it)) : empty();
    }
    static <T> LazyList<T> fromStream(Stream<T> stream){
        Iterator<T> t = stream.iterator();
        return cons(t.next(),()->fromIterator(t));
    }
    static <T> LazyList<T> of(T... value){
        LazyList<T> result = empty();
        for(int i=value.length;i>=0;i++){
            result = result.prepend(value[i-1]);
        }
        return result;
    }
    static <T> LazyList<T> empty(){
        return Nil.Instance;
    }

    default Optional<T> get(int pos){
        T result = null;
        LazyList<T> l = this;
        for(int i=0;i<pos;i++){
           l = l.match(c->c.tail.get(),n->n);
           if(l instanceof Nil){ //short circuit
               return Optional.empty();
           }
        }
        return Optional.ofNullable(l.match(c->c.head,n->null));
    }
    default LazyList<T> prepend(T value){
        return cons(value,()->this);
    }
    default LazyList<T> prependAll(LazyList<T> value){
        return value.prependAll(this);
    }
    default <R> R foldLeft(R zero,BiFunction<? super T, ? super R, ? extends R> f){
        R acc= zero;
        for(T next : iterable()){
            acc= f.apply(next,acc);
        }
        return acc;
    }
    @Override
    default <R> R match(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2){
        return matcher(this,LazyList.Cons.class,LazyList.Nil.class).match(fn1,fn2);
    }

    default Iterable<T> iterable(){
        return ()->new Iterator<T>() {
            LazyList<T> current= LazyList.this;
            @Override
            public boolean hasNext() {
                return current.match(c->true,n->false);
            }

            @Override
            public T next() {
                return current.match(c->{
                    current = c.tail.get();
                    return c.head;
                },n->null);
            }
        };
    }
    <R> R foldRight(R zero, BiFunction<? super T, ? super R, ? extends R> f);

    default LazyList<T> filter(Predicate<? super T> pred){
        return foldRight(empty(),(a,l)->{
            if(pred.test(a)){
                return l.prepend(a);
            }
            return l;
        });
    }
    default <R> LazyList<R> map(Function<? super T, ? extends R> fn) {
        return foldRight(empty(), (a, l) -> l.prepend(fn.apply(a)));
    }

    default <R> LazyList<R> flatMap(Function<? super T, ? extends LazyList<R>> fn) {
        return foldRight(empty(), (a, l) -> fn.apply(a).prependAll(l));
    }
    int size();

    boolean isEmpty();

    static <T> LazyList<T> cons(T head, Supplier<LazyList<T>> tail) {
        return Cons.cons(head,tail);
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cons<T> implements CaseClass2<T,LazyList<T>>, LazyList<T> {

        public final T head;
        public final Supplier<LazyList<T>> tail;

        public static <T> Cons<T> cons(T value,Supplier<LazyList<T>> tail){
            return new Cons<>(value,tail);
        }

        @Override
        public Tuple2<T, LazyList<T>> unapply() {
            return Tuple.tuple(head,tail.get());
        }
        public boolean isEmpty(){
            return false;
        }

        public <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
            class Step{
                public Trampoline<R> loop(LazyList<T> s, Function<? super R, ? extends Trampoline<R>> fn){

                    return s.match(c->Trampoline.more(()->loop(c.tail.get(), rem -> Trampoline.more(() -> fn.apply(f.apply(c.head, rem))))),n->fn.apply(zero));

                }
            }
            return new Step().loop(this,i->Trampoline.done(i)).result();
        }


        public int size(){
            int result =1;
            LazyList<T> current[] = new LazyList[0];
            current[0]=tail.get();
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

    public class Nil<T> implements LazyList<T> {
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

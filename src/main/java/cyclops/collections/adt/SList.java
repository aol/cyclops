package cyclops.collections.adt;


import cyclops.collections.immutable.LinkedListX;
import cyclops.control.Trampoline;
import cyclops.patterns.CaseClass2;
import cyclops.patterns.Sealed2;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.collections.adt.SList.Cons.cons;

//safe list implementation that does not support exceptional states
public interface SList<T> extends Sealed2<SList.Cons<T>,SList.Nil> {

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(iterable());
    }
    default LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(iterable());
    }
    static <T> SList<T> of(T... value){
        SList<T> result = empty();
        for(int i=value.length;i>0;i--){
            result = result.prepend(value[i-1]);
        }
        return result;
    }
    static <T> SList<T> empty(){
        return Nil.Instance;
    }

    default Optional<T> get(int pos){
        T result = null;
        SList<T> l = this;
        for(int i=0;i<pos;i++){
           l = l.match(c->c.tail,n->n);
           if(l instanceof Nil){ //short circuit
               return Optional.empty();
           }
        }
        return Optional.ofNullable(l.match(c->c.head,n->null));
    }
    default SList<T> prepend(T value){
        return cons(value,this);
    }
    default SList<T> prependAll(SList<T> value){
        return value.prependAll(this);
    }


    default Iterable<T> iterable(){
        return ()->new Iterator<T>() {
            SList<T> current= SList.this;
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
    default SList<T> filter(Predicate<? super T> pred){
        return foldRight(empty(),(a,l)->{
            if(pred.test(a)){
                return l.prepend(a);
            }
            return l;
        });
    }
    default <R> SList<R> map(Function<? super T, ? extends R> fn) {
        return foldRight(empty(), (a, l) -> l.prepend(fn.apply(a)));
    }
    default <R> SList<R> flatMap(Function<? super T, ? extends SList<R>> fn) {
        return foldRight(empty(), (a, l) -> fn.apply(a).prependAll(l));
    }


    int size();

    boolean isEmpty();

    default Optional<NonEmptyList<T>> asNonEmptyList(){
        return match(c->Optional.of(NonEmptyList.cons(c.head,c.tail)),n->Optional.empty());
    }

    static <T> SList<T> cons(T head, SList<T> tail) {
        return Cons.cons(head,tail);
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of={"head,tail"})
    public static class Cons<T> implements CaseClass2<T,SList<T>>, SList<T> {

        public final T head;
        public final SList<T> tail;

        public static <T> Cons<T> cons(T value,SList<T> tail){
            return new Cons<>(value,tail);
        }

        @Override
        public Tuple2<T, SList<T>> unapply() {
            return Tuple.tuple(head,tail);
        }
        public boolean isEmpty(){
            return false;
        }

        public <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
            class Step{
                public Trampoline<R> loop(SList<T> s, Function<? super R, ? extends Trampoline<R>> fn){

                    return s.match(c->Trampoline.more(()->loop(c.tail, rem -> Trampoline.more(() -> fn.apply(f.apply(c.head, rem))))),n->fn.apply(zero));

                }
            }
            return new Step().loop(this,i->Trampoline.done(i)).result();
        }


        public int size(){
            int result =1;
            SList<T> current[] = new SList[0];
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

        @Override
        public <R> R match(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
            return fn1.apply(this);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Nil<T> implements SList<T> {
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

        @Override
        public <R> R match(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
            return fn2.apply(this);
        }
    }

}

package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provide easy access to all typeclasses for a type
 * e.g.
 *
 * <pre>
 *     {@code
 *       Active<list,Integer> active = Active.of(ListX.of(1,2,3),ListX.Instances.definitions());
 *       Active<list,Integer> doubled = active.map(i->i*2);
 *       Active<list,Integer> doubledPlusOne = doubled.flatMap(i->ListX.of(i+1));
 *     }
 *
 * </pre>
 *
 * @param <W> Witness type
 * @param <T> Data type
 */
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"single"})
public class Active<W,T> {


    private final Higher<W,T> single;
    @Getter
    private final InstanceDefinitions<W> def1;

    public static <W,T> Active<W,T> of(Higher<W,T> single, InstanceDefinitions<W> def1){
        return new Active<>(single,def1);
    }
    public Higher<W,T> getActive(){
        return single;
    }
    public <R> Active<W,R> unit(R value){
        return of(def1.unit().unit(value),def1);
    }
    public Active<W,T> filter(Predicate<? super T> predicate){
        return of(def1.monadZero().visit(s->s.filter(predicate,single),()->single),def1);
    }
    public <R> Active<W,R> map(Function<? super T, ? extends R> fn){
        return of(def1.functor().map(fn,single),def1);
    }
    public  Active<W,T> peek(Consumer<? super T> fn){
        return of(def1.functor().peek(fn,single),def1);
    }

    public  <R> Function<Active<W, T>, Active<W, R>> lift(final Function<? super T, ? extends R> fn) {
        return t -> of(def1.functor().map(fn,t.single),def1);
    }
    public <R> Active<W,R> flatMap(Function<? super T, ? extends Higher<W, R>> fn){
        return of(def1.monad().flatMap(fn,single),def1);
    }
    public <R> Active<W,R> ap(Higher<W, ? extends Function<T, R>> fn){
        return of(def1.applicative().ap(fn,single),def1);
    }

    public Maybe<T> foldRight(Monoid<T> monoid){
        return def1.foldable().map(f->f.foldRight(monoid,single));
    }


    public Maybe<T> foldRight(T identity, BinaryOperator<T> semigroup){
        return def1.foldable().map(f->f.foldRight(Monoid.fromBiFunction(identity, semigroup),single));
    }

    public Maybe<T> foldLeft(Monoid<T> monoid){
        return def1.foldable().map(f->f.foldLeft(monoid,single));
    }


    public   Maybe<T> foldLeft(T identity, BinaryOperator<T> semigroup){
        return def1.foldable().map(f->f.foldLeft(identity,semigroup,single));
    }
    public String toString(){
        return "Active["+single.toString()+"]";
    }
}

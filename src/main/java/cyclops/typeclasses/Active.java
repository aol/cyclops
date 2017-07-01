package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
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


    private final Higher<W, T> single;
    @Getter
    private final InstanceDefinitions<W> def1;

    public static <W, T> Active<W, T> of(Higher<W, T> single, InstanceDefinitions<W> def1) {
        return new Active<>(single, def1);
    }

    public Higher<W, T> getActive() {
        return single;
    }

    public <R> Active<W, R> unit(R value) {
        return of(def1.unit().unit(value), def1);
    }

    public Active<W, T> filter(Predicate<? super T> predicate) {
        return of(def1.monadZero().visit(s -> s.filter(predicate, single), () -> single), def1);
    }

    public <R> Active<W, R> map(Function<? super T, ? extends R> fn) {
        return of(def1.functor().map(fn, single), def1);
    }

    public Active<W, T> peek(Consumer<? super T> fn) {
        return of(def1.functor().peek(fn, single), def1);
    }

    public <R> Function<Active<W, T>, Active<W, R>> lift(final Function<? super T, ? extends R> fn) {
        return t -> of(def1.functor().map(fn, t.single), def1);
    }

    public <R> Active<W, R> flatMap(Function<? super T, ? extends Higher<W, R>> fn) {
        return of(def1.monad().flatMap(fn, single), def1);
    }

    public <R> Active<W, R> ap(Higher<W, ? extends Function<T, R>> fn) {
        return of(def1.applicative().ap(fn, single), def1);
    }

    public Maybe<Folds> folds(){
        return def1.foldable().visit(e->Maybe.just(new Folds()),Maybe::none);
    }
    public Maybe<Traverse> traverse(){
        return def1.traverse().visit(e->Maybe.just(new Traverse()),Maybe::none);
    }
    public class Folds {


        public T foldRight(Monoid<T> monoid) {
            return def1.foldable().get().foldRight(monoid, single);
        }


        public T foldRight(T identity, BinaryOperator<T> semigroup) {
            return def1.foldable().get().foldRight(Monoid.fromBiFunction(identity, semigroup), single);
        }

        public T foldLeft(Monoid<T> monoid) {
            return def1.foldable().get().foldLeft(monoid, single);
        }


        public T foldLeft(T identity, BinaryOperator<T> semigroup) {
            return def1.foldable().get().foldLeft(identity, semigroup, single);
        }

    }

    public class Traverse{
        public  <W2, R> Higher<W2, Higher<W, R>> flatTraverse(Applicative<W2> applicative,
                                                               Function<? super T,? extends Higher<W2, Higher<W, R>>>f) {
            return def1.traverse().get().flatTraverse(applicative,def1.monad(),single,f);

        }
    }
    public String toString(){
        return "Active["+single.toString()+"]";
    }
}

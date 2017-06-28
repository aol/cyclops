package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Optionals;
import cyclops.function.Monoid;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.optional;
import cyclops.typeclasses.functor.Compose;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class for working with Nested Data Structures.
 *
 * E.g. to work with a List of Optionals
 * <pre>
 *     {@code
 *      import cyclops.monads.Witness.list;
        import cyclops.monads.Witness.optional;

 *      Nested<list,optional,Integer> listOfOptionalInt = Nested.of(ListX.of(Optionals.OptionalKind.of(2)),ListX.Instances.definitions(),Optionals.Instances.definitions());
 *      //Nested[List[Optional[2]]]
 *     }
 *
 * </pre>
 *
 * Transform nested data
 * <pre>
 *     {@code
 *     Nested<list,optional,Integer> listOfOptionalInt;  //Nested[List[Optional[2]]]
 *     Nested<list,optional,Integer> doubled = listOfOptionalInt.map(i->i*2);
 *      //Nested[List[Optional[4]]]
 *     }
 *
 *
 * </pre>
 *
 * Sequencing data
 * <pre>
 *     {@code
 *     Nested<list,optional,Integer> listOfOptionalInt;  //Nested[List[Optional[2]]]
 *     Nested<optional,list,Integer> sequenced = listOfOptionalInt.sequence();
 *     //Nested[Optional[List[2]]]
 *
 *     }
 *
 *
 * </pre>
 *
 *
 * @param <W1> First Witness type {@see cyclops.monads.Witness}
 * @param <W2> Second Witness type {@see cyclops.monads.Witness}
 * @param <T> Nested Data Type
 */
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"nested"})
public class Nested<W1,W2,T> {

    private final Higher<W1,Higher<W2,T>> nested;
    private final Compose<W1,W2> composedFunctor;
    private final InstanceDefinitions<W1> def1;
    private final InstanceDefinitions<W2> def2;

    public static <W1,W2,T> Nested<W1,W2,T> of(Higher<W1,Higher<W2,T>> nested,InstanceDefinitions<W1> def1,InstanceDefinitions<W2> def2){
        Compose<W1,W2> composed = Compose.compose(def1.functor(),def2.functor());
        return new Nested<>(nested,composed,def1,def2);
    }

    public Higher<W1, Higher<W2, T>> getNested() {
        return nested;
    }

    public <R> R fold(Function<? super Higher<W1, Higher<W2, T>>, ? extends R> fn){
        return fn.apply(nested);
    }
    public <R> Nested<W1,W2,R> map(Function<? super T,? extends R> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map(fn, nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }
    public  Nested<W1,W2,T> peek(Consumer<? super T> fn){
        Higher<W1, Higher<W2, T>> res = composedFunctor.peek(fn, nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }

    public <R> Function<Nested<W1,W2,T>, Nested<W1,W2,R>> lift(final Function<? super T, ? extends R> fn) {
        return t -> map(fn);
    }

    public <R> Nested<W1,W2,R> ap(Higher<W2,? extends Function<T, R>> fn){
        Higher<W1, Higher<W2, R>> res = def1.functor().map(a -> def2.applicative().ap(fn, a), nested);
        return of(res,def1,def2);
    }

    public <R> Nested<W1,W2,R> flatMap(Function<? super T, ? extends Higher<W2,R>> fn){
        Higher<W1, Higher<W2, R>> res = composedFunctor.map1(a->def2.monad().flatMap(fn, a),nested);
        return new Nested<>(res,composedFunctor,def1,def2);
    }


    public  Nested<W2, W1, T> sequence(){
        Higher<W2, Higher<W1, T>> res = def1.traverse().sequenceA(def2.applicative(), nested);
        return of(res,def2,def1);
    }
    public  <R> Nested<W2, W1, R> traverse(Function<? super T,? extends R> fn){
       return sequence().map(fn);
    }

    public  Higher<W1,T> foldRight(Monoid<T> monoid){
        return def1.functor().map(a -> def2.foldable().foldRight(monoid, a), nested);
    }
    public  Higher<W1,T> foldLeft(Monoid<T> monoid){
        return def1.functor().map(a -> def2.foldable().foldLeft(monoid, a), nested);
    }
    public String toString(){
        return "Nested["+nested.toString()+"]";
    }


}

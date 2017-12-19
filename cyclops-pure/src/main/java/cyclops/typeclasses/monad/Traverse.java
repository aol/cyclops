package cyclops.typeclasses.monad;

import com.oath.cyclops.hkt.Higher;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.control.Constant;
import cyclops.control.Maybe;
import cyclops.control.State;
import cyclops.function.Monoid;
import cyclops.instances.control.ConstantInstances;
import cyclops.instances.control.StateInstances;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import static cyclops.control.State.state;
import static cyclops.data.tuple.Tuple.tuple;

//HighJ Traverse, ScalaZ Traverse and Cats Traverse Influences
public interface Traverse<CRE> extends Applicative<CRE>{

   <C2,T,R> Higher<C2, Higher<CRE, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                 Higher<CRE, T> ds);

    <C2,T> Higher<C2, Higher<CRE, T>> sequenceA(Applicative<C2> applicative,
                                                Higher<CRE, Higher<C2, T>> ds);

    default  <C2, T, R> Higher<C2, Higher<CRE, R>> flatTraverse(Applicative<C2> applicative, Monad<CRE> monad, Higher<CRE, T> fa,
                                                              Function<? super T,? extends Higher<C2, Higher<CRE, R>>>f) {
       return applicative.map_(traverseA(applicative,f,fa), it->monad.flatten(it));
    }

    default <C2, T> Higher<C2, Higher<CRE, T>> flatSequence(Applicative<C2> applicative, Monad<CRE> monad,Higher<CRE,Higher<C2,Higher<CRE,T>>> fgfa) {
        return applicative.map(i -> monad.flatMap(Function.identity(), i), sequenceA(applicative, fgfa));
    }


    //traverse with a State, State has an inbuilt trampoline in cyclops-react
    default <S,T,R> State<S,Higher<CRE,R>> traverseS(Function<? super T, ? extends State<S,R>> fn,Higher<CRE, T> ds){
        return  State.narrowK(traverseA(StateInstances.applicative(), fn, ds));

    }
    default <S,T,R> Tuple2<S, Higher<CRE, R>> runTraverseS(Function<? super T, ? extends State<S,R>> fn,Higher<CRE, T> ds, S val) {
        return traverseS(fn, ds).run(val);
    }

    //based on ScalaZ mapAccumL
    default  <S,T,R>  Tuple2<S, Higher<CRE, R>> mapAccumL (BiFunction<? super S, ? super T, ? extends Tuple2<S,R>> f,Higher<CRE, T> ds,S z) {
        return runTraverseS(a-> {

            return State.<S>get().forEach2(s1->{
                Tuple2<S, R> t2 = f.apply(s1, a);
                return State.state(__->t2);
            },(s1,b)->b);

        },ds, z);
    }

    default <T> Higher<CRE,T> reverse(Higher<CRE, T> ds){
        Tuple2<LinkedListX<T>, Higher<CRE, T>> t2 = mapAccumL((t,h)-> tuple(t.plus(h),h),ds, LinkedListX.empty());

        return runTraverseS(t ->
                        State.<LinkedListX<T>>get()
                                .forEach2(e -> State.put(e.tail()), (a, b) -> a.head())
                , t2._2(), t2._1())._2();

    }
    default <T, R> R foldMap(Monoid<R> mb, final Function<? super T,? extends R> fn, Higher<CRE, T> ds) {
        return Constant.narrowK(traverseA(ConstantInstances.applicative(mb), a -> Constant.of(fn.apply(a)), ds)).get();
    }
    default <T,R> Higher<CRE,R> mapWithIndex(BiFunction<? super T,Long,? extends R> f, Higher<CRE, T> ds) {

        State<Long,  Higher<CRE, R>> st = State.narrowK(traverseA(StateInstances.applicative(),
                a -> state((Long s) -> tuple(s + 1, f.apply(a, s))), ds));
        return st.run(0l)._2();

    }

    default <T,C2,T2,R> Higher<CRE,R> zipWith(Foldable<C2> foldable, BiFunction<? super T,? super Maybe<T2>,? extends R> f, Higher<CRE, T> ds, Higher<C2, T2> ds2) {
        Iterator<T2> list =foldable.listX(ds2)
                                   .iterator();
        State<Maybe<T2>,  Higher<CRE, R>> st = State.narrowK(traverseA(StateInstances.applicative(),
                a -> {

                    State<Maybe<T2>,R> xz = state((Maybe<T2> s) -> tuple(list.hasNext() ? Maybe.just(list.next()) : Maybe.nothing(), f.apply(a, s)));
                    return xz;
                }, ds));
        Maybe<T2> opt = list.hasNext() ? Maybe.of(list.next()) : Maybe.nothing();
        return st.run(opt)._2();

    }
    default <T,R> Higher<CRE,Tuple2<T,Long>> zipWithIndex(Higher<CRE, T> ds) {
        return mapWithIndex(Tuple::tuple, ds);
    }
}

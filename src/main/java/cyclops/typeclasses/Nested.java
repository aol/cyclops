package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.async.Future;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.CompletableFutures;
import cyclops.companion.CompletableFutures.CompletableFutureKind;
import cyclops.companion.Optionals;
import cyclops.companion.Optionals.OptionalKind;
import cyclops.companion.Streams;
import cyclops.companion.Streams.StreamKind;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.control.Xor;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.functor.Compose;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

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
 * Transform nest data
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
@EqualsAndHashCode(of={"nest"})
public class Nested<W1,W2,T> implements Transformable<T> {

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

    public Traverse traverseUnsafe(){
        return def1.traverse().visit(s-> new Traverse(),()->null);
    }
    public Folds foldsUnsafe(){
        return def1.foldable().visit(s-> new Folds(),()->null);
    }
    public Maybe<Traverse> traverse(){
        return def1.traverse().visit(s-> Maybe.just(new Traverse()),Maybe::none);
    }
    public Maybe<Folds> folds(){
        return def1.foldable().visit(s-> Maybe.just(new Folds()),Maybe::none);
    }

    public class Folds{
        public  Higher<W1,T> foldRight(Monoid<T> monoid){
            return def1.functor().map(a -> def2.foldable().get().foldRight(monoid, a), nested);
        }
        public  Higher<W1,T> foldLeft(Monoid<T> monoid){
            return def1.functor().map(a -> def2.foldable().get().foldLeft(monoid, a), nested);
        }
    }
    public class Traverse {

        public Nested<W2, W1, T> sequence(){
            Higher<W2, Higher<W1, T>> res = def1.traverse().get().sequenceA(def2.applicative(), nested);
            return of(res,def2,def1);
        }
        public  <R> Nested<W2, W1, R> traverse(Function<? super T,? extends R> fn){
            return sequence().map(fn);
        }

    }

    public String toString(){
        return "Nested["+nested.toString()+"]";
    }

    @Override
    public <U> Nested<W1,W2,U> cast(Class<? extends U> type) {
        return (Nested<W1,W2,U>)Transformable.super.cast(type);
    }

    @Override
    public <R> Nested<W1,W2,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Nested<W1,W2,R>)Transformable.super.trampoline(mapper);
    }

    @Override
    public <R> Nested<W1,W2,R> retry(Function<? super T, ? extends R> fn) {
        return (Nested<W1,W2,R>)Transformable.super.retry(fn);
    }

    @Override
    public <R> Nested<W1,W2,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (Nested<W1,W2,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }


    public static <T> Nested<completableFuture,stream,T> completableFutureStream(CompletableFuture<? extends Stream<T>> optionalList){
        CompletableFutureKind<StreamKind<T>> opt = CompletableFutureKind.widen(optionalList.thenApply(StreamKind::widen));
        Higher<completableFuture,Higher<stream,T>> hkt = (Higher)opt;
        return of(hkt, CompletableFutures.Instances.definitions(), Streams.Instances.definitions());
    }
    public static <T> Nested<optional,stream,T> optionalStream(Optional<? extends Stream<T>> optionalList){
        OptionalKind<StreamKind<T>> opt = OptionalKind.widen(optionalList).map(StreamKind::widen);
        Higher<optional,Higher<stream,T>> hkt = (Higher)opt;
        return of(hkt, Optionals.Instances.definitions(), Streams.Instances.definitions());
    }

    public static <T> Nested<optional,list,T> optionalList(Optional<? extends List<T>> optionalList){
        OptionalKind<ListX<T>> opt = OptionalKind.widen(optionalList).map(ListX::fromIterable);
        Higher<optional,Higher<list,T>> hkt = (Higher)opt;
        return of(hkt, Optionals.Instances.definitions(), ListX.Instances.definitions());
    }
    public static <T, X extends Throwable> Nested<future,Higher<tryType,X>,T> futureTry(Future<? extends Try<T,X>> futureTry){
        Higher<future,Higher<Higher<tryType,X>,T>> hkt = (Higher)futureTry;
        return of(hkt, Future.Instances.definitions(), Try.Instances.definitions());
    }
    public static <T, X extends Throwable> Nested<list,Higher<tryType,X>,T> listTry(List<? extends Try<T,X>> futureTry){
        Higher<list,Higher<Higher<tryType,X>,T>> hkt = (Higher)futureTry;
        return of(hkt, ListX.Instances.definitions(), Try.Instances.definitions());
    }
    public static <L,R> Nested<list,Higher<xor,L>,R> listXor(List<? extends Xor<L,R>> listXor){
        Higher<list,Higher<Higher<xor,L>,R>> hkt = (Higher)listXor;
        return of(hkt, ListX.Instances.definitions(), Xor.Instances.definitions());
    }
    public static <L,R> Nested<future,Higher<xor,L>,R> futureXor(Future<? extends Xor<L,R>> futureXor){
        Higher<future,Higher<Higher<xor,L>,R>> hkt = (Higher)futureXor;
        return of(hkt, Future.Instances.definitions(), Xor.Instances.definitions());
    }
    public static <T> Nested<future,list,T> futureList(Future<? extends List<T>> futureList){
        return of(futureList.map(ListX::fromIterable),Future.Instances.definitions(), ListX.Instances.definitions());
    }
    public static <T> Nested<future,vectorX,T> futureVector(Future<VectorX<T>> futureList){
        Higher<future,Higher<vectorX,T>> hkt = (Higher)futureList;
        return of(hkt,Future.Instances.definitions(), VectorX.Instances.definitions());
    }
}

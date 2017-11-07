package cyclops.monads;

import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Either;
import cyclops.control.LazyEither;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.Witness.*;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A Sum type for monads of the same type.
 * XorM is active type biased (rather than right biased).
 * e.g.
 * <pre>
 *     {@code
 *          XorM<stream,optional,Integer> nums = XorM.stream(1,2,3)
                                                     .swap();
 *          int result = nums.map(i->i*2)
 *                           .foldLeft(Monoids.intSum);
 *          //12
 *     }
 *
 * </pre>
 *
 * @param <W1> Witness type of monad
 * @param <W2> Witness type of monad
 * @param <T> Data type
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of="either")
public class XorM<W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> implements Filters<T>,
                                                                                       Transformable<T>,
                                                                                       Folds<T>,
                                                                                       Zippable<T>,
                                                                                       Publisher<T>,
                                                                                       To<XorM<W1,W2,T>> {

    @Getter
    private final Either<AnyM<W1,T>,AnyM<W2,T>> xor;

    public static  <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> XorM<W1,W2,T> of(Either<? extends AnyM<? extends W1,? extends T>,
                    ? extends AnyM<? extends W2,? extends T>> xor){
        return new XorM<>((Either)xor);
    }
    public static  <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> XorM<W1,W2,T> right(AnyM<W2,T> right){
        return new XorM<>(Either.right(right));
    }
    public static  <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> XorM<W1,W2,T> left(AnyM<W1,T> left){
        return new XorM<>(Either.left(left));
    }
    public XorM<W1,W2,T> filter(Predicate<? super T> test) {
      return of(xor.map(m -> m.filter(test)).mapLeft(m->m.filter(test)));
    }

    public <R>  XorM<W1,W2,R> coflatMap(final Function<? super  XorM<W1,W2,T>, R> mapper){

        return visit(leftM ->  left(leftM.unit(mapper.apply(this))),
                     rightM -> right(rightM.unit(mapper.apply(this))));
    }

    @Override
    public <U> XorM<W1,W2,U> ofType(Class<? extends U> type) {
        Either<? extends AnyM<W1, ? extends U>, ? extends AnyM<W2, ? extends U>> x = xor.map(m -> m.ofType(type)).mapLeft(m -> m.ofType(type));
        return of(x);
    }

    @Override
    public XorM<W1,W2,T> filterNot(Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    @Override
    public XorM<W1,W2,T> notNull() {
        return of(xor.map(m -> m.notNull()).mapLeft(m->m.notNull()));
    }



    @Override
    public <R>  XorM<W1,W2,R> map(Function<? super T, ? extends R> fn) {
        return of(xor.map(m->m.map(fn)).mapLeft(m->m.map(fn)));
    }


    @Override
    public  XorM<W1,W2,T> peek(Consumer<? super T> c) {
        return map(a->{
            c.accept(a);
            return a;
        });
    }

    @Override
    public String toString() {
        return "XorM["+xor.toString()+"]";
    }

    @Override
    public <R>  XorM<W1,W2,R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        Either<? extends AnyM<W1, ? extends R>, ? extends AnyM<W2, ? extends R>> x = xor.map(m -> m.trampoline(mapper)).mapLeft(m -> m.trampoline(mapper));
        return of(x);
    }

    @Override
    public <R>  XorM<W1,W2,R> retry(Function<? super T, ? extends R> fn) {
        return of(xor.map(m->m.retry(fn)).mapLeft(m->m.retry(fn)));
    }

    @Override
    public <R>  XorM<W1,W2,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return of(xor.map(m->m.retry(fn,retries,delay,timeUnit)).mapLeft(m->m.retry(fn,retries,delay,timeUnit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return xor.visit(a->a.stream(),b->b.stream());
    }

  @Override
    public <T2, R>  XorM<W1,W2,R> zip(Iterable<? extends T2> iterable, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return of(xor.map(a->a.zip(iterable,fn)).mapLeft(a->a.zip(iterable,fn)));
    }

    @Override
    public <T2, R>  XorM<W1,W2,R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return of(xor.map(a->a.zip(fn, publisher)).mapLeft(a->a.zip(fn, publisher)));
    }


    @Override
    public <U>  XorM<W1,W2,Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (XorM)Zippable.super.zipWithPublisher(other);
    }

    @Override
    public <U>  XorM<W1,W2,Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (XorM)Zippable.super.zip(other);
    }

    @Override
    public <S, U>  XorM<W1,W2,Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (XorM)Zippable.super.zip3(second,third);
    }

    @Override
    public <S, U, R>  XorM<W1,W2,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (XorM)Zippable.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4>  XorM<W1,W2,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (XorM)Zippable.super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R>  XorM<W1,W2,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (XorM)Zippable.super.zip4(second,third,fourth,fn);
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        xor.visit(a->{
            a.subscribe(s);
            return null;
        },b->{
            b.subscribe(s);
            return null;
        });

    }

    public <R> R visit(Function<? super AnyM<W1,? super T>, ? extends R> left,Function<? super AnyM<W2,? super T>, ? extends R> right ){
        return xor.visit(left,right);
    }

    public XorM<W2,W1,T> swap(){
        return of(xor.swap());
    }

    @Override
    public Iterator<T> iterator() {
        return xor.visit(a->a.iterator(),b->b.iterator());
    }


    public static  <W1 extends WitnessType<W1>,T> XorM<W1,vectorX,T> vectorX(VectorX<T> list){
        return new XorM<>(Either.right(AnyM.fromVectorX(list)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,vectorX,T> vectorX(T... values){
        return new XorM<>(Either.right(AnyM.fromVectorX(VectorX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,linkedListX,T> linkedListX(LinkedListX<T> list){
        return new XorM<>(Either.right(AnyM.fromLinkedListX(list)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,linkedListX,T> linkedListX(T... values){
        return new XorM<>(Either.right(AnyM.fromLinkedListX(LinkedListX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,persistentSetX,T> persistentsetX(PersistentSetX<T> list){
        return new XorM<>(Either.right(AnyM.fromPersistentSetX(PersistentSetX.fromIterable(list))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,persistentSetX,T> persistentsetX(T... values){
        return new XorM<>(Either.right(AnyM.fromPersistentSetX(PersistentSetX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,set,T> setX(Set<T> list){
        return new XorM<>(Either.right(AnyM.fromSet(SetX.fromIterable(list))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,set,T> setX(T... values){
        return new XorM<>(Either.right(AnyM.fromSet(SetX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,list,T> listX(List<T> list){
        return new XorM<>(Either.right(AnyM.fromList(ListX.fromIterable(list))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,list,T> listX(T... values){
        return new XorM<>(Either.right(AnyM.fromList(ListX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,stream,T> stream(Stream<T> stream){
        return new XorM<>(Either.right(AnyM.fromStream(stream)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,stream,T> stream(T... values){
        return new XorM<>(Either.right(AnyM.fromStream(Stream.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,reactiveSeq,T> reactiveSeq(ReactiveSeq<T> stream){
        return new XorM<>(Either.right(AnyM.fromStream(stream)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,reactiveSeq,T> reactiveSeq(T... values){
        return new XorM<>(Either.right(AnyM.fromStream(ReactiveSeq.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,tryType,T> success(T value){
        return new XorM<>(Either.right(AnyM.success(value)));
    }
    public static  <W1 extends WitnessType<W1>,X extends Throwable,T> XorM<W1,tryType,T> failure(X value){
        return new XorM<>(Either.right(AnyM.failure(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,future,T> futureOf(Supplier<T> value, Executor ex){
        return new XorM<>(Either.right(AnyM.futureOf(value,ex)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,completableFuture,T> completableFutureOf(Supplier<T> value, Executor ex){
        return new XorM<>(Either.right(AnyM.completableFutureOf(value,ex)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,eval,T> later(Supplier<T> value){
        return new XorM<>(LazyEither.right(AnyM.later(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,optional,T> ofNullable(T value){
        return new XorM<>(Either.right(AnyM.ofNullable(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> just(T value){
        return new XorM<>(Either.right(AnyM.just(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> none(){
        return new XorM<>(Either.right(AnyM.none()));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> maybeNullabe(T value){
        return new XorM<>(Either.right(AnyM.fromMaybe(Maybe.ofNullable(value))));
    }
}

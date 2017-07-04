package cyclops.monads;

import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.reactive.Completable;
import cyclops.async.Future;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import cyclops.control.lazy.Either;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.monads.Witness.*;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
 *          XorM<stream,optional,Integer> nums = XorM.stream(1,2,3);
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
@EqualsAndHashCode(of="xor")
public class XorM<W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> implements Filters<T>,
                                                                                       Transformable<T>,
                                                                                       Folds<T>,
                                                                                       Zippable<T>,
                                                                                       Publisher<T>,
                                                                                       To<XorM<W1,W2,T>> {

    @Getter
    private final Xor<AnyM<W1,T>,AnyM<W2,T>> xor;

    public static  <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> XorM<W1,W2,T> of(Xor<? extends AnyM<? extends W1,? extends T>,
            ? extends AnyM<? extends W2,? extends T>> xor){
        return new XorM<>((Xor)xor);
    }
    public static  <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> XorM<W1,W2,T> right(AnyM<W2,T> right){
        return new XorM<>(Xor.primary(right));
    }
    public static  <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> XorM<W1,W2,T> left(AnyM<W1,T> left){
        return new XorM<>(Xor.secondary(left));
    }
    public XorM<W1,W2,T> filter(Predicate<? super T> test) {
      return of(xor.map(m -> m.filter(test)).secondaryMap(m->m.filter(test)));
    }

    public <R>  XorM<W1,W2,R> coflatMap(final Function<? super  XorM<W1,W2,T>, R> mapper){

        return visit(leftM ->  left(leftM.unit(mapper.apply(this))),
                     rightM -> right(rightM.unit(mapper.apply(this))));
    }

    @Override
    public <U> XorM<W1,W2,U> ofType(Class<? extends U> type) {
        Xor<? extends AnyM<W1, ? extends U>, ? extends AnyM<W2, ? extends U>> x = xor.map(m -> m.ofType(type)).secondaryMap(m -> m.ofType(type));
        return of(x);
    }

    @Override
    public XorM<W1,W2,T> filterNot(Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    @Override
    public XorM<W1,W2,T> notNull() {
        return of(xor.map(m -> m.notNull()).secondaryMap(m->m.notNull()));
    }

    @Override
    public <U>  XorM<W1,W2,U> cast(Class<? extends U> type) {
        Xor<? extends AnyM<? extends W1, ? extends U>, ? extends AnyM<? extends W2, ? extends U>> x = xor.map(m -> m.cast(type)).secondaryMap(m -> m.cast(type));
        return of(x);
    }

    @Override
    public <R>  XorM<W1,W2,R> map(Function<? super T, ? extends R> fn) {
        return of(xor.map(m->m.map(fn)).secondaryMap(m->m.map(fn)));
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
        Xor<? extends AnyM<W1, ? extends R>, ? extends AnyM<W2, ? extends R>> x = xor.map(m -> m.trampoline(mapper)).secondaryMap(m -> m.trampoline(mapper));
        return of(x);
    }

    @Override
    public <R>  XorM<W1,W2,R> retry(Function<? super T, ? extends R> fn) {
        return of(xor.map(m->m.retry(fn)).secondaryMap(m->m.retry(fn)));
    }

    @Override
    public <R>  XorM<W1,W2,R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return of(xor.map(m->m.retry(fn,retries,delay,timeUnit)).secondaryMap(m->m.retry(fn,retries,delay,timeUnit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return xor.visit(a->a.stream(),b->b.stream());
    }

    @Override
    public XorM<W1,W2,T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (XorM<W1,W2,T>)Zippable.super.zip(combiner,app);
    }

    @Override
    public <R>  XorM<W1,W2,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (XorM<W1,W2,R>)Zippable.super.zipWith(fn);
    }

    @Override
    public <R>  XorM<W1,W2,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (XorM<W1,W2,R>)Zippable.super.zipWithS(fn);
    }

    @Override
    public <R>  XorM<W1,W2,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (XorM<W1,W2,R>)Zippable.super.zipWithP(fn);
    }

    @Override
    public <T2, R>  XorM<W1,W2,R> zip(Iterable<? extends T2> iterable, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return of(xor.map(a->a.zip(iterable,fn)).secondaryMap(a->a.zip(iterable,fn)));
    }

    @Override
    public <T2, R>  XorM<W1,W2,R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return of(xor.map(a->a.zipP(publisher,fn)).secondaryMap(a->a.zipP(publisher,fn)));
    }

    @Override
    public <U, R>  XorM<W1,W2,R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return of(xor.map(a->a.zipS(other,zipper)).secondaryMap(a->a.zipS(other,zipper)));
    }

    @Override
    public <U>  XorM<W1,W2,Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return (XorM)Zippable.super.zipS(other);
    }

    @Override
    public <U>  XorM<W1,W2,Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (XorM)Zippable.super.zipP(other);
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
    public <S, U, R>  XorM<W1,W2,R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (XorM)Zippable.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4>  XorM<W1,W2,Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (XorM)Zippable.super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R>  XorM<W1,W2,R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
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
        return new XorM<>(Xor.primary(AnyM.fromVectorX(list)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,vectorX,T> vectorX(T... values){
        return new XorM<>(Xor.primary(AnyM.fromVectorX(VectorX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,linkedListX,T> linkedListX(LinkedListX<T> list){
        return new XorM<>(Xor.primary(AnyM.fromLinkedListX(list)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,linkedListX,T> linkedListX(T... values){
        return new XorM<>(Xor.primary(AnyM.fromLinkedListX(LinkedListX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,persistentSetX,T> persistentsetX(PersistentSetX<T> list){
        return new XorM<>(Xor.primary(AnyM.fromPersistentSetX(PersistentSetX.fromIterable(list))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,persistentSetX,T> persistentsetX(T... values){
        return new XorM<>(Xor.primary(AnyM.fromPersistentSetX(PersistentSetX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,set,T> setX(Set<T> list){
        return new XorM<>(Xor.primary(AnyM.fromSet(SetX.fromIterable(list))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,set,T> setX(T... values){
        return new XorM<>(Xor.primary(AnyM.fromSet(SetX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,list,T> listX(List<T> list){
        return new XorM<>(Xor.primary(AnyM.fromList(ListX.fromIterable(list))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,list,T> listX(T... values){
        return new XorM<>(Xor.primary(AnyM.fromList(ListX.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,stream,T> stream(Stream<T> stream){
        return new XorM<>(Xor.primary(AnyM.fromStream(stream)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,stream,T> stream(T... values){
        return new XorM<>(Xor.primary(AnyM.fromStream(Stream.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,reactiveSeq,T> reactiveSeq(ReactiveSeq<T> stream){
        return new XorM<>(Xor.primary(AnyM.fromStream(stream)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,reactiveSeq,T> reactiveSeq(T... values){
        return new XorM<>(Xor.primary(AnyM.fromStream(ReactiveSeq.of(values))));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,tryType,T> success(T value){
        return new XorM<>(Xor.primary(AnyM.success(value)));
    }
    public static  <W1 extends WitnessType<W1>,X extends Throwable,T> XorM<W1,tryType,T> failure(X value){
        return new XorM<>(Xor.primary(AnyM.failure(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,future,T> futureOf(Supplier<T> value, Executor ex){
        return new XorM<>(Xor.primary(AnyM.futureOf(value,ex)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,completableFuture,T> completableFutureOf(Supplier<T> value, Executor ex){
        return new XorM<>(Xor.primary(AnyM.completableFutureOf(value,ex)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,eval,T> later(Supplier<T> value){
        return new XorM<>(Either.right(AnyM.later(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,optional,T> ofNullable(T value){
        return new XorM<>(Xor.primary(AnyM.ofNullable(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> just(T value){
        return new XorM<>(Xor.primary(AnyM.just(value)));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> none(){
        return new XorM<>(Xor.primary(AnyM.none()));
    }
    public static  <W1 extends WitnessType<W1>,T> XorM<W1,maybe,T> maybeNullabe(T value){
        return new XorM<>(Xor.primary(AnyM.fromMaybe(Maybe.ofNullable(value))));
    }
}

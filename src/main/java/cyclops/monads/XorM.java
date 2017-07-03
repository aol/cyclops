package cyclops.monads;

import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Ior;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class XorM<W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> implements Filters<T>,
                                                                                       Transformable<T>,
                                                                                       Folds<T>,
                                                                                        Zippable<T>,
                                                                                       Publisher<T> {
    
    Xor<AnyM<W1,T>,AnyM<W2,T>> xor;

    public static  <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T> XorM<W1,W2,T> of(Xor<? extends AnyM<? extends W1,? extends T>,
            ? extends AnyM<? extends W2,? extends T>> xor){
        return new XorM<>((Xor)xor);
    }
    public XorM<W1,W2,T> filter(Predicate<? super T> test) {
      return of(xor.map(m -> m.filter(test)).secondaryMap(m->m.filter(test)));
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
    public Zippable<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return null;
    }

    @Override
    public <R> Zippable<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return null;
    }

    @Override
    public <R> Zippable<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return null;
    }

    @Override
    public <R> Zippable<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return null;
    }

    @Override
    public <T2, R> Zippable<R> zip(Iterable<? extends T2> iterable, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return null;
    }

    @Override
    public <T2, R> Zippable<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return null;
    }

    @Override
    public <U, R> Zippable<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return null;
    }

    @Override
    public <U> Zippable<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return null;
    }

    @Override
    public <U> Zippable<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return null;
    }

    @Override
    public <U> Zippable<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return null;
    }

    @Override
    public <S, U> Zippable<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return null;
    }

    @Override
    public <S, U, R> Zippable<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return null;
    }

    @Override
    public <T2, T3, T4> Zippable<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return null;
    }

    @Override
    public <T2, T3, T4, R> Zippable<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return null;
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

    @Override
    public Iterator<T> iterator() {
        return xor.visit(a->a.iterator(),b->b.iterator());
    }
}

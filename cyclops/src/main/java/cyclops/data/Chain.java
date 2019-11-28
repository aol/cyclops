package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentIndexed;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Memoize;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class Chain<T> implements ImmutableList<T>{


    public static abstract class NonEmptyChain<T> extends Chain<T> implements ImmutableList.Some<T>{
        @Override
        public NonEmptyChain<T> appendAll(Iterable<? extends T> value) {
            Chain<? extends T> w = wrap(value);
            return w.isEmpty() ? this : append(this,w);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public NonEmptyChain<T> prepend(T value) {
            return append(singleton(value),this);
        }

        @Override
        public NonEmptyChain<T> append(T value) {
            return append(this,singleton(value));
        }
        @Override
        public  <R> NonEmptyChain<R> map(Function<? super T, ? extends R> fn){
            return new Wrap(ReactiveSeq.fromIterable(this).map(fn));
        }

        @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn1.apply(this);
        }

        @Override
        public NonEmptyChain<T> onEmpty(T value) {
            return this;
        }

        @Override
        public NonEmptyChain<T> onEmptyGet(Supplier<? extends T> supplier) {
            return this;
        }


        private Supplier<Integer> hash;
        @Override
        public int hashCode() {
            if(hash==null){
                Supplier<Integer> local = Memoize.memoizeSupplier(()->{
                    int hashCode = 1;
                    for (T e : this)
                        hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
                    return hashCode;
                });
                hash= local;
                return local.get();
            }
            return hash.get();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj==null)
                return false;
            if (obj == this)
                return true;
            if (obj instanceof Chain) {
                Chain<T> seq1 = this;
                Chain seq2 = (Chain) obj;
                if (seq1.size() != seq2.size()) {
                    return false;
                }
            }
            if(obj instanceof PersistentIndexed) {
                return equalToIteration((Iterable)obj);
            }
            return false;
        }
        @Override
        public NonEmptyChain<T> reverse() {
            return new Wrap(this::reverseIterator);
        }
    }

    public static <T> Chain<T> narrow(Chain<? extends T> broad) {
        return (Chain<T>)broad;
    }

    private final static EmptyChain EMPTY = new EmptyChain();

    public static <T> EmptyChain<T> empty(){
        return EMPTY;
    }
    public static <T> NonEmptyChain<T> singleton(T value){
        return new Singleton<T>(value);
    }
    public static <T> NonEmptyChain<T> append(NonEmptyChain<? extends T> left, NonEmptyChain<? extends T> right){
        return  new Append(left,right);
    }
    public static <T> NonEmptyChain<T> append(Chain<? extends T> left, NonEmptyChain<? extends T> right){
        return left.isEmpty() ?  (NonEmptyChain<T>)right : new Append((NonEmptyChain<T>)left,right);
    }
    public static <T> NonEmptyChain<T> append(NonEmptyChain<? extends T> left, Chain<? extends T> right){
        return right.isEmpty() ?  (NonEmptyChain<T>)left : new Append(left,(NonEmptyChain<T>)right);
    }
    public static <T> Chain<T> wrap(Iterable<T> it){
        return  it.iterator().hasNext() ? new Wrap(it) : new EmptyChain<>();
    }
    public abstract Chain<T> concat(Chain<T> b);
    @Override
    public <R> ImmutableList<R> unitStream(Stream<R> stream) {
        return wrap(ReactiveSeq.fromStream(stream));
    }

    public abstract boolean isEmpty();

    public abstract Iterator<T> iterator();
    @Override
    public <R> Chain<R> unitIterable(Iterable<R> it) {
        return wrap(it);
    }

    @Override
    public EmptyChain<T> emptyUnit() {
        return empty();
    }

    @Override
    public Chain<T> drop(long num) {
        return null;
    }

    @Override
    public Chain<T> take(long num) {
        return wrap(ReactiveSeq.fromIterable(this).take(num));
    }

    @Override
    public NonEmptyChain<T> prepend(T value) {
        return append(singleton(value),this);
    }

    @Override
    public NonEmptyChain<T> append(T value) {
        return append(this,singleton(value));
    }

    @Override
    public abstract  Chain<T> appendAll(Iterable<? extends T> value);

    @Override
    public Chain<T> reverse() {
        return wrap(this::reverseIterator);
    }

    @Override
    public abstract Option<T> get(int pos);

    @Override
    public abstract T getOrElse(int pos, T alt);

    @Override
    public abstract T getOrElseGet(int pos, Supplier<? extends T> alt);

    @Override
    public abstract int size();


    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }

    @Override
    public Chain<T> filter(Predicate<? super T> fn) {
        return wrap(stream().filter(fn));
    }

    @Override
    public abstract <R> Chain<R> map(Function<? super T, ? extends R> fn);

    @Override
    public <R> Chain<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
        return wrap(ReactiveSeq.fromIterable(this).concatMap(fn));
    }

    @Override
    public abstract  <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2);
    @Override
    public abstract  NonEmptyChain<T> onEmpty(T value);

    @Override
    public abstract NonEmptyChain<T> onEmptyGet(Supplier<? extends T> supplier) ;

    @Override
    public Chain<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier){
        return isEmpty() ? wrap(supplier.get()) : this;
    }

    @Override
    public <R> ImmutableList<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return wrap(ReactiveSeq.fromIterable(this).concatMap(mapper));
    }

    @Override
    public <R> ImmutableList<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return wrap(ReactiveSeq.fromIterable(this).mergeMap(fn));
    }

    @Override
    public <R> ImmutableList<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return wrap(ReactiveSeq.fromIterable(this).mergeMap(maxConcurecy,fn));
    }

    public abstract  Iterator<T> reverseIterator();
    private static final class EmptyChain<T> extends Chain<T> implements ImmutableList.None<T>{

        @Override
        public Chain<T> concat(Chain<T> b) {
            return b.isEmpty() ? this : b;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
        @Override
        public Iterator<T> reverseIterator() {
            return iterator();
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new  NoSuchElementException();
                }
            };
        }

        @Override
        public Chain<T> appendAll(Iterable<? extends T> value) {
            Chain<? extends T> w = wrap(value);
            return w.isEmpty() ? this : narrow(w);
        }

        @Override
        public Option<T> get(int pos) {
            return Option.none();
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return alt;
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return alt.get();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public <R> EmptyChain<R> map(Function<? super T, ? extends R> fn) {
            return EMPTY;
        }

        @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }

        @Override
        public NonEmptyChain<T> onEmpty(T value) {
            return Chain.singleton(value);
        }

        @Override
        public NonEmptyChain<T> onEmptyGet(Supplier<? extends T> supplier) {
            return Chain.singleton(supplier.get());
        }



        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj==null)
                return false;
            if (obj == this)
                return true;

            if(obj instanceof PersistentList) {
                return ((PersistentList)obj).size()==0;
            }
            return false;
        }


    }

    private static final class Singleton<T> extends NonEmptyChain<T> {
        public Singleton(T value) {
            this.value = value;
        }

        private final T value;

        @Override
        public NonEmptyChain<T> concat(Chain<T> b) {
            return b.isEmpty() ?  this : new Append(this,(NonEmptyChain<T>)b) ;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                boolean first = true;

                @Override
                public boolean hasNext() {
                    return first;
                }

                @Override
                public T next() {

                    if(!first)
                       throw new  NoSuchElementException();
                   first = false;
                   return value;
                }
            };
        }

        @Override
        public NonEmptyChain<T> appendAll(Iterable<? extends T> values) {
            Chain<? extends T> w = wrap(values);
            return w.isEmpty() ? this : append(this,w);
        }
        public  <R> NonEmptyChain<R> map(Function<? super T, ? extends R> fn){
            return Chain.singleton(fn.apply(value));
        }

        @Override
        public Iterator<T> reverseIterator() {
            return iterator();
        }

        @Override
        public Option<T> get(int pos) {
            return pos == 0 ? Option.some(value) :  Option.none();
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return pos == 0  ? value : alt;
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return pos == 0  ? value : alt.get();
        }

        @Override
        public int size() {
            return 0;
        }


        @Override
        public EmptyChain<T> tail() {
            return Chain.EMPTY;
        }

        @Override
        public T head() {
            return value;
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }
    }
    @AllArgsConstructor
    private static final class Append<T> extends NonEmptyChain<T>{
        private final NonEmptyChain<T> left;
        private final NonEmptyChain<T> right;

        @Override
        public NonEmptyChain<T> concat(Chain<T> b) {
            return b.isEmpty() ? this : new Append(this,(NonEmptyChain<T>)b);
        }



        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                Iterator<T> active = left.iterator();
                boolean first = true;
                @Override
                public boolean hasNext() {
                    boolean res =  active.hasNext();
                    if(!res && first){
                        first = false;
                        active = right.iterator();
                        res  = active.hasNext();
                    }
                    return res;

                }

                @Override
                public T next() {
                    return active.next();
                }
            };
        }

        @Override
        public Option<T> get(int pos) {
            return LazySeq.fromIterable(this).get(pos);
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return LazySeq.fromIterable(this).getOrElse(pos,alt);
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return LazySeq.fromIterable(this).getOrElseGet(pos,alt);
        }

        @Override
        public int size() {
            return ReactiveSeq.fromIterable(this).size();
        }

        @Override
        public Iterator<T> reverseIterator() {
            return new Iterator<T>() {
                Iterator<T> active = right.reverseIterator();
                boolean first = true;
                @Override
                public boolean hasNext() {
                    boolean res =  active.hasNext();
                    if(!res && first){
                        first = false;
                        active = left.reverseIterator();
                        res  = active.hasNext();
                    }
                    return res;

                }

                @Override
                public T next() {
                    return active.next();
                }
            };
        }

        @Override
        public ImmutableList<T> tail() {
            return drop(1);
        }

        @Override
        public T head() {
            return getOrElse(0,null);
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }
    }
    @AllArgsConstructor
    private static final class Wrap<T> extends NonEmptyChain<T>{
        private final Iterable<T> it;

        @Override
        public Chain<T> concat(Chain<T> b) {
            return b.isEmpty() ? this : new Append(this,(NonEmptyChain<T>)b) ;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public Iterator<T> iterator() {
            return it.iterator();
        }

        @Override
        public Option<T> get(int pos) {
            return LazySeq.fromIterable(it).get(pos);
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return LazySeq.fromIterable(it).getOrElse(pos,alt);
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return LazySeq.fromIterable(it).getOrElseGet(pos,alt);
        }

        @Override
        public int size() {
            return ReactiveSeq.fromIterable(it).size();
        }

        @Override
        public Iterator<T> reverseIterator() {
            return LazySeq.fromIterable(it).reverse().iterator();
        }
        @Override
        public ImmutableList<T> tail() {
            return drop(1);
        }

        @Override
        public T head() {
            return getOrElse(0,null);
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }
    }
}

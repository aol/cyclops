package cyclops.data;


import cyclops.control.Maybe;
import cyclops.function.Ordering;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.Enumeration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.collections.tuple.Tuple2;

import java.util.Comparator;
import java.util.function.Function;

import static cyclops.collections.tuple.Tuple.tuple;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Range<T> {
    public final T start;
    public final T end;
    private final Enumeration<T> enm;
    private final Ordering<? super T> comp;

    public static <T> Range<T> range(T start, T end, Enumeration<T> enm, Comparator<? super T> comp){
        return new Range<>(start,end,enm, Ordering.of(comp));
    }
    public static <T extends Comparable<T>> Range<T> range(T start, T end, Enumeration<T> enm){
        return new Range<>(start,end,enm, Ordering.of(Comparator.naturalOrder()));
    }

    public static Range<Integer> range(int start,int end){
        return range(start, end, new Enumeration<Integer>() {
            @Override
            public Maybe<Integer> toEnum(int e) {
                return Maybe.just(e);
            }

            @Override
            public int fromEnum(Integer a) {
                return a;
            }
        });
    }
    public Enumeration<T> enumeration(){
        return enm;
    }
    public boolean startsBefore(Range<T> r){
        return comp.isLessThan(start,r.start);
    }
    public boolean endsAfter(Range<T> r){
        return comp.isGreaterThan(end,r.end);
    }
    public <R> Range<R> map(Function<? super T,? extends R> fn, Enumeration<R> enm, Comparator<? super R> comp){
        R s2 = fn.apply(start);
        R e2 = fn.apply(end);

        return Ordering.of(comp).isLessThanOrEqual(s2,e2)? range( s2,e2,enm,comp) : range(e2,s2,enm,comp);
    }
    public Ordering<? super T> ordering(){
        return comp;
    }
    public Range<T> reverse(){
        return range(end,start,enm,comp);
    }
    public boolean contains(T value){
        if(comp.isGreaterThanOrEqual(value,start) && comp.isLessThanOrEqual(value,end))
            return true;
        return false;
    }
    public boolean contains(Range<T> compareTo){
        return comp.isLessThanOrEqual(start,compareTo.start) && comp.isGreaterThanOrEqual(end,compareTo.end);
    }


    public Tuple2<Range<T>,Maybe<Range<T>>> plusAll(Range<T> range){
        //1 .. x    >=1 .. y
        if(comp.isLessThanOrEqual(start,range.start)){
            if(range.contains(end)){
                return tuple(range(start,range.end,enm,comp), Maybe.none());
            }
            if(end.equals(enm.succ(range.end).orElse(null))){
                return tuple(range(start,end,enm,comp), Maybe.none());
            }
            return tuple(this, Maybe.just(range));

        }else{
            if(this.contains(range.end)){
                return tuple(range(range.start,end,enm,comp), Maybe.none());
            }if(range.end.equals(enm.succ(end).orElse(null))){
                return tuple(range(start,range.end,enm,comp), Maybe.none());
            }
            else{
                return tuple(range, Maybe.just(this));
            }
        }

    }
    public Maybe<Tuple2<Range<T>,Maybe<Range<T>>>> minusAll(Range<T> range){
        //            |         |  <--range
        // |    |
        if (comp.isLessThan(end, range.start)) {
            return Maybe.just(tuple(this, Maybe.none()));
        }
        //                           |   |
        if(comp.isGreaterThanOrEqual(start,range.end)){
            return Maybe.just(tuple(this, Maybe.none()));
        }
        //                 | |
        if(range.contains(this)){
            return Maybe.none();
        }
        if(comp.isLessThanOrEqual(start,range.start)){
            if(comp.isLessThanOrEqual(end,range.end))
                return Maybe.just(tuple(range(start,range.start,enm,comp), Maybe.none()));
            else
                return Maybe.just(tuple(range(start,range.start,enm,comp), Maybe.just(range(range.end,end,enm,comp))));
        }

        //     |              |  <--range
        // |       |
        // |                            |
        //               |            |
        return Maybe.just(tuple(range(range.end,end,enm,comp), Maybe.none()));

    }


    public Maybe<Range<T>> intersection(Range<T> toMerge) {

        T newStart = (T) comp.max(this.start, toMerge.start);
        T newEnd = (T) comp.min(this.end, toMerge.end);
        if (comp.isLessThanOrEqual(start, end))
            return Maybe.just(range(start, end, enm, comp));
        return Maybe.none();
    }

    public ReactiveSeq<T> stream(){
        return ReactiveSeq.iterate(start, e->!end.equals(e), e->enm.succ(e).orElse(null));
    }

    public LazySeq<T> lazySeq(){
        int order = comp.compare(start,end);
        if(order==0){
            return LazySeq.of(start);
        }if(order<0){
            return LazySeq.cons(start,()->range(enm.succ(start).orElse(null),end,enm,comp).lazySeq());
        }
        return LazySeq.cons(start,()->range(enm.pred(start).orElse(null),end,enm,comp).lazySeq());
    }


}

package cyclops.data;


import cyclops.control.Option;
import cyclops.function.Ordering;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple2;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;

import static cyclops.data.tuple.Tuple.tuple;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Range<T> implements Iterable<T>, Serializable{

    private static final long serialVersionUID = 1L;

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
            public Option<Integer> toEnum(int e) {
                return Option.some(e);
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
        if(comp.isGreaterThanOrEqual(value,start) && comp.isLessThan(value,end))
            return true;
        return false;
    }
    public boolean contains(Range<T> compareTo){
        return comp.isLessThanOrEqual(start,compareTo.start) && comp.isGreaterThanOrEqual(end,compareTo.end);
    }


    public Tuple2<Range<T>,Option<Range<T>>> plusAll(Range<T> range){
        //1 .. x    >=1 .. y
        if(comp.isLessThanOrEqual(start,range.start)){
            if(range.contains(end)){
                return tuple(range(start,range.end,enm,comp), Option.none());
            }
            if(end.equals(enm.succ(range.end).orElse(null))){
                return tuple(range(start,end,enm,comp), Option.none());
            }
            return tuple(this, Option.some(range));

        }else{
            if(this.contains(range.end)){
                return tuple(range(range.start,end,enm,comp), Option.none());
            }if(range.end.equals(enm.succ(end).orElse(null))){
                return tuple(range(start,range.end,enm,comp), Option.none());
            }
            else{
                return tuple(range, Option.some(this));
            }
        }

    }
    public Option<Tuple2<Range<T>,Option<Range<T>>>> minusAll(Range<T> range){
        //            |         |  <--range
        // |    |
        if (comp.isLessThan(end, range.start)) {
            return Option.some(tuple(this, Option.none()));
        }
        //                           |   |
        if(comp.isGreaterThanOrEqual(start,range.end)){
            return Option.some(tuple(this, Option.none()));
        }
        //                 | |
        if(range.contains(this)){
            return Option.none();
        }
        if(comp.isLessThanOrEqual(start,range.start)){
            if(comp.isLessThanOrEqual(end,range.end))
                return Option.some(tuple(range(start,range.start,enm,comp), Option.none()));
            else
                return Option.some(tuple(range(start,range.start,enm,comp), Option.some(range(range.end,end,enm,comp))));
        }

        //     |              |  <--range
        // |       |
        // |                            |
        //               |            |
        return Option.some(tuple(range(range.end,end,enm,comp), Option.none()));

    }


    public Option<Range<T>> intersection(Range<T> toMerge) {

        T newStart = (T) comp.max(this.start, toMerge.start);
        T newEnd = (T) comp.min(this.end, toMerge.end);
        if (comp.isLessThanOrEqual(newStart, newEnd))
            return Option.some(range(newStart, newEnd, enm, comp));
        return Option.none();
    }

    public ReactiveSeq<T> stream(){
        return lazySeq().stream();
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


    @Override
    public String toString(){
            return "["+  start + " .. " + end + "]";
    }

    public String allToString(){
        return stream().join(",","[","]");
    }

    @Override
    public Iterator<T> iterator() {
        return lazySeq().iterator();
    }
}

package cyclops.data;

import com.aol.cyclops2.matching.Deconstruct.Deconstruct3;
import com.aol.cyclops2.matching.Sealed2;
import com.aol.cyclops2.types.foldable.Folds;
import cyclops.control.Option;
import cyclops.control.lazy.Trampoline;

import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.Enumeration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;

import static cyclops.control.lazy.Trampoline.done;
import static cyclops.data.tuple.Tuple.tuple;

//Discrete Interval Encoded Tree
public interface DIET<T> extends Sealed2<DIET.Node<T>,DIET.Nil<T>>, Iterable<T>, Folds<T> {

    public static <T> DIET<T> empty(){
        return Nil.INSTANCE;
    }
    public static <T> DIET<T> cons(Range<T> focus){
        return cons(empty(),focus,empty());
    }
    public static <T> DIET<T> cons(DIET<T> left, Range<T> focus, DIET<T> right){
        return new Node(left,focus,right);
    }
    default boolean contains(T value){
        return containsRec(value).result();
    }
    default boolean contains(Range<T> range){
        return containsRec(range).result();
    }
    default DIET<T> add(T value, Enumeration<T> enm, Comparator<? super T> comp){
        return enm.succ(value).visit(s-> {
            return add(Range.range(value, s, enm, comp));
        },()->this);
    }
    Trampoline<Boolean> containsRec(T value);
    Trampoline<Boolean> containsRec(Range<T> range);


    DIET<T> add(Range<T> range);
    DIET<T> merge(DIET<T> merge);
    default DIET<T> remove(T value, Enumeration<T> enm, Comparator<? super T> comp){
        return enm.succ(value).visit(s-> {
            return remove(Range.range(value,s, enm, comp));
        },()->this);
    }
    DIET<T> remove(Range<T> range);

    <R> DIET<R> map(Function<? super T, ? extends R> fn, Enumeration<R> enm, Comparator<? super R> comp);

    LazySeq<T> lazySeq();
    ReactiveSeq<T> stream();
    default Iterator<T> iterator(){
        return stream().iterator();
    }


    boolean isEmpty();
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Node<T> implements DIET<T>,Deconstruct3<DIET<T>,Range<T>,DIET<T>>, Serializable {
        private final DIET<T> left;
        private final Range<T> focus;
        private final DIET<T> right;

        private static final long serialVersionUID = 1L;

        @Override
        public Trampoline<Boolean> containsRec(T value) {
            if(focus.ordering().isLessThan(value,focus.start))
                return left.containsRec(value);
            if(focus.ordering().isGreaterThan(value,focus.end))
                return right.containsRec(value);
            return done(focus.contains(value));

        }

        @Override
        public Trampoline<Boolean> containsRec(Range<T> range) {
            if(focus.contains(range))
                 return done(true);
            if(focus.ordering().isLessThan(range.start,focus.start))
                return left.containsRec(range);
            return right.containsRec(range);
        }
        private Tuple2<Range<T>,DIET<T>> max(){
           return right.fold(s->s.max().transform((r, d)->tuple(r,cons(left,focus,d))), n->tuple(focus,left));
        }
        private Trampoline<Tuple2<DIET<T>,T>> findRightAndEndingPoint(T value){
            //            value
            //                    start         (This is lazyRight Diet, end at old start)
            if(focus.ordering().isLessThan(value,focus.start)) {
                return done(tuple(this, value));
            }
            //            value
            //                   end
            //    start                 (lazyRight is lazyRight diet, end new at old end)
            if(focus.ordering().isLessThanOrEqual(value,focus.end))
                return done(tuple(right,focus.end));
            //             value
            //                                  end
            //                    start              (split rightwad diet recursively)
            return left.fold(p->p.findRightAndEndingPoint(value), leftNil->done(tuple(leftNil,value)));


        }

        private Trampoline<Tuple2<DIET<T>,T>> findLeftAndStartingPoint(T value){
               //            value
               //       end         (This is leftward Diet, start new at end)
               if(focus.ordering().isGreaterThan(value,focus.end)) {
                   return done(tuple(this, value));
               }
               //                        value
               //                                 end
               //<-- lazyLeft  -->    start                 (Left is leftward diet, start new at start)
               if(focus.ordering().isGreaterThanOrEqual(value,focus.start))
                   return done(tuple(left,focus.start));
               //             value
               //                                  end
               //   <--     lazyLeft         --> start               (split leftward diet recursively)
               return left.fold(p->p.findLeftAndStartingPoint(value), leftNil->done(tuple(leftNil,value)));


        }
        @Override
        public DIET<T> add(Range<T> range) {
            Tuple2<Range<T>, Option<Range<T>>> t = focus.plusAll(range);
            return t._2().visit(s-> t._1()==focus? cons(left,focus,right.add(s)) : cons(left.add(s),focus,right),()->{

                //create new expanded range and rebalance the trees
                Tuple2<DIET<T>,T> leftAndStart = left.fold(l->l.findLeftAndStartingPoint(t._1().start).get(), n->tuple(n,t._1().start));
                Tuple2<DIET<T>,T> rightAndEnd = right.fold(l->l.findRightAndEndingPoint(t._1().end).get(), n->tuple(n,t._1().start));

                return cons(leftAndStart._1(), Range.range(leftAndStart._2(), rightAndEnd._2(), focus.enumeration(), focus.ordering()), rightAndEnd._1());

            });
        }

        @Override
        public DIET<T> merge(DIET<T> merge) {
            return merge.fold(s-> {
                        Tuple2<Range<T>, DIET<T>> t2 = max();
                        DIET<T> x =  cons(t2._2(), t2._1(), merge);
                        return x;
                    }
            ,n->this);
        }

        @Override
        public DIET<T> remove(Range<T> range) {

            Option<Tuple2<Range<T>, Option<Range<T>>>> x = focus.minusAll(range);
            System.out.println("X is " + x);
            return x.visit(s->s.transform((r, mr) ->  mr.visit(sr ->{
                        Range<T> x1 = r;
                        DIET<T> a1 = right;

                       return cons( left,r, empty()).merge(cons(empty(),sr,  right));
                    },
                                                                () -> cons(range.startsBefore(focus) ? left.remove(range) : left, r, range.endsAfter(focus) ? right.remove(range) : right))
                                            ),
                    //none
                    ()->left.merge(right));
//
        }

        @Override
        public <R> DIET<R> map(Function<? super T, ? extends R> fn, Enumeration<R> enm, Comparator<? super R> comp) {
            Range<R> r = focus.map(fn,enm,comp);
            DIET<R> l2 = left.map(fn,enm,comp);
            return l2.add(r).merge(right.map(fn,enm,comp));

        }

        @Override
        public LazySeq<T> lazySeq() {
            return left.lazySeq().append(()->focus.lazySeq()).append(()->right.lazySeq());
        }

        @Override
        public ReactiveSeq<T> stream() {
            return left.stream().appendS(focus.stream()).appendS(right.stream());
        }


        public boolean isEmpty(){
            return false;
        }

        @Override
        public <R> R fold(Function<? super Node<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2) {
            return fn1.apply(this);
        }

        @Override
        public String toString() {
            return "[{" + left +
                    "}," + focus +
                    ",{" + right +
                    "}]";
        }

        @Override
        public Tuple3<DIET<T>, Range<T>, DIET<T>> unapply() {
            return tuple(left,focus,right);
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Nil<T> implements DIET<T>, Serializable {
        public final static Nil INSTANCE = new Nil();

        private static final long serialVersionUID = 1L;

        @Override
        public Trampoline<Boolean> containsRec(T value) {
            return done(false);
        }

        @Override
        public Trampoline<Boolean> containsRec(Range<T> range) {
            return done(false);
        }

        @Override
        public DIET<T> add(Range<T> range) {
            return DIET.cons(range);
        }

        @Override
        public DIET<T> merge(DIET<T> merge) {
            return merge;
        }

        @Override
        public DIET<T> remove(Range<T> range) {
            return this;
        }

        @Override
        public <R> DIET<R> map(Function<? super T, ? extends R> fn, Enumeration<R> enm, Comparator<? super R> comp) {
            return INSTANCE;
        }

        @Override
        public LazySeq<T> lazySeq() {
            return LazySeq.empty();
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public <R> R fold(Function<? super Node<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }

        @Override
        public String toString() {
            return "{}";
        }
    }
}

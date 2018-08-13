package cyclops.data;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import com.oath.cyclops.hkt.DataWitness.zipper;
import cyclops.data.tuple.Tuple3;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
@Getter
@Wither
public class Zipper<T> implements Iterable<T>,
                                    Higher<zipper,T> {

    private final ImmutableList<T> left;
    private final T point;
    private final ImmutableList<T> right;


    public static <T> Zipper<T> of(ImmutableList<T> left, T value, ImmutableList<T> right){
        return new Zipper<>(left,value,right);
    }
    public static <T> Zipper of(ReactiveSeq<T> left, T value, ReactiveSeq<T> right){
        return new Zipper<>(LazySeq.fromStream(left),value, LazySeq.fromStream(right));
    }

    public boolean isStart(){
        return left.isEmpty();
    }
    public boolean isEnd(){
        return right.isEmpty();
    }
    public <R> Zipper<R> map(Function<? super T, ? extends R> fn){
        return of(left.map(fn),fn.apply(point),right.map(fn));
    }
    public <R> Zipper<R> zip(Zipper<T> zipper, BiFunction<? super T, ? super T, ? extends R> fn){
        ImmutableList<R> newLeft = left.zip(zipper.left.stream(),fn);
        R newPoint = fn.apply(point, zipper.point);
        ImmutableList<R> newRight = right.zip(zipper.right.stream(),fn);
        return of(newLeft,newPoint,newRight);
    }

    public  Zipper<Tuple2<T,T>> zip(Zipper<T> zipper){
        return zip(zipper, Tuple::tuple);
    }

    public Zipper<T> start() {
        Option<Zipper<T>> result = Option.some(this);
        Option<Zipper<T>> next = result;
        while(next.isPresent()){
                next = result.flatMap(p->p.previous());
                if(next.isPresent())
                    result = next;

        }
        return result.orElse(this);
    }
    public Zipper<T> end() {
        Option<Zipper<T>> result = Option.some(this);
        Option<Zipper<T>> next = result;
        while(next.isPresent()){
            next = result.flatMap(p->p.next());
            if(next.isPresent())
                result = next;
        }
        return result.orElse(this);
    }
    public int index(){
        return left.size();
    }
    public Option<Zipper<T>> position(int index) {
        Zipper<T> result = this;
        while (index != result.index()) {
            if (result.index() < index && !result.isEnd()) {
                result = result.next(result);
            } else if (result.index() > index && !result.isStart()) {
                result = result.previous(result);
            } else {
                return Option.none();
            }
        }
        return Option.some(result);
    }
    public <R> Option<Zipper<T>> next(){
        return right.fold(c-> Option.some(new Zipper(left.append(point), c.head(), c.tail())), nil-> Option.none());
    }
    public <R> Zipper<T> next(Zipper<T> alt){
        return next().orElse(alt);
    }
    public <R> Zipper<T> previous(Zipper<T> alt){
        return previous().orElse(alt);
    }
    public Zipper<T> cycleNext() {
        return left.fold(cons->right.fold(c->next().orElse(this), nil->{

            return of(LazySeq.empty(),cons.head(),cons.tail().append(point));
        }),nil->this);

    }
    public Zipper<T> cyclePrevious() {
        return right.fold(cons->left.fold(c->previous().orElse(this), nil->{
            ImmutableList.Some<T> reversed = cons.reverse();
            return of(reversed.tail().reverse().prepend(point),reversed.head(), LazySeq.empty());
        }),nil->this);
    }
    public <R> Option<Zipper<T>> previous(){
        return left.fold(c-> Option.some(new Zipper(c.take(c.size()-1),c.last(null) ,right.prepend(point))), nil-> Option.none());
    }

    public Zipper<T> left(T value){
        return new Zipper<>(left,value,right.prepend(point));
    }
    public Zipper<T> right(T value){
        return new Zipper<>(left.append(point),value,right);
    }
    public Zipper<T> deleteAllLeftAndRight() {
        return new Zipper<>(LazySeq.empty(), point, LazySeq.empty());
    }
    public Option<Zipper<T>> deleteLeft() {

        return left.fold(c->right.fold(c2-> Option.some(of(c.dropRight(1),c.last(null),right)), n-> Option.some(of(c.dropRight(1),c.last(null),right))),
                n->right.fold(c-> Option.some(of(left,c.head(),c.tail())), n2-> Option.none()));
    }
    public Option<Zipper<T>> deleteRight() {

        return right.fold(c->left.fold(c2-> Option.some(of(left,c.head(),c.tail())), n-> Option.some(of(left,c.head(),c.tail()))),
                n->left.fold(c-> Option.some(of(c.tail(),c.head(),right)), n2-> Option.none()));
    }



    public Zipper<T> filterLeft(Predicate<? super T> predicate) {
        return of(left.filter(predicate),point,right);
    }


    public Zipper<T> filterRight(Predicate<? super T> predicate) {
        return of(left,point,right.filter(predicate));
    }


    public Tuple3<ImmutableList<T>,T, ImmutableList<T>> split() {
        return Tuple.tuple(left,point, right);
    }
    public ImmutableList<T> list(){
        return right.prepend(point).prependAll(left);
    }

    public ReactiveSeq<T> stream(){
        return left.stream().append(point).appendStream(right.stream());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zipper<?> zipper = (Zipper<?>) o;

        return Objects.equals(left, zipper.left) &&
                Objects.equals(point, zipper.point) &&
                Objects.equals(right, zipper.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, point, right);
    }

    @Override
    public String toString() {
        String l = left.stream().join(", ","[","");
        String p = ",>>" + point.toString() + "<<";

        String r = right.stream().join(", ",",","]");
       return l + p +r ;
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }
}

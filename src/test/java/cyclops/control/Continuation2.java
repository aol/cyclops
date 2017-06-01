package cyclops.control;

import cyclops.collections.box.Mutable;
import cyclops.function.Fn0;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Iterator;
import java.util.Optional;

@AllArgsConstructor
public class Continuation2<T> implements Iterable<T> {


    @Wither
    private final ContSupplierComplete<T> remainderOfWorkToBeDone;

    public Continuation2(T value, ContSupplier<T> work){

        local.set(work);
        this.remainderOfWorkToBeDone = ()-> work !=null ? Tuple.tuple(Maybe.just(value),work.get()) : Tuple.tuple(Maybe.just(value),null);
    }
    public Continuation2(ContSupplier<T> work) {

        System.out.println("Work is " + work);
        local.set(work);
        this.remainderOfWorkToBeDone = () -> Tuple.tuple(Maybe.none(), work.get());
    }

    public Ior<T,Continuation2<T>> proceed() {
        Tuple2<Maybe<T>, Continuation2<T>> t2 = remainderOfWorkToBeDone.get();

       return t2.v1.visit(v->Ior.both(v, t2.v2),()->
                Ior.primary(t2.v2));
    }


    static ThreadLocal<ContSupplier<?>> local = ThreadLocal.withInitial(()->null);
    public static <T> Continuation2<T> yield(T value, ContSupplier<T> remainderOfWorkToBeDone){
        local.set(remainderOfWorkToBeDone);
        return new Continuation2<T>(value,remainderOfWorkToBeDone );
    }
    public static <T> Continuation2<T> yield(T value){
        return new Continuation2<T>(value,(ContSupplier<T>)local.get() );
    }
    /**
    public static <T> Continuation2<T> yield(T value, GeneratorSupplier<T> nextA, GeneratorSupplier<T> nextB){

        return new Continuation2<T>(value,()->nextA.get().withRemainderOfWorkToBeDone(nextB) );
    }
    public static <T> Continuation2<T> yield(T value, GeneratorSupplier<T> nextA, GeneratorSupplier<T> nextB, GeneratorSupplier<T> nextC){

        return new Continuation2<T>(value,()->nextA.get().withRemainderOfWorkToBeDone(()->nextB) );
    }

    public static <T> Continuation2<T> yieldSequence(T value, GeneratorSupplier<T>... sequence){
        Mutable<GeneratorSupplier<T>> next1 = Mutable.of(sequence[0]);
        Mutable<GeneratorSupplier<T>> next2 = Mutable.of(sequence[1]);
        return new Continuation2<T>(value,()->{
            GeneratorSupplier<T> nextA = next1.get();
            GeneratorSupplier<T> nextB = calcNextB(next2.get();
            return nextA.get().withRemainderOfWorkToBeDone(nextB);
        } );
    }
     **/
    public static <T> Continuation2<T> yieldAndStop(T value){
        System.out.println("Yeild and stop!");
        return new Continuation2<T>(value,null );
    }
    public static <T> Continuation2<T> suspend(ContSupplier<T> value){
        return new Continuation2<T>(value);
    }

    @Override
    public Iterator<T> iterator() {
        Continuation2<T> parent = this;
        return new Iterator<T>() {
            Tuple2<Maybe<T>, Continuation2<T>> current;
            Continuation2<T> nextCont = parent;
            boolean ready = false;
            @Override
            public boolean hasNext() {

                while(!ready){
                    if(nextCont.remainderOfWorkToBeDone==null || current.v2==null){

                        return false;
                    }
                    current = nextCont.remainderOfWorkToBeDone.get();
                    nextCont = current.v2;
                    System.out.println("Next cont! " + nextCont);

                    ready = current.v1().isPresent();
                }
                return true;

            }

            @Override
            public T next() {
                ready = false;
                return current.v1().get();
            }
        };
    }
    static interface ContSupplier<T> extends Fn0<Continuation2<T>>
    {

    }
    static interface ContSupplierComplete<T> extends Fn0<Tuple2<Maybe<T>,Continuation2<T>>>
    {

    }


    static int i = 10;
    public static void main(String[] args){

        ReactiveSeq.fromIterable(suspend(()->yield(i++)))
                    .take(3)
                    .printOut();

        ReactiveSeq.fromIterable(suspend(()->i<20 ? yield(i++) : yieldAndStop(i)))
                   .printOut();
        /**
        ReactiveSeq.fromIterable(suspend(()->
                yieldSequence(1,
                        () -> yield(2),
                        () -> yield(3),
                        () -> yield(4),
                        () -> yield(5))
        ))
                .printOut();
         **/

   /**     ReactiveSeq.fromIterable(suspend(()->yieldAndStop(i)))
                .printOut();
**/
    }
}

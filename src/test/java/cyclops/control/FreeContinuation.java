package cyclops.control;

import com.aol.cyclops2.types.functor.Transformable;
import cyclops.collections.box.Mutable;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
public class FreeContinuation<T> {//} implements Iterable<T> {

    @AllArgsConstructor
    static class State<U,T> implements Transformable<T> {
        private final Maybe<U> value;
        private final T next;
        @Override
        public <R> Transformable<R> map(Function<? super T, ? extends R> fn) {
            return new State<>(value,fn.apply(next));
        }
    }
    private final Unrestricted<T> cont;


    public Ior<T,FreeContinuation<T>> proceed() {
        Xor<? extends State<T, ?>, T> next = cont.resume(t -> (State<T, ?>) t);
       return null;
    }


   // static ThreadLocal<ContSupplier<?>> local = ThreadLocal.withInitial(()->null);
   /**
    public static <T> FreeContinuation<T> yield(T value, ContSupplier<T> remainderOfWorkToBeDone){
        local.set(remainderOfWorkToBeDone);
        return new FreeContinuation<T>(value,remainderOfWorkToBeDone );
    }
    public static <T> FreeContinuation<T> yield(T value){
        return new FreeContinuation<T>(value,(ContSupplier<T>)local.get() );
    }
    public static <T> FreeContinuation<T> yield(T value, ContSupplier<T> nextA, ContSupplier<T> nextB){

        return new FreeContinuation<T>(value,()->nextA.get().withRemainderOfWorkToBeDone(nextB) );
    }
    public static <T> ContSupplier<T> include(ContSupplier<T> nextA,ContSupplier<T> nextB){

        return new FreeContinuation<T>(value,()->nextA.get().withRemainderOfWorkToBeDone(nextB) );
    }

    public static <T> FreeContinuation<T> yieldSequence(T value, ContSupplier<T>... sequence){
        Mutable<ContSupplier<T>> next1 = Mutable.of(sequence[0]);
        Mutable<ContSupplier<T>> next2 = Mutable.of(sequence[1]);
        return new FreeContinuation<T>(value,()->{
            ContSupplier<T> nextA = next1.get();
            ContSupplier<T> nextB = calcNextB(next2.get();
            return nextA.get().withRemainderOfWorkToBeDone(nextB);
        } );
    }

    public static <T> FreeContinuation<T> yieldAndStop(T value){
        System.out.println("Yeild and stop!");
        return new FreeContinuation<T>(value,null );
    }
    public static <T> FreeContinuation<T> suspend(ContSupplier<T> value){
        return new FreeContinuation<T>(value);
    }

    @Override
    public Iterator<T> iterator() {
        FreeContinuation<T> parent = this;
        return new Iterator<T>() {
            FreeContinuation<T> nextCont = parent;
            boolean ready = nextCont.value.isPresent();
            @Override
            public boolean hasNext() {
                while(!ready){
                    if(nextCont.remainderOfWorkToBeDone==null){

                        return false;
                    }
                    nextCont = proceed().get();
                    System.out.println("Next cont! " + nextCont);

                    ready = nextCont.value.isPresent();
                }
                return true;

            }

            @Override
            public T next() {
                ready = false;
                return nextCont.value.get();
            }
        };
    }

    static interface ContSupplier<T> extends Supplier<FreeContinuation<T>>
    {

    }

**/

}

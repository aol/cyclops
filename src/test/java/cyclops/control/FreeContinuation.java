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


   // static ThreadLocal<GeneratorSupplier<?>> local = ThreadLocal.withInitial(()->null);
   /**
    public static <T> FreeContinuation<T> yield(T value, GeneratorSupplier<T> remainderOfWorkToBeDone){
        local.set(remainderOfWorkToBeDone);
        return new FreeContinuation<T>(value,remainderOfWorkToBeDone );
    }
    public static <T> FreeContinuation<T> yield(T value){
        return new FreeContinuation<T>(value,(GeneratorSupplier<T>)local.get() );
    }
    public static <T> FreeContinuation<T> yield(T value, GeneratorSupplier<T> nextA, GeneratorSupplier<T> nextB){

        return new FreeContinuation<T>(value,()->nextA.get().withRemainderOfWorkToBeDone(nextB) );
    }
    public static <T> GeneratorSupplier<T> include(GeneratorSupplier<T> nextA,GeneratorSupplier<T> nextB){

        return new FreeContinuation<T>(value,()->nextA.get().withRemainderOfWorkToBeDone(nextB) );
    }

    public static <T> FreeContinuation<T> yieldSequence(T value, GeneratorSupplier<T>... sequence){
        Mutable<GeneratorSupplier<T>> next1 = Mutable.of(sequence[0]);
        Mutable<GeneratorSupplier<T>> next2 = Mutable.of(sequence[1]);
        return new FreeContinuation<T>(value,()->{
            GeneratorSupplier<T> nextA = next1.get();
            GeneratorSupplier<T> nextB = calcNextB(next2.get();
            return nextA.get().withRemainderOfWorkToBeDone(nextB);
        } );
    }

    public static <T> FreeContinuation<T> yieldAndStop(T value){
        System.out.println("Yeild and stop!");
        return new FreeContinuation<T>(value,null );
    }
    public static <T> FreeContinuation<T> suspend(GeneratorSupplier<T> value){
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

    static interface GeneratorSupplier<T> extends Supplier<FreeContinuation<T>>
    {

    }

**/

}

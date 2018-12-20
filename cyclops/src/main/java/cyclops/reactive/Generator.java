package cyclops.reactive;

import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.data.Range;
import cyclops.function.BooleanFunction0;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import com.oath.cyclops.types.stream.ToStream;
import cyclops.function.Function0;
import cyclops.function.Function1;
import lombok.experimental.Wither;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Sequence generator, for simple and complex sequences
 *
 * <pre>
 *     {@code
 *      import static cyclops.reactive.Generator.*;
 *     int i = 100;
        ReactiveSeq.generate(suspend(infinitely(),s->s.yield(i++)))
                   .take(6)
                    .printOut();

}
100
101
102
103
104
105
106
 {@code
     ReactiveSeq.generate(suspend((Integer i)->i!=4, s-> {


                    Generator<Integer> gen1 = suspend(times(5),s2->s2.yield(i++));
                    Generator<Integer> gen2 = suspend(times(2),s2->s2.yield(k--));

                    return s.yieldAll(gen1.stream(),gen2.stream());
                                                            }
                    ))
                .take(12)
                .printOut();
 }
 * </pre>
 *
 * @param <T>
 */
@Deprecated
public class Generator<T> implements Iterable<T>, ToStream<T> {
    private final Suspended suspended;
    private final Maybe<T> value;
    @Wither
    private final GeneratorSupplier<T> remainderOfWorkToBeDone;


    public Generator(Suspended suspended, Maybe<T> value, GeneratorSupplier<T> remainderOfWorkToBeDone){
        this.value = value;
        this.suspended=suspended;
        this.remainderOfWorkToBeDone = remainderOfWorkToBeDone;
    }
    public Generator(Suspended suspended, T value, GeneratorSupplier<T> work){
        this.value = Maybe.just(value);
        this.suspended=suspended;
        this.suspended.local = work;
        this.remainderOfWorkToBeDone = work;
    }
    public Generator(Suspended suspended, GeneratorSupplier<T> work){
        this.value = Maybe.nothing();
        this.suspended=suspended;
        this.suspended.local = work;
        this.remainderOfWorkToBeDone = work;
    }

    public ConvertableSequence<T> to(){
        return new ConvertableSequence(this);
    }

    public <R> R to(Function<? super Iterable<? super T>,? extends R> reduce){
        return reduce.apply(this);
    }

    Ior<T,Generator<T>> proceed() {
       return value.fold(v->Ior.both(v, remainderOfWorkToBeDone.get()),()->
                Ior.right(remainderOfWorkToBeDone.get()));
    }


    public T extract(){
        T ex = value.orElse(null); //unsafe call but should never be null
        suspended.value=ex;
        return ex;
    }

    public static class Suspended<T>{
        private final Suspended<T> parent;
        private GeneratorSupplier<T> local;
        private T value;

        public Suspended(){
            parent=null;
        }
        public Suspended(Suspended<T> parent){
            this.parent = parent;
        }

        public Maybe<T> maybe(){
            return Maybe.ofNullable(value);
        }

        public T current(){
            return value;
        }

        public Generator<T> yield(T value, GeneratorSupplier<T> remainderOfWorkToBeDone){
            //this.local = remainderOfWorkToBeDone;
            Generator<T> res = new Generator<T>(this,value,remainderOfWorkToBeDone );
            local = nullValue();
            return res;
        }
        public Generator<T> yield(T value){
            Generator<T> res = new Generator<T>(this, value, (GeneratorSupplier<T>) local);
            local = nullValue();
            return res;
        }
        public Generator<T> yieldAll(T... values){
            if(values.length==1){
                return Generator.empty(parent);
            }
            T first = values[0];
            return yieldAll(first,ReactiveSeq.of(values).drop(1));

        }
        public Generator<T> yieldAll(Iterable<T>... next){
            if(next.length==0)
                return empty(this);
            ReactiveSeq[] array= new ReactiveSeq[next.length];
            int index =0;
            for(Iterable<T> it : next){
                array[index++] = ReactiveSeq.fromIterable(it);
            }

            return yieldAll(ReactiveSeq.concat(array));

        }
        public Generator<T> yieldAll(Iterable<T> next){


            Iterator<T> it = next.iterator();
            if(it.hasNext()) {
                T nextA = it.next();
                return yieldAll(nextA, () -> it);
            }
            return empty(this);
        }
        public Generator<T> yieldAll(T value, Iterable<T> next){


            Iterator<T> it = next.iterator();

            if(it.hasNext()) {
                T nextA = it.next();
                local = () -> {
                    setNestedContinutationV(it);

                    return yield(nextA);
                };
            }

            return yield(value);
        }
        public Generator<T> yield(T value, Predicate<? super T> predicate){
            return new Generator<T>(this,value,(GeneratorSupplier<T>)local );
        }
        public Generator<T> yield(T value, GeneratorSupplier<T> nextA, GeneratorSupplier<T> nextB){


            local = ()->{
                local = ()->{
                    return nextB.get();
                };

                return nextA.get();
            };

            return yield(value);
        }
        public Generator<T> yield(T value, GeneratorSupplier<T>... next){
            Iterator<GeneratorSupplier<T>> it = Arrays.asList(next).iterator();
            GeneratorSupplier<T> nextA = it.next();
            local = ()->{
                setNestedContinutation(it);

                return nextA.get();
            };

            return yield(value);
        }
        public Generator<T> yieldRef(T value, Supplier<T>... next){


            Iterator<Supplier<T>> it = Arrays.asList(next).iterator();
            Supplier<T> nextA = it.next();
            local = ()->{
                setNestedContinutationS(it);

                return yield(nextA.get());
            };

            return yield(value);
        }
        private  void setNestedContinutationS(Iterator<Supplier<T>> it) {
            if(it.hasNext()) {
                Supplier<T> nextB = it.next();
                local = () -> {
                    setNestedContinutationS(it);
                    return yield(nextB.get());

                };
            }else{
                local = nullValue();
            }
        }
        private  void setNestedContinutationV(Iterator<T> it) {
            if(it.hasNext()) {
                T nextB = it.next();
                local = () -> {
                    setNestedContinutationV(it);
                    return yield(nextB);

                };
            }else{

                local = nullValue();
            }
        }

        private  void setNestedContinutation(Iterator<GeneratorSupplier<T>> it) {
            if(it.hasNext()) {
                GeneratorSupplier<T> nextB = it.next();
                local = () -> {
                    setNestedContinutation(it);
                    return nextB.get();

                };
            }else{
                local = nullValue();
            }
        }
        private GeneratorSupplier<T> nullValue(){
            if(parent==null)
                return null;
            return parent.local;
        }

        public Generator<T> yieldAndStop(T value){
            Suspended<T> stopped = new Suspended<>();
            stopped.value=value;
            stopped.local = local;
            return new Generator<T>(stopped,value,null );
        }
        public Generator<T> stop(){
            Suspended<T> stopped = new Suspended<>();

            stopped.local = local;
            return new Generator<T>(stopped,Maybe.nothing(),null );
        }

        Suspended<T> copy() {
           Suspended<T> res =  parent!=null ? new Suspended<T>(parent.copy()) : new Suspended<>();
           res.local =local;
           res.value = value;
           return res;
        }
    }

    public static <T> Generator<T> suspend(GeneratorFunction<T> value){
        Suspended<T> suspended = new Suspended<>();
        suspended.local=()->value.apply(suspended);
        Generator<T> res = new Generator<T>(suspended,suspended.local);
        suspended.local = null;

        return res;
    }

    public static <T> Generator<T> suspend(BooleanSupplier pred, GeneratorFunction<T> value){
        Suspended<T> suspended = new Suspended<>(new Suspended<>());

        suspended.local=()->value.apply(suspended);
        suspended.parent.local = ()->value.apply(suspended);
        Generator<T> res = new Generator<T>(suspended,suspended.local);
        GeneratorSupplier<T> current = suspended.local;
        suspended.parent.local = ()->{


            if(pred.getAsBoolean()){
                return current.get();
            }
            return empty(suspended.parent);
        };


        return res;
    }


    public static <T> Generator<T> suspend(Predicate<? super T> pred, GeneratorFunction<T> value){
        Suspended<T> suspended = new Suspended<>(new Suspended<>());

        suspended.local=()->value.apply(suspended);
        suspended.parent.local = ()->value.apply(suspended);

        Generator<T> res = new Generator<T>(suspended,suspended.local);
        GeneratorSupplier<T> current = suspended.local;
        suspended.parent.local = ()->{

            if(pred.test(suspended.value)){
                return current.get();
            }
            return empty(suspended.parent);
        };


        return res;
    }

    private static <T> Generator<T> empty(Suspended<T> suspended) {
        return new Generator(suspended,Maybe.nothing(),null);
    }

    public static <T> Generator<T> suspendRef(Supplier<T> value){
        return suspend(s->s.yield(value.get()));
    }
    public static <T> Generator<T> suspendRef(BooleanSupplier pred, Supplier<T> value){
        return suspend(pred,s->s.yield(value.get()));
    }
    public static <T> Generator<T> suspendRef(Predicate<? super T> pred, Supplier<T> value){
        return suspend(pred,s->s.yield(value.get()));
    }

    private Generator<T> copy(){
        return new Generator<>(suspended.copy(),value,this.remainderOfWorkToBeDone);
    }
    @Override
    public Iterator<T> iterator() {

        return new Iterator<T>() {
            Generator<T> nextCont = copy();
            boolean ready = nextCont.value.isPresent();
            @Override
            public boolean hasNext() {
                while(!ready){
                    if(nextCont.remainderOfWorkToBeDone==null){
                        return false;
                    }
                    nextCont = nextCont.proceed().orElse(null);

                    ready = nextCont==null? false : nextCont.value.isPresent();
                }
                return true;

            }

            @Override
            public T next() {
                ready = false;
                return nextCont.extract();
            }
        };
    }

    public static BooleanFunction0 infinitely(){
        return ()->true;
    }
    public static BooleanFunction0 times(int times){
        int[] num = {0};
        return ()->++num[0]<times-1;
    }
    public static BooleanFunction0 once(){
        return times(1);
    }

    public static interface GeneratorSupplier<T> extends Function0<Generator<T>>
    {

    }
    public static interface GeneratorFunction<T> extends Function1<Suspended<T>,Generator<T>>
    {

    }






}

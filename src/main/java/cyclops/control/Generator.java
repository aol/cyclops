package cyclops.control;

import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.foldable.ConvertableSequence;
import com.aol.cyclops2.types.mixins.Printable;
import com.aol.cyclops2.types.stream.ToStream;
import com.sun.tools.javah.Gen;
import cyclops.function.Fn0;
import cyclops.function.Fn1;
import cyclops.stream.ReactiveSeq;
import lombok.experimental.Wither;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class Generator<T> implements Iterable<T>, ToStream<T> {
    private final Suspended suspended;
    private final Maybe<T> value;
    @Wither
    private final ContSupplier<T> remainderOfWorkToBeDone;


    public Generator(Suspended suspended, Maybe<T> value, ContSupplier<T> remainderOfWorkToBeDone){
        this.value = value;
        this.suspended=suspended;
        this.remainderOfWorkToBeDone = remainderOfWorkToBeDone;
    }
    public Generator(Suspended suspended, T value, ContSupplier<T> work){
        this.value = Maybe.just(value);
        this.suspended=suspended;
        this.suspended.local = work;
        this.remainderOfWorkToBeDone = work;
    }
    public Generator(Suspended suspended, ContSupplier<T> work){
        this.value = Maybe.none();
        this.suspended=suspended;
        this.suspended.local = work;
        this.remainderOfWorkToBeDone = work;
    }

    public ConvertableSequence<T> to(){
        return new ConvertableSequence(this);
    }

    Ior<T,Generator<T>> proceed() {
       return value.visit(v->Ior.both(v, remainderOfWorkToBeDone.get()),()->
                Ior.primary(remainderOfWorkToBeDone.get()));
    }


    public T extract(){
        T ex = value.get();
        suspended.value=ex;
        return ex;
    }

    static class Suspended<T>{
        private final Suspended<T> parent;
        private ContSupplier<T>  local;
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

        public Generator<T> yield(T value, ContSupplier<T> remainderOfWorkToBeDone){
            //this.local = remainderOfWorkToBeDone;
            Generator<T> res = new Generator<T>(this,value,remainderOfWorkToBeDone );
            local = nullValue();
            return res;
        }
        public Generator<T> yield(T value){
            Generator<T> res = new Generator<T>(this, value, (ContSupplier<T>) local);
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
            return new Generator<T>(this,value,(ContSupplier<T>)local );
        }
        public Generator<T> yield(T value, ContSupplier<T> nextA, ContSupplier<T> nextB){


            local = ()->{
                local = ()->{
                    return nextB.get();
                };

                return nextA.get();
            };

            return yield(value);
        }
        public Generator<T> yield(T value, ContSupplier<T>... next){
            Iterator<ContSupplier<T>> it = Arrays.asList(next).iterator();
            ContSupplier<T> nextA = it.next();
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

        private  void setNestedContinutation(Iterator<ContSupplier<T>> it) {
            if(it.hasNext()) {
                ContSupplier<T> nextB = it.next();
                local = () -> {
                    setNestedContinutation(it);
                    return nextB.get();

                };
            }else{
                local = nullValue();
            }
        }
        private ContSupplier<T> nullValue(){
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

        public Suspended<T> copy() {
           Suspended<T> res =  parent!=null ? new Suspended<T>(parent.copy()) : new Suspended<>();
           res.local =local;
           res.value = value;
           return res;
        }
    }

    public static <T> Generator<T> suspend(ContFunction<T> value){
        Suspended<T> suspended = new Suspended<>();
        suspended.local=()->value.apply(suspended);
        Generator<T> res = new Generator<T>(suspended,suspended.local);
        suspended.local = null;

        return res;
    }

    public static <T> Generator<T> suspend(BooleanSupplier pred, ContFunction<T> value){
        Suspended<T> suspended = new Suspended<>(new Suspended<>());

        suspended.local=()->value.apply(suspended);
        suspended.parent.local = ()->value.apply(suspended);
        Generator<T> res = new Generator<T>(suspended,suspended.local);
        ContSupplier<T> current = suspended.local;
        suspended.parent.local = ()->{


            if(pred.getAsBoolean()){
                return current.get();
            }
            return empty(suspended.parent);
        };


        return res;
    }
    public static <T> Generator<T> suspend(Predicate<? super T> pred, ContFunction<T> value){
        Suspended<T> suspended = new Suspended<>(new Suspended<>());

        suspended.local=()->value.apply(suspended);
        suspended.parent.local = ()->value.apply(suspended);

        Generator<T> res = new Generator<T>(suspended,suspended.local);
        ContSupplier<T> current = suspended.local;
        suspended.parent.local = ()->{

            if(pred.test(suspended.value)){
                return current.get();
            }
            return empty(suspended.parent);
        };


        return res;
    }

    private static <T> Generator<T> empty(Suspended<T> suspended) {
        return new Generator(suspended,Maybe.none(),null);
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
                    nextCont = nextCont.proceed().get();

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

    public static ComposableBooleanSupplier infinitely(){
        return ()->true;
    }
    public static ComposableBooleanSupplier times(int times){
        int[] num = {0};
        return ()->++num[0]<times-1;
    }
    public static ComposableBooleanSupplier once(){
        return times(1);
    }

    static interface ContSupplier<T> extends Fn0<Generator<T>>
    {

    }
    static interface ContFunction<T> extends Fn1<Suspended<T>,Generator<T>>
    {

    }

    static interface ComposableBooleanSupplier extends BooleanSupplier{

        default ComposableBooleanSupplier before(Runnable r){
            return ()->{
                r.run();
                return getAsBoolean();
            };
        }
        default ComposableBooleanSupplier after(Runnable r){
            return ()->{

                boolean res = getAsBoolean();
                r.run();
                return res;
            };
        }

    }



    public static void main(String[] args){





/**
         ReactiveSeq.<Integer>fromIterable(suspend(new ContFunction<Integer>() {
                    int runningTotal =0;

                   @Override
                   public Generator<Integer> apply(Suspended<Integer> s) {
                       System.out.println("Top level - should see this only once!");
                       return s.yield(1,
                               () -> {
                                    runningTotal = runningTotal +5;
                                    return s.yield(runningTotal+2);
                               },
                               () -> s.yield(runningTotal+3),
                               () -> s.yieldAndStop(runningTotal+6));

                   }
               }

        )).take(6)
                .printOut();
**/
        /**
        ReactiveSeq.fromIterable(suspend(()-> yield(1,__ -> true)
        )).take(4)
                .printOut();
         **/

        /**
        ReactiveSeq.fromIterable(suspend(()->
                yieldSequence(1,
                        () -> yield(2),
                        () -> yield(3),
                        () -> yield(4),
                        () -> yield(5))
        ))
                .printOut();**/



   /**     ReactiveSeq.fromIterable(suspend(()->yieldAndStop(i)))
                .printOut();
**/
    }
}

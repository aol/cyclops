package cyclops.monads;

import java.util.Deque;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops2.internal.adapters.*;
import cyclops.collections.immutable.*;
import cyclops.control.*;
import cyclops.async.Future;
import cyclops.control.lazy.Either;
import cyclops.control.lazy.Either3;
import cyclops.control.lazy.Either4;
import cyclops.control.lazy.Either5;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.mutable.DequeX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.QueueX;
import cyclops.collections.mutable.SetX;
import cyclops.collections.mutable.SortedSetX;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import cyclops.typeclasses.Product;

public interface Witness {
   static interface MonadicValueWitness<W extends MonadicValueWitness<W>>  extends WitnessType<W>{
        
    }
    static interface StreamWitness<W extends StreamWitness<W>>  extends WitnessType<W>{

    }
   static interface CollectionXWitness<W extends CollectionXWitness<W>>  extends WitnessType<W>{
       
   }
    public static <T> Identity<T> identity(AnyM<identity,? extends T> anyM){
        return anyM.unwrap();
    }

    public static <T> Stream<T> stream(AnyM<stream,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> ReactiveSeq<T> toReactiveSeq(AnyM<stream,? extends T> anyM){
        return ReactiveSeq.fromStream(anyM.unwrap());
    }
    public static <T> ReactiveSeq<T> reactiveSeq(AnyM<reactiveSeq,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> FutureStream<T> futureStream(AnyM<futureStream,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Streamable<T> streamable(AnyM<streamable,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> VectorX<T> vectorX(AnyM<vectorX,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> PersistentQueueX<T> persistentQueueX(AnyM<persistentSetX,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> PersistentSetX<T> persistentSetX(AnyM<persistentSetX,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> OrderedSetX<T> orderedSetX(AnyM<orderedSetX,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> BagX<T> bagX(AnyM<bagX,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> LinkedListX<T> linkedListX(AnyM<linkedListX,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> ListX<T> list(AnyM<list,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Deque<T> deque(AnyM<deque,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Set<T>set(AnyM<set,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Queue<T> queue(AnyM<queue,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> SortedSet<T> sortedSet(AnyM<sortedSet,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Optional<T> optional(AnyM<optional,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T,W extends CollectionXWitness<W>> CollectionX<T> collectionX(AnyM<W,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T,W extends MonadicValueWitness<W>> MonadicValue<T> monadicValue(AnyM<W,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Eval<T> eval(AnyM<eval,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Maybe<T> maybe(AnyM<maybe,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> Future<T> future(AnyM<future,? extends T> anyM){
        return anyM.unwrap();
    }
    
    public static <T> CompletableFuture<T> completableFuture(AnyM<completableFuture,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <ST,T> Xor<ST,T> xor(AnyM<xor,? extends T> anyM){
        return anyM.unwrap();
    }


    public static <L,R> Either<L,R> either(AnyM<either,? extends R> anyM){
        return anyM.unwrap();
    }
    public static <L1,L2,R> Either3<L1,L2,R> either3(AnyM<either3,? extends R> anyM){
        return anyM.unwrap();
    }
    public static <L1,L2,L3,R> Either4<L1,L2,L3,R> either4(AnyM<either4,? extends R> anyM){
        return anyM.unwrap();
    }
    public static <L1,L2,L3,L4,R> Either5<L1,L2,L3,L4,R> either5(AnyM<either5,? extends R> anyM){
        return anyM.unwrap();
    }
    public static <ST,T> Ior<ST,T> ior(AnyM<ior,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <X extends Throwable,T> Try<T,X> Try(AnyM2<tryType,X,? extends T> anyM){
        return anyM.unwrap();
    }


    public static enum stream implements StreamWitness<stream>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<stream> adapter() {
            return StreamAdapter.stream;
        }
        
    }
    public static enum futureStream implements StreamWitness<futureStream>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<futureStream> adapter() {
            return StreamAdapter.futureStream;
        }

    }
    public static enum reactiveSeq implements StreamWitness<reactiveSeq>{

        REACTIVE,CO_REACTIVE;

        @Override
        public  FunctionalAdapter<reactiveSeq> adapter() {
            if(ordinal()==0)
                return ReactiveAdapter.reactiveSeq;
            return StreamAdapter.reactiveSeq;


        }
        
    }

    public static enum sortedSet implements CollectionXWitness<sortedSet>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<sortedSet> adapter() {
            return new CollectionXAdapter<Witness.sortedSet>(SortedSetX::empty,
                    SortedSetX::of,SortedSetX::fromIterator,this);
        }
        
    }
    public static enum set implements CollectionXWitness<set>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<set> adapter() {
            return new CollectionXAdapter<Witness.set>(SetX::empty,
                    SetX::of,SetX::fromIterator,this);
        }
        
    }
    public static enum list implements CollectionXWitness<list>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<list> adapter() {
            return new CollectionXAdapter<Witness.list>(ListX::empty,
                    ListX::of,ListX::fromIterator,this);
        }
        
    }
    public static enum linkedListX implements CollectionXWitness<linkedListX>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<linkedListX> adapter() {
            return new CollectionXAdapter<linkedListX>(LinkedListX::empty,
                    LinkedListX::of, LinkedListX::fromIterator,this);
        }
        
    }
    public static enum vectorX implements CollectionXWitness<vectorX>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<vectorX> adapter() {
            return new CollectionXAdapter<vectorX>(VectorX::empty,
                    VectorX::of, VectorX::fromIterator,this);
        }
        
    }
    public static enum persistentQueueX implements CollectionXWitness<persistentQueueX>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<persistentQueueX> adapter() {
            return new CollectionXAdapter<persistentQueueX>(PersistentQueueX::empty,
                    PersistentQueueX::of, PersistentQueueX::fromIterator,this);
        }

    }
    public static enum persistentSetX implements CollectionXWitness<persistentSetX>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<persistentSetX> adapter() {
            return new CollectionXAdapter<persistentSetX>(PersistentSetX::empty,
                    PersistentSetX::of, PersistentSetX::fromIterator,this);
        }

    }
    public static enum orderedSetX implements CollectionXWitness<orderedSetX>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<orderedSetX> adapter() {
            return new CollectionXAdapter<orderedSetX>(OrderedSetX::empty,
                    OrderedSetX::of, OrderedSetX::fromIterator,this);
        }

    }
    public static enum bagX implements CollectionXWitness<bagX>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<bagX> adapter() {
            return new CollectionXAdapter<bagX>(BagX::empty,
                    BagX::of, BagX::fromIterator,this);
        }

    }
    public static enum deque implements CollectionXWitness<deque>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<deque> adapter() {
            return new CollectionXAdapter<Witness.deque>(DequeX::empty,
                    DequeX::of,DequeX::fromIterator,this);
        }
        
    }
    public static enum queue implements CollectionXWitness<queue>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<queue> adapter() {
            return new CollectionXAdapter<Witness.queue>(QueueX::empty,
                    QueueX::of,QueueX::fromIterator,this);
        }
        
    }
    public static enum streamable implements WitnessType<streamable>{
        INSTANCE;

        @Override
        public FunctionalAdapter<streamable> adapter() {
            return StreamableAdapter.streamable;
        }
        
    }

    public static enum tryType implements MonadicValueWitness<tryType>{
        INSTANCE;


        @Override
        public FunctionalAdapter<tryType> adapter() {
            return new MonadicValueAdapter<tryType>(()->Try.failure(null),
                    Try::success,Try::fromIterable,false,this);
        }
        
    }
    public static enum ior implements MonadicValueWitness<ior>{
        INSTANCE;


        @Override
        public FunctionalAdapter<ior> adapter() {
            return new MonadicValueAdapter<Witness.ior>(()->Ior.secondary(null),
                    Ior::primary,Ior::fromIterable,false,this);
        }
        
    }
    public static enum either implements MonadicValueWitness<either>{
        INSTANCE;


        @Override
        public FunctionalAdapter<either> adapter() {
            return new MonadicValueAdapter<Witness.either>(()-> Either.left(null),
                    Either::right,Either::fromIterable,false,this);
        }

    }
    public static enum either3 implements MonadicValueWitness<either3>{
        INSTANCE;


        @Override
        public FunctionalAdapter<either3> adapter() {
            return new MonadicValueAdapter<Witness.either3>(()-> Either3.left1(null),
                    Either3::right,Either3::fromIterable,false,this);
        }

    }
    public static enum either4 implements MonadicValueWitness<either4>{
        INSTANCE;


        @Override
        public FunctionalAdapter<either4> adapter() {
            return new MonadicValueAdapter<Witness.either4>(()-> Either4.left1(null),
                    Either4::right, Either4::fromIterable,false,this);
        }

    }
    public static enum either5 implements MonadicValueWitness<either5>{
        INSTANCE;


        @Override
        public FunctionalAdapter<either5> adapter() {
            return new MonadicValueAdapter<Witness.either5>(()-> Either5.left1(null),
                    Either5::right, Either5::fromIterable,false,this);
        }

    }
    public static enum xor implements MonadicValueWitness<xor>{
        INSTANCE;


        @Override
        public FunctionalAdapter<xor> adapter() {
            return new MonadicValueAdapter<Witness.xor>(()->Xor.secondary(null),
                    Xor::primary,Xor::fromIterable,false,this);
        }
        
    }
    public static enum eval implements MonadicValueWitness<eval>{
        INSTANCE;


        @Override
        public FunctionalAdapter<eval> adapter() {
            return new MonadicValueAdapter<Witness.eval>(()->Eval.now(null),
                    Eval::now,Eval::fromIterable,false,this);
        }
        
    }
    public static enum maybe implements MonadicValueWitness<maybe>{
        INSTANCE;


        @Override
        public FunctionalAdapter<maybe> adapter() {
            return new MonadicValueAdapter<Witness.maybe>(()->Maybe.none(),
                    Maybe::just,Maybe::fromIterable,true,this);
        }
        
    }
    public static enum future implements MonadicValueWitness<future>{
        INSTANCE;


        @Override
        public FunctionalAdapter<future> adapter() {
            return new MonadicValueAdapter<Witness.future>(Future::future,
                    Future::ofResult, Future::fromIterable,false,this);
        }
        
    }
    public static enum completableFuture implements WitnessType<completableFuture>{
        INSTANCE;

        @Override
        public FunctionalAdapter<completableFuture> adapter() {
            return FutureAdapter.completableFuture;
        }
        
    }
    
    public static enum optional implements WitnessType<optional>{
        INSTANCE;

        @Override
        public FunctionalAdapter<optional> adapter() {
            return OptionalAdapter.optional;
        }
        
    }

    public static enum reader implements WitnessType<reader>{
        INSTANCE;

        @Override
        public FunctionalAdapter<reader> adapter() {
            return null;
        }

    }
    public static enum supplier implements WitnessType<supplier>{
        INSTANCE;

        @Override
        public FunctionalAdapter<supplier> adapter() {
            return null;
        }

    }
    public static enum yoneda implements WitnessType<yoneda>{
        INSTANCE;

        @Override
        public FunctionalAdapter<yoneda> adapter() {
            return null;
        }

    }
    public static enum coyoneda implements WitnessType<coyoneda>{
        INSTANCE;

        @Override
        public FunctionalAdapter<coyoneda> adapter() {
            return null;
        }

    }
    public static enum kleisli implements WitnessType<kleisli>{
        INSTANCE;

        @Override
        public FunctionalAdapter<kleisli> adapter() {
            return null;
        }

    }
    public static enum rws implements WitnessType<rws>{
        INSTANCE;

        @Override
        public FunctionalAdapter<rws> adapter() {
            return null;
        }

    }
    public static enum state implements WitnessType<state>{
        INSTANCE;

        @Override
        public FunctionalAdapter<state> adapter() {
            return null;
        }

    }
    public static enum writer implements WitnessType<writer>{
        INSTANCE;

        @Override
        public FunctionalAdapter<writer> adapter() {
            return null;
        }

    }
    public static enum constant implements WitnessType<constant>{
        INSTANCE;

        @Override
        public FunctionalAdapter<constant> adapter() {
            return null;
        }

    }
    public static enum cofree implements WitnessType<cofree>{
        INSTANCE;

        @Override
        public FunctionalAdapter<cofree> adapter() {
            return null;
        }

    }
    public static enum identity implements WitnessType<identity>{
        INSTANCE;

        @Override
        public FunctionalAdapter<identity> adapter() {
            return new IdentityAdapter();
        }

    }
    public static enum product implements WitnessType<product>{
        INSTANCE;

        @Override
        public FunctionalAdapter<product> adapter() {
            return null;
        }

    }
}

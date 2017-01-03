package cyclops.monads;

import java.util.Deque;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import cyclops.control.Eval;
import cyclops.async.Future;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.either.Either;
import cyclops.control.either.Either3;
import cyclops.control.either.Either4;
import cyclops.control.either.Either5;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import cyclops.control.Try;
import cyclops.control.Xor;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.QueueX;
import cyclops.collections.SetX;
import cyclops.collections.SortedSetX;
import com.aol.cyclops2.internal.comprehensions.comprehenders.CollectionXAdapter;
import com.aol.cyclops2.internal.comprehensions.comprehenders.FutureAdapter;
import com.aol.cyclops2.internal.comprehensions.comprehenders.MonadicValueAdapter;
import com.aol.cyclops2.internal.comprehensions.comprehenders.OptionalAdapter;
import com.aol.cyclops2.internal.comprehensions.comprehenders.StreamAdapter;
import com.aol.cyclops2.internal.comprehensions.comprehenders.StreamableAdapter;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;

public interface Witness {
   static interface MonadicValueWitness<W extends MonadicValueWitness<W>>  extends WitnessType<W>{
        
    }
    static interface StreamWitness<W extends StreamWitness<W>>  extends WitnessType<W>{

    }
   static interface CollectionXWitness<W extends CollectionXWitness<W>>  extends WitnessType<W>{
       
   }
    public static <T> Stream<T> stream(AnyM<stream,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> ReactiveSeq<T> reactiveSeq(AnyM<stream,? extends T> anyM){
        return ReactiveSeq.fromStream(anyM.unwrap());
    }
    public static <T> Streamable<T> streamable(AnyM<streamable,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> PVectorX<T> pvector(AnyM<pvector,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <T> PStackX<T> pstack(AnyM<pstack,? extends T> anyM){
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
    public static <X extends Throwable,T> Try<T,X> Try(AnyM<tryType,? extends T> anyM){
        return anyM.unwrap();
    }
    public static enum stream implements StreamWitness<stream>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<stream> adapter() {
            return StreamAdapter.stream;
        }
        
    }
    public static enum reactiveSeq implements StreamWitness<reactiveSeq>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<reactiveSeq> adapter() {
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
    public static enum pstack implements CollectionXWitness<pstack>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<pstack> adapter() {
            return new CollectionXAdapter<Witness.pstack>(PStackX::empty,
                    PStackX::of,PStackX::fromIterator,this);
        }
        
    }
    public static enum pvector implements CollectionXWitness<pvector>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<pvector> adapter() {
            return new CollectionXAdapter<Witness.pvector>(PVectorX::empty,
                    PVectorX::of,PVectorX::fromIterator,this);
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
            return new MonadicValueAdapter<Witness.tryType>(()->Try.failure(null),
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
}

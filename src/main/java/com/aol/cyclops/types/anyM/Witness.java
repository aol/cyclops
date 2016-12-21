package com.aol.cyclops.types.anyM;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.FluentSequenceX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.internal.comprehensions.comprehenders.CollectionXAdapter;
import com.aol.cyclops.internal.comprehensions.comprehenders.FutureAdapter;
import com.aol.cyclops.internal.comprehensions.comprehenders.MonadicValueAdapter;
import com.aol.cyclops.internal.comprehensions.comprehenders.OptionalAdapter;
import com.aol.cyclops.internal.comprehensions.comprehenders.StreamAdapter;
import com.aol.cyclops.internal.comprehensions.comprehenders.StreamableAdapter;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.extensability.FunctionalAdapter;

public interface Witness {
   static interface MonadicValueWitness<W extends MonadicValueWitness<W>>  extends WitnessType<W>{
        
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
    public static <T> FutureW<T> future(AnyM<future,? extends T> anyM){
        return anyM.unwrap();
    }
    
    public static <T> CompletableFuture<T> completableFuture(AnyM<completableFuture,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <ST,T> Xor<ST,T> xor(AnyM<xor,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <ST,T> Ior<ST,T> ior(AnyM<ior,? extends T> anyM){
        return anyM.unwrap();
    }
    public static <X extends Throwable,T> Try<T,X> Try(AnyM<tryType,? extends T> anyM){
        return anyM.unwrap();
    }
    public static enum stream implements WitnessType<stream>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<stream> adapter() {
            return StreamAdapter.stream;
        }
        
    }
    public static enum reactiveSeq implements WitnessType<stream>{
        INSTANCE;

        @Override
        public  FunctionalAdapter<stream> adapter() {
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
            return new MonadicValueAdapter<Witness.future>(FutureW::future,
                    FutureW::ofResult,FutureW::fromIterable,false,this);
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

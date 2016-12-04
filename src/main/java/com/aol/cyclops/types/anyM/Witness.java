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
import com.aol.cyclops.control.FeatureToggle;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.internal.comprehensions.comprehenders.CompletableFutureComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.DequeComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.EvalComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.FeatureToggleComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.FutureFunctorComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.IorComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.ListComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaybeComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.OptionalComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.QueueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.SetComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.StreamComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.StreamableComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.TryComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.XorComprehender;
import com.aol.cyclops.types.extensability.Comprehender;

public interface Witness {
    
    public static <T> Stream<T> stream(AnyM<stream,T> anyM){
        return anyM.unwrap();
    }
    public static <T> Streamable<T> streamable(AnyM<streamable,T> anyM){
        return anyM.unwrap();
    }
    public static <T> List<T> list(AnyM<list,T> anyM){
        return anyM.unwrap();
    }
    public static <T> Deque<T> deque(AnyM<deque,T> anyM){
        return anyM.unwrap();
    }
    public static <T> Set<T>set(AnyM<set,T> anyM){
        return anyM.unwrap();
    }
    public static <T> Queue<T> queue(AnyM<queue,,T> anyM){
        return anyM.unwrap();
    }
    public static <T> SortedSet<T> sortedSet(AnyM<sortedSet,T> anyM){
        return anyM.unwrap();
    }
    public static <T> Optional<T> optional(AnyM<optional,T> anyM){
        return anyM.unwrap();
    }
    public static <T> Eval<T> eval(AnyM<eval,T> anyM){
        return anyM.unwrap();
    }
    public static <T> Maybe<T> maybe(AnyM<maybe,T> anyM){
        return anyM.unwrap();
    }
    public static <T> FutureW<T> futureW(AnyM<futureW,T> anyM){
        return anyM.unwrap();
    }
    public static <T> FeatureToggle<T> featureToggle(AnyM<featureToggle,T> anyM){
        return anyM.unwrap();
    }
    public static <T> CompletableFuture<T> completableFuture(AnyM<completableFuture,T> anyM){
        return anyM.unwrap();
    }
    public static <ST,T> Xor<ST,T> xor(AnyM<xor,T> anyM){
        return anyM.unwrap();
    }
    public static <ST,T> Ior<ST,T> ior(AnyM<ior,T> anyM){
        return anyM.unwrap();
    }
    public static <X extends Throwable,T> Try<T,X> Try(AnyM<tryType,T> anyM){
        return anyM.unwrap();
    }
    public static enum stream implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) StreamComprehender.INSTANCE;
        }
        
    }
    public static enum streamable implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) StreamableComprehender.INSTANCE;
        }
        
    }
    public static enum set implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) SetComprehender.INSTANCE;
        }
        
    }
    public static enum list implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) ListComprehender.INSTANCE;
        }
        
    }
    public static enum deque implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) DequeComprehender.INSTANCE;
        }
        
    }
    public static enum queue implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) QueueComprehender.INSTANCE;
        }
        
    }
    public static enum tryType implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) TryComprehender.INSTANCE;
        }
        
    }
    public static enum ior implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) IorComprehender.INSTANCE;
        }
        
    }
    public static enum xor implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) XorComprehender.INSTANCE;
        }
        
    }
    public static enum completableFuture implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) CompletableFutureComprehender.INSTANCE;
        }
        
    }
    public static enum eval implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) EvalComprehender.INSTANCE;
        }
        
    }
    public static enum featureToggle implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) FeatureToggleComprehender.INSTANCE;
        }
        
    }
    public static enum futureW implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) FutureFunctorComprehender.INSTANCE;
        }
        
    }
    public static enum maybe implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) MaybeComprehender.INSTANCE;
        }
        
    }
    public static enum optional implements WitnessType{
        INSTANCE;

        @Override
        public <T> Comprehender<T> adapter() {
            return (Comprehender<T>) OptionalComprehender.INSTANCE;
        }
        
    }
}

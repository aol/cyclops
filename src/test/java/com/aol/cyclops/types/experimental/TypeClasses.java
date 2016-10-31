package com.aol.cyclops.types.experimental;

import static com.aol.cyclops.types.higherkindedtypes.type.constructors.ListType.narrow;
import static com.aol.cyclops.types.higherkindedtypes.type.constructors.ListType.widen;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.derive4j.hkt.__;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.higherkindedtypes.Higher;
import com.aol.cyclops.types.higherkindedtypes.type.constructors.ListType;
import com.aol.cyclops.types.higherkindedtypes.type.constructors.SetType;
import com.aol.cyclops.util.function.PartialApplicator;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Experimental implementation of Haskell type classes, using Witnes types as 
 * expounded in HighJ https://github.com/highj/highj. Attempting to make things more
 * readable with meaningful names and intermediate interfaces where possible.
 * 
 * 
 * @author johnmcclean
 *
 */
public interface TypeClasses {
    
    /**
     * Functor type class, performs a transformation operation over the supplied data structure
     * 
     * @author johnmcclean
     *
     * @param <CRE>
     */
    static interface _Functor<CRE>{
        
        /**
         * Transform the supplied data structure using the supplied transformation function
         * 
         * <pre>
         * {@code 
         *  ListX<Integer> listx = ListX.of(1,2,3);
            ListType<Integer> mapped1 =listFunctor().map(a->a+1, listx);
            mapped1.add(1);
            ListX<Integer> listxMapped = mapped1.list();
         * }
         * </pre>
         * 
         * @param fn Transformation function
         * @param ds Datastructure to transform
         * @return
         */
        public <T,R> Higher<CRE,R> map(Function<? super T,? extends R> fn, Higher<CRE,T> ds);
        
        default <T,R> Function<Function<? super T,? extends R>,Higher<CRE,R>> partialMap(Higher<CRE,T> ds){
            return PartialApplicator.partial2b(ds, this::map);
        }
        
    }
    static interface _Unit<CRE>{
        public <T> Higher<CRE,T> unit(T value,Higher<CRE,T> ds);
    }
    static interface _Monad<CRE> extends _Functor<CRE>, _Unit<CRE>{
       
        public <T,R> Higher<CRE,R> bind(Function<? super T,? extends Higher<CRE,R>> fn,Higher<CRE,T> functor);
        
    }
    static interface _Apply<CRE> extends _Functor<CRE>{
        public <T,R> Higher<CRE,R> ap(Higher<CRE, Function<? super T, ? extends R>> fn, Higher<CRE,T> apply);
    }
    static interface _Applicative<CRE> extends _Apply<CRE>, _Unit<CRE>{
        
    }
   
    
    
    static interface Lists {
        static interface ListFunctor extends _Functor<ListType.listx>{
           
            @Override
            default <T, R> ListType<R> map(Function<? super T,? extends R> fn, Higher<ListType.listx, T> functor){
                return ListType.widen(ListType.narrow(functor).map(fn));
            }
        }
        static interface ListUnit extends _Unit<ListType.listx>{
           
            @Override
            default <T> ListType<T> unit(T value,Higher<ListType.listx, T> ds){
                return ListType.widen(narrow(ds).unit(value));
            }
        }
        static interface ListMonad extends _Monad<ListType.listx>, ListFunctor,ListUnit{
           
            @Override
            default <T,R> ListType<R> bind(Function<? super T,? extends Higher<ListType.listx,R>> fn,Higher<ListType.listx,T> ds){
                return ListType.widen(narrow(ds).flatMap(fn.andThen(ListType::narrow)));
            }
        }
        static interface ListApply extends _Apply<ListType.listx>{
            
            @Override
            default <T,R> ListType<R> ap(Higher<ListType.listx, Function<? super T, ? extends R>> fn, 
                                        Higher<ListType.listx,T> apply){
                return ListType.widen(narrow(fn).zip(narrow(apply), (fn1,data)->fn1.apply(data)));
            }
        }
        static interface ListApplicative extends _Applicative<ListType.listx>, ListApply, ListUnit, ListFunctor{

            
            
        }
       
        static  ListFunctor listFunctor(){
            
            return new ListFunctor(){};
        }
        static  <T> ListFunctor listFunctor(List<T> list){
            
            return new ListFunctor(){};
        }
        static  ListMonad listMonad(){
            
            return new ListMonad(){};
        }
        static  ListApplicative listApplicative(){
            
            return new ListApplicative(){};
        }
        
        
        default void test(){
            ListX<Integer> listx = ListX.of(1,2,3);
            Higher<ListType.listx,Integer> mapped1 =listFunctor().map(a->a+1, widen(listx));
            mapped1.convert(ListType::narrow)
                   .map(x->x*2)
                   .add(1);
            listFunctor().partialMap(listx)
                         .apply(a->a+1);
                        
            
            listApplicative().ap(ListX.of(a->a*2),ListX.of(1,2,3));
        }
    }
    static interface General {
        
        
        
        
        
        
       
       
        @AllArgsConstructor
        static class GeneralFunctor<CRE,A> implements _Functor<CRE>{
            Function<Higher<CRE,?>,A> narrowFn;
            BiFunction<A,Function,Higher<CRE,?>> mapRef;
            //widenFn needed everywhere? -> add to base class
            @Override
            public <T,R> Higher<CRE,R> map(Function<? super T,? extends R> fn, Higher<CRE,T> ds){
                return (Higher<CRE, R>) mapRef.apply(narrowFn.apply(ds),fn);
            }
        }
        static  <CRE,A> GeneralFunctor<CRE,A> functor(Function<Higher<CRE,?>,A> narrowFn, BiFunction<A,Function,Higher<CRE,?>>  f){
        
            return new GeneralFunctor<>(narrowFn,f);
             
        }
        @AllArgsConstructor
        static class GeneralUnit<CRE,A> implements _Unit<CRE> {
            Function<A,Higher<CRE,?>> widenFn; //assumes target class may not implement HKT interface
            Function<Object,A> unitRef;
           
            @Override
            public <T> Higher<CRE,T> unit(T value,Higher<CRE, T> ds){
                return (Higher<CRE, T>) widenFn.apply(unitRef.apply((Object)ds));
            }
        }
        static  <CRE,A> GeneralUnit<CRE,A> unit(Function<A,Higher<CRE,?>> widenFn, Function<Object,A> unit){
       
            return new GeneralUnit<CRE,A>(widenFn,unit);
        }
       
        @AllArgsConstructor
        @Builder
        static class GeneralMonad<CRE,A> implements  _Monad<CRE> {
            
            GeneralFunctor<CRE,A> functor;
            GeneralUnit<CRE,A> unit;
            BiFunction<A,Function,Higher<CRE,?>> bindRef; //reference to bind / flatMap method
            
           
            @Override
            public <T,R> Higher<CRE,R> bind(Function<? super T,? extends Higher<CRE,R>> fn,Higher<CRE,T> ds){
                return (Higher<CRE, R>) bindRef.apply(functor.narrowFn.apply(ds),fn.andThen(functor.narrowFn));
            }
           
            
            @Override
            public <T> Higher<CRE,T> unit(T value,Higher<CRE, T> ds){
                return unit.unit(value, ds);
                
            }


            @Override
            public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
               return functor.map(fn, ds);
            }
        }
        @AllArgsConstructor
        @Builder
        static class GeneralApply<CRE,A> implements _Apply<CRE>{
            GeneralFunctor<CRE,A> functor;
            
            TriFunction<A,A,BiFunction,Higher<CRE,?>> applyRef;
            @Override
            public <T,R> Higher<CRE,R> ap(Higher<CRE, Function<? super T, ? extends R>> fn, 
                                        Higher<CRE,T> apply){
                BiFunction<Function,Object,Object> combiner =(fn1,data)->fn1.apply(data);
                return (Higher<CRE, R>) applyRef.apply(functor.narrowFn.apply(fn),functor.narrowFn.apply(apply),combiner);
            }
            @Override
            public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
                return functor.map(fn, ds);
            }
        }
        static  <CRE,A> GeneralMonad<CRE,A> monad(GeneralFunctor<CRE,A> functor,GeneralUnit<CRE,A> unit,BiFunction<A,Function,Higher<CRE,?>> bindRef) {
       
            return new GeneralMonad<CRE,A>(functor,unit,bindRef);
            
        }
        static  <CRE,A> GeneralApply<CRE,A> apply(GeneralFunctor<CRE,A> functor,TriFunction<A,A,BiFunction,Higher<CRE,?>> applyRef) {
            
            return new GeneralApply<CRE,A>(functor,applyRef);
            
        }
        
        
        
    }

   
}

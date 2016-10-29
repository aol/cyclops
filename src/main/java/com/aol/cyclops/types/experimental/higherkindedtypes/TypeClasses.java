package com.aol.cyclops.types.experimental.higherkindedtypes;

import java.util.function.Function;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;

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
        public <T,R> Higher2<CRE,R> map(Function<? super T,? extends R> fn, Higher2<CRE,T> ds);
        
    }
    static interface _Unit<CRE>{
        public <T> Higher2<CRE,T> unit(T value,Higher2<CRE,T> ds);
    }
    static interface _Monad<CRE> extends _Functor<CRE>, _Unit<CRE>{
       
        public <T,R> Higher2<CRE,R> bind(Function<? super T,? extends Higher2<CRE,R>> fn,Higher2<CRE,T> functor);
        
    }
    static interface _Apply<CRE> extends _Functor<CRE>{
        public <T,R> Higher2<CRE,R> ap(Higher2<CRE, Function<? super T, ? extends R>> fn, Higher2<CRE,T> apply);
    }
    static interface _Applicative<CRE> extends _Apply<CRE>{
        public <T> Higher2<CRE,T> pure(T value);
    }
   
    
    
    static interface Lists {
        static interface ListFunctor extends _Functor<ListType.listx>{
            default <T> ListX<T> narrow(Higher2<ListType.listx, T> list){
                return (ListX<T>)list;
            }
            @Override
            default <T, R> ListType<R> map(Function<? super T,? extends R> fn, Higher2<ListType.listx, T> functor){
                return narrow(functor).map(fn);
            }
        }
        static interface ListUnit extends _Unit<ListType.listx>{
            default <T> ListX<T> narrow(Higher2<ListType.listx, T> list){
                return (ListX<T>)list;
            }
            @Override
            default <T> ListType<T> unit(T value,Higher2<ListType.listx, T> ds){
                return narrow(ds).unit(value);
            }
        }
        static interface ListMonad extends _Monad<ListType.listx>, ListFunctor,ListUnit{
            default <T> ListX<T> narrow(Higher2<ListType.listx, T> list){
                return (ListX<T>)list;
            }
            @Override
            default <T,R> ListType<R> bind(Function<? super T,? extends Higher2<ListType.listx,R>> fn,Higher2<ListType.listx,T> ds){
                return narrow(ds).flatMap(fn.andThen(this::narrow));
            }
        }
        static interface ListApply extends _Apply<ListType.listx>{
            default <T> ListX<T> narrow(Higher2<ListType.listx, T> list){
                return (ListX<T>)list;
            }
            @Override
            default <T,R> ListType<R> ap(Higher2<ListType.listx, Function<? super T, ? extends R>> fn, 
                                        Higher2<ListType.listx,T> apply){
                return narrow(fn).zip(narrow(apply), (fn1,data)->fn1.apply(data));
            }
        }
        static interface ListApplicative extends _Applicative<ListType.listx>, ListApply, ListFunctor{
            default <T> ListX<T> narrow(Higher2<ListType.listx, T> list){
                return (ListX<T>)list;
            }
            @Override
            default <T> ListType<T> pure(T value){
               return ListX.of(value);
            }
        }
       
        static  ListFunctor listFunctor(){
            
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
            ListType<Integer> mapped1 =listFunctor().map(a->a+1, listx);
            mapped1.add(1);
            listApplicative().ap(ListX.of(a->a*2),ListX.of(1,2,3));
        }
    }
    static interface Sets {
        static interface SetFunctor extends _Functor<SetType.setx>{
            default <T> SetX<T> narrow(Higher2<SetType.setx, T> list){
                return (SetX<T>)list;
            }
            default <T, R> SetType<R> map(Function<? super T,? extends R> fn, Higher2<SetType.setx, T> functor){
                return narrow(functor).map(fn);
            }
        }
       
        static  SetFunctor setFunctor(){
            
            return new SetFunctor(){};
        }
        
        default void test(){
            SetX<Integer> setx = SetX.of(1,2,3);
            SetType<Integer> mapped1 =setFunctor().map(a->a+1, setx);
            mapped1.add(1);
        }
    }
}

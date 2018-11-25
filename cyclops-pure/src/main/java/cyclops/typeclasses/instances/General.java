package cyclops.typeclasses.instances;

import com.oath.cyclops.hkt.Higher;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * General instance used to create typeclass instances from Java 8 method references
 *
 * @author johnmcclean
 *
 */
@Deprecated
public interface General {
    @AllArgsConstructor
    static class GeneralFunctor<CRE,A,B> implements Functor<CRE> {

        BiFunction<? extends Higher<CRE,A>,Function<? super A, ? extends B>,? extends Higher<CRE,B>> mapRef;


        <T,R> BiFunction<Higher<CRE,T>,Function<? super T, ? extends R>,Higher<CRE,R>> mapRef(){
            return (BiFunction)mapRef;
        }

        @Override
        public <T,R> Higher<CRE,R> map(Function<? super T,? extends R> fn, Higher<CRE,T> ds){
            return this.<T,R>mapRef().apply(ds,fn);
        }
    }
    static  <CRE,T,R> GeneralFunctor<CRE,T,R> functor(BiFunction<? extends Higher<CRE, T>, Function<? super T, ? extends R>, ? extends Higher<CRE, R>> f){

        return new GeneralFunctor<>(f);

    }
    @AllArgsConstructor
    static class GeneralPure<CRE,A> implements Pure<CRE> {

        Function<A,Higher<CRE,A>> unitRef;

         <T> Function<T,Higher<CRE,T>> unitRef(){
            return (Function) unitRef;
        }
         @Override
        public <T> Higher<CRE,T> unit(T value){
            return this.<T>unitRef().apply(value);
        }
    }
    static  <CRE,A> GeneralPure<CRE,A> unit(Function<A, Higher<CRE, A>> unitRef){

        return new GeneralPure<CRE,A>(unitRef);
    }


    @AllArgsConstructor
    @Builder
    static class GeneralApplicative<CRE,A,B> implements Applicative<CRE> {
        Functor<CRE> functor;
        Pure<CRE> pure;
        BiFunction<? extends Higher<CRE, ? extends Function<A, B>>,? extends Higher<CRE,A>,? extends Higher<CRE,B>> applyRef;


        <T,R> BiFunction<Higher<CRE, ? extends Function<T, R>>,Higher<CRE,T>,Higher<CRE,R>> applyRef(){
            return (BiFunction)applyRef;
        }

        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,
                                    Higher<CRE,T> apply){

            return  this.<T,R>applyRef().apply(fn,apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return functor.map(fn, ds);
        }
        @Override
        public <T> Higher<CRE, T> unit(T value) {
           return pure.unit(value);
        }
    }

    static  <CRE,T,R> GeneralApplicative<CRE,T,R> applicative(Functor<CRE> functor, Pure<CRE> pure,
                                                              BiFunction<? extends Higher<CRE, Function<T, R>>, ? extends Higher<CRE, T>, ? extends Higher<CRE, R>> applyRef) {

        return new GeneralApplicative<CRE,T,R>(functor, pure,applyRef);

    }

    @AllArgsConstructor
    @Builder
    static class GeneralMonad<CRE,A,B> implements Monad<CRE> {


        Applicative<CRE> applicative;
        BiFunction<? extends Higher<CRE,A>,Function<? super A,? extends Higher<CRE,B>>,? extends Higher<CRE,B>> bindRef; //reference to bind / flatMap method


        <T,R> BiFunction<Higher<CRE,T>,Function<? super T,? extends Higher<CRE,R>>,Higher<CRE,R>> bindRef(){
            return (BiFunction)bindRef;
        }


        @Override
        public <T,R> Higher<CRE,R> flatMap(Function<? super T,? extends Higher<CRE,R>> fn,Higher<CRE,T> ds){
            return this.<T,R>bindRef().apply(ds,fn);
        }


        @Override
        public <T> Higher<CRE,T> unit(T value){
            return applicative.unit(value);

        }


        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
           return applicative.map(fn, ds);
        }


        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,  Higher<CRE,T> apply){
            return applicative.ap(fn, apply);
        }
    }
    static  <CRE,A,B> GeneralMonad<CRE,A,B> monad(Applicative<CRE> applicative,
                                                  BiFunction<? extends Higher<CRE, A>, Function<? super A, ? extends Higher<CRE, B>>, ? extends Higher<CRE, B>> bindRef) {

        return new GeneralMonad<CRE,A,B>(applicative,bindRef);

    }
    @AllArgsConstructor
    static class GeneralMonadZero<CRE,A,B>  implements MonadZero<CRE> {
        Higher<CRE, A> zero;
        Monad<CRE> monad;



        @Override
        public Higher<CRE, ?> zero() {
            return (Higher)zero;
        }

        @Override
        public <T, R> Higher<CRE, R> flatMap(Function<? super T, ? extends Higher<CRE, R>> fn, Higher<CRE, T> ds) {
            return monad.flatMap(fn, ds);
        }

        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,  Higher<CRE,T> apply){
            return monad.ap(fn, apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return monad.map(fn, ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return monad.unit(value);
        }

    }
    @AllArgsConstructor
    static class SupplierMonadZero<CRE,A,B>  implements MonadZero<CRE>{
        Supplier<Higher<CRE, A>> zero;
        Monad<CRE> monad;

        BiFunction<? extends Higher<CRE,A>,Predicate<? super A>,? extends Higher<CRE,A>> filterRef;


        <T> BiFunction<Higher<CRE,T>,Predicate<? super T>,Higher<CRE,T>> filterRef(){
            return (BiFunction)filterRef;
        }

        @Override
        public Higher<CRE, ?> zero() {
            return (Higher)zero.get();
        }

        @Override
        public <T, R> Higher<CRE, R> flatMap(Function<? super T, ? extends Higher<CRE, R>> fn, Higher<CRE, T> ds) {
            return monad.flatMap(fn, ds);
        }

        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,  Higher<CRE,T> apply){
            return monad.ap(fn, apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return monad.map(fn, ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return monad.unit(value);
        }

        @Override
        public <T> Higher<CRE, T> filter(Predicate<? super T> predicate, Higher<CRE, T> ds) {
            return this.<T>filterRef().apply(ds,predicate);
        }

    }
    static  <CRE,A,B> GeneralMonadZero<CRE,A,B> monadZero(Monad<CRE> monad,
                                                          Higher<CRE, A> zero) {

        return new GeneralMonadZero<CRE,A,B>(zero,monad);

    }
    static  <CRE,A,B> SupplierMonadZero<CRE,A,B> monadZero(Monad<CRE> monad,
                                                           Supplier<Higher<CRE, A>> zero,
                                                           BiFunction<Higher<CRE, A>, Predicate<? super A>, Higher<CRE, A>> filterRef) {

        return new SupplierMonadZero<CRE,A,B>(zero,monad,filterRef);

    }
    @AllArgsConstructor
    static class GeneralMonadPlus<CRE,T> implements MonadPlus<CRE> {
        MonoidK<CRE> monoid;
        Monad<CRE> monad;



      public <T> Higher<CRE,T> plus(Higher<CRE, T> a, Higher<CRE, T> b) {
        return this.monoid.apply(a, b);
      }

        @Override
        public <T, R> Higher<CRE, R> flatMap(Function<? super T, ? extends Higher<CRE, R>> fn, Higher<CRE, T> ds) {
            return monad.flatMap(fn, ds);
        }

        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,  Higher<CRE,T> apply){
            return monad.ap(fn,apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return monad.map(fn,ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return monad.unit(value);
        }




        public MonoidK<CRE> monoid() {
            return monoid;
        }

    }
    @AllArgsConstructor
    static class SupplierMonadPlus<CRE,T,B> implements MonadPlus<CRE>{
        MonoidK<CRE> monoid;
        MonadZero<CRE> monad;

      public <T> Higher<CRE,T> plus(Higher<CRE, T> a, Higher<CRE, T> b){

        return this.monoid.apply(a, b);
      }

        @Override
        public <T, R> Higher<CRE, R> flatMap(Function<? super T, ? extends Higher<CRE, R>> fn, Higher<CRE, T> ds) {
            return monad.flatMap(fn, ds);
        }

        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,  Higher<CRE,T> apply){
            return monad.ap(fn,apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return monad.map(fn,ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return monad.unit(value);
        }

        @Override
        public Higher<CRE, ?> zero(){
            return monad.zero();
        }


        public MonoidK<CRE> monoid() {
            return monoid;
        }

    }
    static  <CRE,A,B> GeneralMonadPlus<CRE,A> monadPlus(Monad<CRE> monad,
                                                        MonoidK<CRE> monoid) {

        return new GeneralMonadPlus<CRE,A>(monoid,monad);

    }
    static  <CRE,A,B> SupplierMonadPlus<CRE,A,B> monadPlus(MonadZero<CRE> monad,
                                                           MonoidK<CRE> monoid) {

        return new SupplierMonadPlus<CRE,A,B>(monoid,monad);

    }
    @AllArgsConstructor
    static class GeneralComonad<CRE,A,B> implements ComonadByPure<CRE> {
        Functor<CRE> functor;
        Pure<CRE> pure;
        Function<? super Higher<CRE, A>, ? extends A> extractFn;
        <T> Function<? super Higher<CRE, T>, ? extends T> extractFn(){
            return (Function)extractFn;
        }
        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return pure.unit(value);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return functor.map(fn, ds);
        }



        @Override
        public <T> T extract(Higher<CRE, T> ds) {
           return this.<T>extractFn().apply(ds);
        }

    }
    static  <CRE,T,R> GeneralComonad<CRE,T,R> comonad(Functor<CRE> functor, Pure<CRE> pure,
                                                      Function<? super Higher<CRE, T>, ? extends T> extractFn) {

        return new GeneralComonad<>(functor, pure,extractFn);

    }
    @AllArgsConstructor
    static class GeneralFoldable<CRE,T,R> implements Foldable<CRE> {
        BiFunction<Monoid<T>,Higher<CRE,T>,T> foldRightFn;
        BiFunction<Monoid<T>,Higher<CRE,T>,T> foldLeftFn;
        Function3<Monoid<R>,Function<T,R>,Higher<CRE,T>,R> foldMapFn;

        <T> BiFunction<Monoid<T>,Higher<CRE,T>,T> foldRightFn(){
            return (BiFunction)foldRightFn;
        }
        <T> BiFunction<Monoid<T>,Higher<CRE,T>,T> foldLeftFn(){
            return (BiFunction)foldLeftFn;
        }
        <T,R> Function3<Monoid<R>,Function<? super T,? extends R>,Higher<CRE,T>,R> foldMapFn(){
            return (Function3)foldMapFn;
        }

        public <T> T foldRight(Monoid<T> monoid, Higher<CRE,T> ds){
            return this.<T>foldRightFn().apply(monoid,ds);
        }

        public <T> T foldLeft(Monoid<T> monoid, Higher<CRE,T> ds){
            return this.<T>foldLeftFn().apply(monoid,ds);
        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<CRE, T> nestedA) {
            return this.<T,R>foldMapFn().apply(mb,fn,nestedA);
        }
    }

    static <CRE,T,R> GeneralFoldable<CRE,T,R> foldable(BiFunction<Monoid<T>, Higher<CRE, T>, T> foldRightFn, BiFunction<Monoid<T>, Higher<CRE, T>, T> foldLeftFn,
                                                       Function3<Monoid<R>,Function<T,R>,Higher<CRE,T>,R> foldMapFn){
        return new GeneralFoldable<>(foldRightFn,foldLeftFn,foldMapFn);
    }
    @AllArgsConstructor
    static class GeneralTraverse<CRE,C2,A,B> implements TraverseBySequence<CRE> {

        Applicative<CRE> applicative;
        BiFunction<Applicative<C2>,Higher<CRE, Higher<C2, A>>,Higher<C2, Higher<CRE, A>> > sequenceFn;

        <C2,T> BiFunction<Applicative<C2>,Higher<CRE, Higher<C2, T>>,Higher<C2, Higher<CRE, T>> > sequenceFn(){
            return (BiFunction)sequenceFn;
        }

        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,  Higher<CRE,T> apply){
            return applicative.ap(fn, apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return applicative.map(fn, ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return applicative.unit(value);
        }

        @Override
        public <C2, T> Higher<C2, Higher<CRE, T>> sequenceA(Applicative<C2> applicative,
                Higher<CRE, Higher<C2, T>> ds) {
           return this.<C2,T>sequenceFn().apply(applicative, ds);
        }

    }
    static <CRE,C2,T,R> Traverse<CRE> traverse(Applicative<CRE> applicative,
                                               BiFunction<Applicative<C2>, Higher<CRE, Higher<C2, T>>, Higher<C2, Higher<CRE, T>>> sequenceFn)  {
        return new GeneralTraverse<>(applicative,sequenceFn);
    }
    @AllArgsConstructor
    static class GeneralTraverseByTraverse<CRE,C2,A,B> implements TraverseByTraverse<CRE>{

        Applicative<CRE> applicative;
        Function3<Applicative<C2>,Function<A, Higher<C2, B>>,Higher<CRE, A>,Higher<C2, Higher<CRE, B>>> traverseFn;

        <C2,T,R> Function3<Applicative<C2>,Function< T, Higher<C2, R>>,Higher<CRE, T>,Higher<C2, Higher<CRE, R>>> traverseFn(){
            return (Function3)traverseFn;
        }

        @Override
        public <T,R> Higher<CRE,R> ap(Higher<CRE, ? extends Function< T,R>> fn,  Higher<CRE,T> apply){
            return applicative.ap(fn, apply);
        }

        @Override
        public <T, R> Higher<CRE, R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds) {
            return applicative.map(fn, ds);
        }

        @Override
        public <T> Higher<CRE, T> unit(T value) {
            return applicative.unit(value);
        }



        @Override
        public <C2, T, R> Higher<C2, Higher<CRE, R>> traverseA(Applicative<C2> applicative,
                Function<? super T, ? extends Higher<C2, R>> fn, Higher<CRE, T> ds) {
           return this.<C2,T,R>traverseFn().apply(applicative,(Function) fn , ds);
        }


    }
    static <CRE,C2,T,R> Traverse<CRE> traverseByTraverse(Applicative<CRE> applicative,
                                                         Function3<Applicative<C2>, Function<T, Higher<C2, R>>, Higher<CRE, T>, Higher<C2, Higher<CRE, R>>> traverseFn)  {
        return new GeneralTraverseByTraverse<>(applicative,traverseFn);
    }


}

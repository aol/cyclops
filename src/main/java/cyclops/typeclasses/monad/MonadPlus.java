package cyclops.typeclasses.monad;


import com.aol.cyclops2.hkt.Higher;
import cyclops.collectionx.mutable.ListX;
import cyclops.function.Monoid;
import cyclops.typeclasses.functions.MonoidK;

public interface MonadPlus<CRE> extends MonadZero<CRE>{

     <T> Monoid<Higher<CRE,T>> monoid();
        
    default <T> Monoid<Higher<CRE,T>> narrowMonoid(){
       return (Monoid)monoid();
   }

    default <T> MonoidK<CRE,T> asMonoid(){
        return MonoidK.of(narrowZero(),(a,b)->plus(a,b));
    }
    @Override
    default <T> Higher<CRE, T> zero(){
        return this.<T>monoid().zero();
    }
    
    default <T> Higher<CRE,T> plus(Higher<CRE, T> a, Higher<CRE, T> b){
        return this.<T>narrowMonoid().apply(a,b);         
    }

    default <T> Higher<CRE, T> sum(ListX<Higher<CRE, T>> list) {
        return list.foldLeft(this.narrowZero(),this::plus);
    }
}

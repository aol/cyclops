package cyclops.typeclasses.monad;


import com.aol.cyclops2.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.function.Monoid;

public interface MonadPlus<CRE> extends MonadZero<CRE>{

    Monoid<Higher<CRE,?>> monoid();
        
   default <T> Monoid<Higher<CRE,T>> narrowMonoid(){
       return (Monoid)monoid();
   }
    
    @Override
    default Higher<CRE, ?> zero(){
        return monoid().zero();
    }
    
    default <T> Higher<CRE,T> plus(Higher<CRE, T> a, Higher<CRE, T> b){
        return this.<T>narrowMonoid().apply(a,b);         
    }

    default <T> Higher<CRE, T> sum(ListX<Higher<CRE, T>> list) {
        return list.foldLeft(this.narrowZero(),this::plus);
    }
}



import static cyclops.function.Lambda.l1;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Xor;
import cyclops.control.lazy.Either;
import cyclops.function.Fn1;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.eval;
import org.junit.Test;



public class EvalsTest {

    @Test
    public void unit(){
      
        Eval<String> opt = Eval.Instances.unit()
                                            .unit("hello")
                                            .convert(Eval::narrowK);
        
        assertThat(opt,equalTo(Eval.now("hello")));
    }
    @Test
    public void functor(){
        
        Eval<Integer> opt = Eval.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Eval.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(Eval::narrowK);
        
        assertThat(opt,equalTo(Eval.now("hello".length())));
    }
    @Test
    public void apSimple(){

        Eval.Instances.applicative()
            .ap(Eval.now(l1(this::multiplyByTwo)),Eval.now(1));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void tailRec(){
        System.out.println(Eval.Instances.monadRec().tailRec(10,i->Eval.now(Xor.primary(i+10))));
        System.out.println(Eval.Instances.monadRec().tailRec(10,i-> i<1000_000 ? Eval.now(Either.left(i+1)) : Eval.now(Either.right(i+10))));
    }
    @Test
    public void applicative(){


        Eval<Fn1<Integer,Integer>> optFn =Eval.Instances.unit()
                                                             .unit(l1((Integer i) ->i*2))
                                                              .convert(Eval::narrowK);
        
        Eval<Integer> opt = Eval.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Eval.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h->Eval.Instances.applicative().ap(optFn, h))
                                     .convert(Eval::narrowK);
        
        assertThat(opt,equalTo(Eval.now("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       Eval<Integer> opt  = Eval.Instances.monad()
                                            .<Integer,Integer>flatMap(i->Eval.now(i*2), Eval.now(3))
                                            .convert(Eval::narrowK);
    }
    @Test
    public void monad(){
        
        Eval<Integer> opt = Eval.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Eval.Instances.monad().flatMap((String v) ->Eval.Instances.unit().unit(v.length()), h))
                                     .convert(Eval::narrowK);
        
        assertThat(opt,equalTo(Eval.now("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        Eval<String> opt = Eval.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Eval.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(Eval::narrowK);
        
        assertThat(opt,equalTo(Eval.now("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        Eval<String> opt = Eval.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Eval.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(Eval::narrowK);
        
        assertThat(opt,equalTo(Eval.now(null)));
    }
    
    @Test
    public void monadPlus(){
        Eval<Integer> opt = Eval.Instances.<Integer>monadPlus()
                                      .plus(Eval.now(null), Eval.now(10))
                                      .convert(Eval::narrowK);
        assertThat(opt,equalTo(Eval.now(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<Eval<Integer>> m = Monoid.of(Eval.now(null), (a, b)->a.isPresent() ? b : a);
        Eval<Integer> opt = Eval.Instances.<Integer>monadPlus(m)
                                      .plus(Eval.now(5), Eval.now(10))
                                      .convert(Eval::narrowK);
        assertThat(opt,equalTo(Eval.now(10)));
    }
    @Test
    public void  foldLeft(){
        int sum  = Eval.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, Eval.now(4));
        
        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = Eval.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, Eval.now(1));
        
        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Maybe<Higher<eval, Integer>> res = Eval.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), Eval.now(1))
                                                         .convert(Maybe::narrowK);
       
       
       assertThat(res.map(h->h.convert(Eval::narrowK).get()),
                  equalTo(Maybe.just(Eval.now(2).get())));
    }
    
}

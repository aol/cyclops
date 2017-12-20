package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import com.oath.cyclops.hkt.DataWitness.option;
import cyclops.arrow.MonoidKs;
import cyclops.instances.control.MaybeInstances;
import org.junit.Test;



public class MaybesTest {

    @Test
    public void unit(){

        Maybe<String> opt = MaybeInstances.unit()
                                            .unit("hello")
                                            .convert(Maybe::narrowK);

        assertThat(opt,equalTo(Maybe.of("hello")));
    }
    @Test
    public void functor(){

        Maybe<Integer> opt = MaybeInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> MaybeInstances.functor().map((String v) ->v.length(), h))
                                     .convert(Maybe::narrowK);

        assertThat(opt,equalTo(Maybe.of("hello".length())));
    }
    @Test
    public void apSimple(){
        MaybeInstances.applicative()
            .ap(Maybe.of(l1(this::multiplyByTwo)),Maybe.of(1));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        Maybe<Function1<Integer,Integer>> optFn = MaybeInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(Maybe::narrowK);

        Maybe<Integer> opt = MaybeInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> MaybeInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> MaybeInstances.applicative().ap(optFn, h))
                                     .convert(Maybe::narrowK);

        assertThat(opt,equalTo(Maybe.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       Maybe<Integer> opt  = MaybeInstances.monad()
                                            .<Integer,Integer>flatMap(i->Maybe.of(i*2), Maybe.of(3))
                                            .convert(Maybe::narrowK);
    }
    @Test
    public void monad(){

        Maybe<Integer> opt = MaybeInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> MaybeInstances.monad().flatMap((String v) -> MaybeInstances.unit().unit(v.length()), h))
                                     .convert(Maybe::narrowK);

        assertThat(opt,equalTo(Maybe.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        Maybe<String> opt = MaybeInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> MaybeInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(Maybe::narrowK);

        assertThat(opt,equalTo(Maybe.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        Maybe<String> opt = MaybeInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> MaybeInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(Maybe::narrowK);

        assertThat(opt,equalTo(Maybe.nothing()));
    }

    @Test
    public void monadPlus(){
        Maybe<Integer> opt = MaybeInstances.<Integer>monadPlus()
                                      .plus(Maybe.nothing(), Maybe.of(10))
                                      .convert(Maybe::narrowK);
        assertThat(opt,equalTo(Maybe.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){


        Maybe<Integer> opt = MaybeInstances.<Integer>monadPlus(MonoidKs.firstPresentOption())
                                      .plus(Maybe.of(5), Maybe.of(10))
                                      .convert(Maybe::narrowK);
        assertThat(opt,equalTo(Maybe.of(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = MaybeInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, Maybe.of(4));

        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = MaybeInstances.foldable()
                        .foldRight(0, (a,b)->a+b,Maybe.of(1));

        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Maybe<Higher<option, Integer>> res = MaybeInstances.traverse()
                                                          .traverseA(MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), Maybe.just(1))
                                                          .convert(Maybe::narrowK);


       assertThat(res.map(h->h.convert(Maybe::narrowK).toOptional().get()),
                  equalTo(Maybe.just(Maybe.just(2).toOptional().get())));
    }

}

package cyclops.typeclasses.cyclops;

import com.oath.cyclops.hkt.DataWitness.option;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.MonoidKs;
import cyclops.control.Option;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.instances.control.OptionInstances;
import org.junit.Test;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class OptionsTest {

    @Test
    public void unit(){

        Option<String> opt = OptionInstances.unit()
                                            .unit("hello")
                                            .convert(Option::narrowK);

        assertThat(opt,equalTo(Option.of("hello")));
    }
    @Test
    public void functor(){

        Option<Integer> opt = OptionInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> OptionInstances.functor().map((String v) ->v.length(), h))
                                     .convert(Option::narrowK);

        assertThat(opt,equalTo(Option.of("hello".length())));
    }
    @Test
    public void apSimple(){
        OptionInstances.applicative()
            .ap(Option.of(l1(this::multiplyByTwo)),Option.of(1));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        Option<Function1<Integer,Integer>> optFn = OptionInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(Option::narrowK);

        Option<Integer> opt = OptionInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> OptionInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> OptionInstances.applicative().ap(optFn, h))
                                     .convert(Option::narrowK);

        assertThat(opt,equalTo(Option.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       Option<Integer> opt  = OptionInstances.monad()
                                            .<Integer,Integer>flatMap(i->Option.of(i*2), Option.of(3))
                                            .convert(Option::narrowK);
    }
    @Test
    public void monad(){

        Option<Integer> opt = OptionInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> OptionInstances.monad().flatMap((String v) -> OptionInstances.unit().unit(v.length()), h))
                                     .convert(Option::narrowK);

        assertThat(opt,equalTo(Option.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        Option<String> opt = OptionInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> OptionInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(Option::narrowK);

        assertThat(opt,equalTo(Option.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        Option<String> opt = OptionInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> OptionInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(Option::narrowK);

        assertThat(opt,equalTo(Option.none()));
    }

    @Test
    public void monadPlus(){
        Option<Integer> opt = OptionInstances.<Integer>monadPlus()
                                      .plus(Option.none(), Option.of(10))
                                      .convert(Option::narrowK);
        assertThat(opt,equalTo(Option.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){


        Option<Integer> opt = OptionInstances.<Integer>monadPlus(MonoidKs.firstPresentOption())
                                      .plus(Option.of(5), Option.of(10))
                                      .convert(Option::narrowK);
        assertThat(opt,equalTo(Option.of(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = OptionInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, Option.of(4));

        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = OptionInstances.foldable()
                        .foldRight(0, (a,b)->a+b,Option.of(1));

        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Option<Higher<option, Integer>> res = OptionInstances.traverse()
                                                          .traverseA(OptionInstances.applicative(), (Integer a)->Option.some(a*2), Option.some(1))
                                                          .convert(Option::narrowK);


       assertThat(res.map(h->h.convert(Option::narrowK).toOptional().get()),
                  equalTo(Option.some(Option.some(2).toOptional().get())));
    }

}

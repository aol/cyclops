package cyclops.typeclasses.jdk;

import com.oath.cyclops.hkt.DataWitness.stream;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.instances.control.MaybeInstances;
import cyclops.instances.jdk.StreamInstances;
import cyclops.kinds.StreamKind;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.function.Lambda.l1;
import static cyclops.kinds.StreamKind.widen;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;



public class StreamsTest {


    @Test
    public void unit(){

        StreamKind<String> list = StreamInstances.unit()
                                     .unit("hello")
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){

        StreamKind<Integer> list = StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> StreamInstances.functor().map((String v) ->v.length(), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        StreamInstances.zippingApplicative()
            .ap(widen(Stream.of(l1(this::multiplyByTwo))),StreamKind.widen(Stream.of(1,2,3)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        StreamKind<Function1<Integer,Integer>> listFn = StreamInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(StreamKind::narrowK);

        StreamKind<Integer> list = StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> StreamInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> StreamInstances.zippingApplicative().ap(listFn, h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       StreamKind<Integer> list  = StreamInstances.monad()
                                      .flatMap(i->StreamKind.widen(ReactiveSeq.range(0,i)), StreamKind.widen(Stream.of(1,2,3)))
                                      .convert(StreamKind::narrowK);
    }
    @Test
    public void monad(){

        StreamKind<Integer> list = StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> StreamInstances.monad().flatMap((String v) -> StreamInstances.unit().unit(v.length()), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        StreamKind<String> list = StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> StreamInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        StreamKind<String> list = StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> StreamInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList()));
    }

    @Test
    public void monadPlus(){
        StreamKind<Integer> list = StreamInstances.<Integer>monadPlus()
                                      .plus(widen(Stream.of()), widen(Stream.of(10)))
                                      .convert(StreamKind::narrowK);
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList(10)));
    }

    @Test
    public void  foldLeft(){
        int sum  = StreamInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, widen(Stream.of(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = StreamInstances.foldable()
                        .foldRight(0, (a,b)->a+b, widen(Stream.of(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){
       Maybe<Higher<stream, Integer>> res = StreamInstances.traverse()
                                                         .traverseA(MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), StreamKind.of(1,2,3))
                                                         .convert(Maybe::narrowK);


       assertThat(res.map(i->i.convert(StreamKind::narrowK).collect(Collectors.toList())),
                  equalTo(Maybe.just(ListX.of(2,4,6))));
    }

}

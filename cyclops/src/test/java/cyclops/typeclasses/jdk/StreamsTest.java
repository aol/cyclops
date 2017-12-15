package cyclops.typeclasses.jdk;

import static cyclops.companion.Streams.StreamKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Streams;
import cyclops.companion.Streams.StreamKind;
import cyclops.collections.mutable.ListX;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import com.oath.cyclops.hkt.DataWitness.stream;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;



public class StreamsTest {

    @Test
    public void unit(){

        StreamKind<String> list = Streams.StreamInstances.unit()
                                     .unit("hello")
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){

        StreamKind<Integer> list = Streams.StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Streams.StreamInstances.functor().map((String v) ->v.length(), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        Streams.StreamInstances.zippingApplicative()
            .ap(widen(Stream.of(l1(this::multiplyByTwo))),widen(Stream.of(1,2,3)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        StreamKind<Function1<Integer,Integer>> listFn = Streams.StreamInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(StreamKind::narrowK);

        StreamKind<Integer> list = Streams.StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Streams.StreamInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> Streams.StreamInstances.zippingApplicative().ap(listFn, h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       StreamKind<Integer> list  = Streams.StreamInstances.monad()
                                      .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Stream.of(1,2,3)))
                                      .convert(StreamKind::narrowK);
    }
    @Test
    public void monad(){

        StreamKind<Integer> list = Streams.StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Streams.StreamInstances.monad().flatMap((String v) -> Streams.StreamInstances.unit().unit(v.length()), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        StreamKind<String> list = Streams.StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Streams.StreamInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        StreamKind<String> list = Streams.StreamInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Streams.StreamInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(StreamKind::narrowK);

        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList()));
    }

    @Test
    public void monadPlus(){
        StreamKind<Integer> list = Streams.StreamInstances.<Integer>monadPlus()
                                      .plus(widen(Stream.of()), widen(Stream.of(10)))
                                      .convert(StreamKind::narrowK);
        assertThat(list.collect(Collectors.toList()),equalTo(Arrays.asList(10)));
    }

    @Test
    public void  foldLeft(){
        int sum  = Streams.StreamInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, widen(Stream.of(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = Streams.StreamInstances.foldable()
                        .foldRight(0, (a,b)->a+b, widen(Stream.of(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){
       Maybe<Higher<stream, Integer>> res = Streams.StreamInstances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), StreamKind.of(1,2,3))
                                                         .convert(Maybe::narrowK);


       assertThat(res.map(i->i.convert(StreamKind::narrowK).collect(Collectors.toList())),
                  equalTo(Maybe.just(ListX.of(2,4,6))));
    }

}

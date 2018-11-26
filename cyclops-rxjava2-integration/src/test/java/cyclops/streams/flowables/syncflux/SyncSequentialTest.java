package cyclops.streams.flowables.syncflux;


import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.async.adapters.Topic;
import com.oath.cyclops.streams.BaseSequentialTest;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.reactive.FlowableReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import cyclops.companion.Streamable;
import cyclops.reactive.collections.mutable.ListX;
import org.hamcrest.Matchers;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class SyncSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return FlowableReactiveSeq.reactiveSeq(Flux.just(array));
    }
    @Test
    public void flatMapStream() {
        for (int i = 0; i < ITERATIONS; i++) {
            assertThat(of(1, 2, 3).flatMap(Stream::of)
                            .collect(Collectors.toList()),
                    Matchers.equalTo(Arrays.asList(1, 2, 3)));
        }
    }




    @Test
    public void testLimitUntilWithNulls() {


        assertThat(of(1, 2,  3, 4, 5).takeUntil(i -> false).toList(),equalTo(asList(1, 2,  3, 4, 5)));
    }
    @Test
    public void flatMapStreamFilter() {
        assertThat(of(1, 2, 3).flatMap(i -> ReactiveSeq.of(i).filter(Objects::nonNull))
                        .collect(Collectors.toList()),
                Matchers.equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void flatMapIStream() {
        assertThat(of(1, 2, 3).concatMap(i -> ReactiveSeq.of(i).filter(Objects::nonNull))
                        .collect(Collectors.toList()),
                Matchers.equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void flatMapIMaybe() {
        assertThat(of(1, 2, 3).concatMap(Maybe::ofNullable)
                        .collect(Collectors.toList()),
                Matchers.equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void spoutsCollect(){
        Integer[] array = new Integer[100];
        for(int i=0;i<array.length;i++) {
            array[i]=i;
        }
        for(int i=0;i<100;i++) {
            List<Integer> list = of(array).collect(Collectors.toList());
            assertThat(list.size(),equalTo(array.length));
        }
    }
    @Test
    public void broadcastTest(){
        Topic<Integer> topic = of(1,2,3)
                .broadcast();


        ReactiveSeq<Integer> stream1 = topic.stream();
        ReactiveSeq<Integer> stream2 = topic.stream();
        assertThat(stream1.to(ReactiveConvertableSequence::converter).listX(), Matchers.equalTo(ListX.of(1,2,3)));
        assertThat(stream2.stream().to(ReactiveConvertableSequence::converter).listX(), Matchers.equalTo(ListX.of(1,2,3)));

    }
    @Test
    public void mergePTest(){
        for(int i=0;i<100;i++) {
            ListX<Integer> list = of(3, 6, 9).mergeP(of(2, 4, 8), of(1, 5, 7)).to(ReactiveConvertableSequence::converter).listX();

            assertThat("List is " + list,list, hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertThat("List is " + list,list.size(), Matchers.equalTo(9));
        }
    }
    @Test
    public void prependPlay(){
        System.out.println(of(1,2,3).prependAll(100,200,300).collect(Collectors.toList()));


    }
    @Test
    public void splitAtExp(){
        of(1, 2, 3).peek(e->System.out.println("Peeking! " +e)).splitAt(0).transform( (a,b)->{
            a.printOut();
            b.printOut();
            return null;
        });

    }



    @Test
    public void testCycleAsync() {
      //  of(1, 2).collectStream(CyclopsCollectors.listX())
        //        .concatMap(i->i.cycle(3)).printOut();

       // of(1, 2).cycle().limit(6).forEach(n->System.out.println("Next " + n));


        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle().limit(6).toList());
        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle().limit(6).toList());

    }

    @Test
    public void multicast(){
        final ListX<ReactiveSeq<Integer>> t = of(1,2,3,4,5,6,7,8).multicast(2).to(ReactiveConvertableSequence::converter).listX();

//        t._1().forEach(e->System.out.println("First " + e));
        //       t._2().printOut();


        assertThat(t.get(0).limit(1).toList(),equalTo(ListX.of(1)));
        System.out.println("Second!");
        assertThat(t.get(1).limit(1).toList(),equalTo(ListX.of(1)));

    }
    @Test
    public void duplicate(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1,2,3,4,5,6,7,8).duplicate();

//        t._1().forEach(e->System.out.println("First " + e));
 //       t._2().printOut();


        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
        System.out.println("Second!");
        assertThat(t._2().limit(1).toList(),equalTo(ListX.of(1)));

    }
    @Test
    public void multicastCycle(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1,2,3,4,5,6,7,8).duplicate();

//        t._1().forEach(e->System.out.println("First " + e));
        //       t._2().printOut();


        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
        System.out.println("Second!");
        assertThat(t._2().cycle().limit(1).toList(),equalTo(ListX.of(1)));

    }
    @Test
    public void duplicateReplay(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
//        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
    }
    @Test
    public void limitSkip(){
        ReactiveSeq<Integer> stream = of(1);
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = stream.duplicate();


        assertThat(Streamable.fromStream(dup._1().limit(1)).toList(),equalTo(ListX.of(1)));
        assertThat(Streamable.fromStream(dup._2().skip(1)).toList(),equalTo(ListX.of()));

    }@Test
    public void limitSkip2(){
        ReactiveSeq<Integer> stream = of(1);
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = stream.duplicate();
        assertThat(dup._1().limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(dup._2().skip(1).toList(),equalTo(ListX.of()));




    }


    @Test
    public void splitAtHeadImpl(){

        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();



        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = new Tuple2(
                t._1().limit(1), t._2().skip(1));
        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t._2().skip(1).toList(),equalTo(ListX.of()));


    }
    @Test
    public void splitAtHeadImpl2(){

        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();



        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = new Tuple2(
                t._1().limit(1), t._2().skip(1));
        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t._2().skip(1).toList(),equalTo(ListX.of()));


    }
    @Test
    public void splitLimit(){
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));

    }
    @Test
    public void splitLimit2(){
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(),equalTo(ListX.of(1)));

        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
    }

    @Test
    public void duplicateDuplicate(){
        for(int k=0;k<ITERATIONS;k++) {
            assertThat(of(1, 2, 3).duplicate()
                    ._1().duplicate()._1().duplicate()._1().to(ReactiveConvertableSequence::converter).listX(), equalTo(ListX.of(1, 2, 3)));
        }

    }
    @Test
    public void duplicateDuplicateDuplicate(){
        for(int k=0;k<ITERATIONS;k++) {
            assertThat(of(1, 2, 3).duplicate()
                    ._1().duplicate()._1().duplicate()._1().duplicate()._1().to(ReactiveConvertableSequence::converter).listX(), equalTo(ListX.of(1, 2, 3)));
        }

    }
    @Test
    public void skipDuplicateSkip() {
        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).to(ReactiveConvertableSequence::converter).listX(), equalTo(ListX.of(3)));
        assertThat(of(1, 2, 3).duplicate()._2().skip(1).duplicate()._2().skip(1).to(ReactiveConvertableSequence::converter).listX(), equalTo(ListX.of(3)));
    }
    @Test
    public void skipLimitDuplicateLimitSkip() {
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup = of(1, 2, 3).triplicate();
        Optional<Integer> head1 = dup._1().limit(1).to(ReactiveConvertableSequence::converter).optional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> dup2 = dup._2().skip(1).triplicate();
        Optional<Integer> head2 = dup2._1().limit(1).to(ReactiveConvertableSequence::converter).optional().flatMap(l -> {
            return l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty();
        });
       assertThat(dup2._2().skip(1).to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(3)));

        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).to(ReactiveConvertableSequence::converter).listX(), equalTo(ListX.of(3)));
    }


    @Test
    public void splitThenSplit(){
        assertThat(of(1,2,3).to(ReactiveConvertableSequence::converter).optional(),equalTo(Optional.of(ListX.of(1,2,3))));
       // System.out.println(of(1, 2, 3).splitAtHead()._2().listX());
        System.out.println("split " + of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().to(ReactiveConvertableSequence::converter).listX());
        assertEquals(Option.of(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
    }
    @Test
    public void testSplitAtHead() {

        assertEquals(Option.none(), of().splitAtHead()._1());
        assertEquals(asList(), of().splitAtHead()._2().toList());

        assertEquals(Option.of(1), of(1).splitAtHead()._1());
        assertEquals(asList(), of(1).splitAtHead()._2().toList());

        assertEquals(Option.of(1), of(1, 2).splitAtHead()._1());
        assertEquals(asList(2), of(1, 2).splitAtHead()._2().toList());

        assertEquals(Option.of(1), of(1, 2, 3).splitAtHead()._1());
        assertEquals(Option.of(2), of(1, 2, 3).splitAtHead()._2().splitAtHead()._1());
        assertEquals(Option.of(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
        assertEquals(asList(2, 3), of(1, 2, 3).splitAtHead()._2().toList());
         assertEquals(asList(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
        assertEquals(asList(), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList());
    }
    @Test
    public void concatMapStream() {
        assertThat(of(1, 2, 3).concatMap(i -> ReactiveSeq.of(i).filter(Objects::nonNull))
                .collect(Collectors.toList()),
            Matchers.equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void concatMapMaybe() {
        assertThat(of(1, 2, 3).concatMap(Maybe::ofNullable)
                .collect(Collectors.toList()),
            equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void testLimitUntilInclusiveWithNulls() {


        assertThat(of(1, 2, 3, 4, 5).takeUntilInclusive(i -> false).toList(), equalTo(asList(1, 2, 3, 4, 5)));
    }

}

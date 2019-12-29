package cyclops.data.chain;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.companion.Streams;
import cyclops.data.Chain;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IteratorTest {

    @Test
    public void it(){
        System.out.println(ReactiveSeq.fromStream(Stream.of(1,2,3)).iterator().hasNext());
    }
    @Test
    public void removeValue2(){
        Chain<Integer> vec =Chain.wrap(Seq.of(5,2,1));
        ReactiveSeq<Integer> stream = vec.stream().removeFirst(e -> Objects.equals(e, 2));

        System.out.println(stream.toList());
        System.out.println(stream.toList());
        //   System.out.println(vec);
        //   System.out.println("SeqToStre"+vec.stream().seq());
        //  System.out.println("Seq"+vec.stream(). removeFirst(e-> Objects.equals(e,2)).seq());
      //  System.out.println("FS"+Chain.wrap(vec.stream(). removeFirst(e-> Objects.equals(e,2))));
        // assertThat(vec.removeValue(2), CoreMatchers.equalTo(of(5,1)));
    }

    int count =0;
    @Test @Ignore
    public void testCycleWhileNoOrd() {
        count =0;

        ImmutableList<Integer> ch =  Chain.wrap(asList(1, 2, 3)).cycleWhile(next->count++<6);

        assertThat(ch.toList(),equalTo(asList(1,2,3,1,2,3)));
        count = 0;
        assertThat(ch.toList(),equalTo(asList(1,2,3,1,2,3)));

    }

    @Test
    public void zip2(){
        System.out.println(Chain.wrap(asList(1,2,3,4,5,6)));
        List<Tuple2<Integer,Integer>> list =
            Chain.wrap(asList(1,2,3,4,5,6)).zipWithStream(Stream.of(100,200,300,400)).toList();
        // .peek(it -> System.out.println(it))

        //    .collect(Collectors.toList());

        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
        System.out.println(right);
        MatcherAssert.assertThat(right,hasItem(100));
        MatcherAssert.assertThat(right,hasItem(200));
        MatcherAssert.assertThat(right,hasItem(300));
        MatcherAssert.assertThat(right,hasItem(400));

        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
        MatcherAssert.assertThat(Chain.wrap(asList(1,2,3,4,5,6)),hasItem(left.get(0)));


    }

    @Test
    public void zipList(){
        System.out.println(Chain.wrap(asList(1,2,3,4,5,6)));
        List<Tuple2<Integer,Integer>> list =
            Chain.wrap(asList(1,2,3,4,5,6)).zip(asList(100,200,300,400)).toList();
        // .peek(it -> System.out.println(it))

        //    .collect(Collectors.toList());

        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
        System.out.println(right);
        MatcherAssert.assertThat(right,hasItem(100));
        MatcherAssert.assertThat(right,hasItem(200));
        MatcherAssert.assertThat(right,hasItem(300));
        MatcherAssert.assertThat(right,hasItem(400));

        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
        MatcherAssert.assertThat(Chain.wrap(asList(1,2,3,4,5,6)),hasItem(left.get(0)));


    }

    @Test
    public void prependAppend(){
        System.out.println(Chain.wrap(asList(1))
            .prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6)
            .prependAll(7,8)
            .insertAt(4,9).deleteBetween(1,2)
            .insertStreamAt(5,Stream.of(11,12)));

        System.out.println("RS" + ReactiveSeq.fromIterable(asList(1))
            .prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6)
            .prependAll(7,8)
            .insertAt(4,9).deleteBetween(1,2)
            .insertStreamAt(5,Stream.of(11,12)).toList());
        MatcherAssert.assertThat(Chain.wrap(asList(1))
            .prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6)
            .prependAll(7,8)
            .insertAt(4,9).deleteBetween(1,2)
            .insertStreamAt(5,Stream.of(11,12)).stream().count(), CoreMatchers.equalTo(10L));
    }

    @Test
    public void prependAppend2(){
        System.out.println(Chain.wrap(asList(1))
            .prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6));

        System.out.println("RS" + ReactiveSeq.fromIterable(asList(1))
            .prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6).toList());

    }
    @Test
    public void prependAppend3(){
        assertThat(Chain.wrap(asList(1))
            .prependStream(Stream.of(2)).toList(),equalTo(ReactiveSeq.fromIterable(asList(1))
            .prependStream(Stream.of(2)).toList()));


    }
    @Test
    public void prependStream(){
        assertThat(Chain.wrap(asList(1))
            .prepend(2), equalTo( Chain.wrap(asList(2,1)) ));
        assertThat(Chain.wrap(asList(1))
            .prependAll(asList(2)), equalTo( Chain.wrap(asList(2,1)) ));
        assertThat(Chain.wrap(asList(1))
            .prependStream(Stream.of(2)), equalTo( Chain.wrap(asList(2,1)) ));


    }

    @Test
    public void allCombinations3() {

        System.out.println("Chain -> " + Chain.wrap(asList(1, 2, 3)).combinations().map(s->s.toList()));
        /**
        assertThat(Chain.wrap(asList(1, 2, 3)).combinations().map(s->s.toList()), equalTo(Chain.wrap(asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
            Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3)))));
         **/
    }

    @Test
    public void allCombinationsStream() {

        System.out.println("Chain -> " + Chain.wrap(asList(1, 2, 3)).combinations().map(s->s.toList()));

        ReactiveSeq<List<Integer>> l = ReactiveSeq.of(1, 2, 3).combinations().map(s -> s.toList());
        l.iterator();
        for(List<Integer> n : l){
            System.out.println(n);
        }

        System.out.println(l.toList());
        l.forEach(System.out::println);
        /**
         assertThat(Chain.wrap(asList(1, 2, 3)).combinations().map(s->s.toList()), equalTo(Chain.wrap(asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
         Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3)))));
         **/
    }
    @Test
    public void allCombinationsStreams() {



        ReactiveSeq<List<Integer>> l = ReactiveSeq.of(1, 2, 3).combinations().map(s -> s.toList());

        for(List<Integer> n : l){
            System.out.println(n);
        }
        //[[], [1], [2], [3], [1, 2], [1, 3], [2, 3], [1, 2, 3]]



        /**
         assertThat(Chain.wrap(asList(1, 2, 3)).combinations().map(s->s.toList()), equalTo(Chain.wrap(asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
         Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3)))));
         **/
    }

    @Test
    public void combinations2() {
        System.out.println("Chain -> " + Chain.wrap(asList(1, 2, 3)).combinations(2).map(s->s.toList()));
        System.out.println(Chain.wrap(asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
        assertThat(Chain.wrap(asList(1, 2, 3)).combinations(2).map(s->s.toList()),
                    equalTo(Chain.wrap(asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3)))));
    }

    @Test
    public void zipping() {

        IterableX<Integer> odds =Chain.wrap(asList(1, 3, 5, 7, 9));
        IterableX<Integer> even = Chain.wrap(asList(2, 4, 6));

        IterableX<Tuple2<Integer, Integer>> zipped = odds.zip((t1, t2) -> Tuple.tuple(t1, t2), even);

        System.out.println(zipped);



    }
    @Test
    public void zip3Tuple() {
        Chain<Integer> it1 = Chain.wrap(asList(1));
        Chain<Integer> it2 = Chain.wrap(asList(2));
        Chain<Integer> it3 = Chain.wrap(asList(3));


        ImmutableList<Tuple3<Integer, Integer, Integer>> zipped = it1.zip3(it2, it3);



        StepVerifier.create(zipped)
            .consumeNextWith(t -> MatcherAssert.assertThat(Seq.of(t._1(),t._2(),t._3()),contains(1,2,3)))
            .expectComplete()
            .verify();
    }

    @Test
    public void reactive(){
       for(Integer n :  Spouts.from(Chain.wrap(asList(1, 3, 5, 7, 9)))){
           System.out.println(n);
       }
    }
}


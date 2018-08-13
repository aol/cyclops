package com.oath.cyclops.types.foldable;


import cyclops.control.Option;
import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class StatCollectorsTest {

    ReactiveSeq<Integer> stats = ReactiveSeq.range(0,100);

    ReactiveSeq<Integer> stats1000 = ReactiveSeq.range(0,1000);


    @Test
    public void atPercentileEmptyData(){
        assertThat(ReactiveSeq.empty().atPercentile(4),equalTo(Option.none()));

    }
    @Test
    public void atPercentileOutOfRange(){
        assertThat(stats.atPercentile(-1),equalTo(Option.none()));
        assertThat(stats.atPercentile(200),equalTo(Option.none()));
    }
    @Test
    public void atPercentile(){
        assertThat(stats.atPercentile(0),equalTo(Option.some(0)));
        assertThat(stats.atPercentile(1),equalTo(Option.some(1)));
        assertThat(stats.atPercentile(5),equalTo(Option.some(5)));
        assertThat(stats.size(),equalTo(100));
        assertThat(stats.atPercentile(99),equalTo(Option.some(99)));

    }

    @Test
    public void atPercentile1000(){
        assertThat(stats1000.atPercentile(0),equalTo(Option.some(0)));
        assertThat(stats1000.atPercentile(1),equalTo(Option.some(10)));
        assertThat(stats1000.atPercentile(5),equalTo(Option.some(50)));
        assertThat(stats1000.atPercentile(5.5),equalTo(Option.some(55)));
        assertThat(stats1000.size(),equalTo(1000));
        assertThat(stats1000.atPercentile(99),equalTo(Option.some(990)));

    }

    @Test
    public void median(){
        assertThat(stats1000.median(),equalTo(Option.some(500)));
    }

    @Test
    public void variance(){

        assertThat(stats.take(10).variance(i->i),equalTo(9.166666666666666));
    }

    @Test
    public void populationVariance(){

        assertThat(stats.take(10).populationVariance(i->i),equalTo(8.25));
    }

    @Test
    public void stdDeviation(){

        assertThat(stats.take(10).stdDeviation(i->i),equalTo(2.8722813232690143));
    }

    @Test
    public void withPercentiles(){
        stats.withPercentiles().take(4).printOut();
        assertThat(stats.withPercentiles().take(4).map(t->t.map2(bd->bd.intValue())),equalTo(Vector.of(Tuple.tuple(0,0),
                    Tuple.tuple(1,1),Tuple.tuple(2,2),Tuple.tuple(3,3))));
    }

    @Test
    public void mode(){
        ReactiveSeq<Integer> s = ReactiveSeq.of(1,2,2,2,3,3,3,3,3,4,4,4);
        assertThat(s.mode().orElse(-1),equalTo(3));
    }
    @Test
    public void maxBy(){
        ReactiveSeq<Integer> s = ReactiveSeq.of(1,2,2,2,3,3,3,3,3,4,4,4);
        assertThat(s.maxBy(i->i).orElse(-1),equalTo(4));
    }

    @Test
    public void doubleCollect(){
        Tuple2<List<Integer>, Set<Integer>> t2 = stats.collect(Collectors.toList(), Collectors.toSet());
        assertThat(t2._1(),equalTo(stats.toList()));
        assertThat(t2._2(),equalTo(stats.toSet()));
    }
    @Test
    public void tripleCollect(){
        Tuple3<List<Integer>, Set<Integer>,List<Integer>> t3 = stats.collect(Collectors.toList(), Collectors.toSet(),Collectors.toList());
        assertThat(t3._1(),equalTo(stats.toList()));
        assertThat(t3._2(),equalTo(stats.toSet()));
        assertThat(t3._3(),equalTo(stats.toList()));
    }

}

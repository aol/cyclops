package cyclops.monads.transformers;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;

import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.ReactiveReducers;
import cyclops.control.Option;
import cyclops.data.HashMap;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import cyclops.companion.Reducers;
import cyclops.control.Maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;

public abstract class AbstractNestedFoldableTest<W extends WitnessType<W>> {
    @Rule
    public final SystemOutRule sout = new SystemOutRule().enableLog();
    @Rule
    public final SystemErrRule serr = new SystemErrRule().enableLog();

    public abstract <T> FoldableTransformerSeq<W,T> of(T...elements);
    public abstract <T> FoldableTransformerSeq<W,T> empty();


    @Test
    public void mapReduce() {
        assertThat(of("hello","2","world","4").foldMap(Reducers.toCountInt()).singleOrElse(null),equalTo(4));
    }

    @Test
    public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
        assertThat(of("replaceWith","two","three","four").foldMap(this::toInt,Reducers.toTotalInt()).singleOrElse(null),
                equalTo(10));
    }
    int toInt(String s){
        if("replaceWith".equals(s))
            return 1;
        if("two".equals(s))
            return 2;
        if("three".equals(s))
            return 3;
        if("four".equals(s))
            return 4;
        return -1;
    }

    @Test
    public void reduceMonoid() {
        assertThat(of("hello","2","world","4").foldLeft(Reducers.toString(",")).singleOrElse(null).length(),
                equalTo(",hello,2,world,4".length()));
    }

    @Test
    public void reduceBinaryOperator() {
        assertThat(of(100,200,300,400,500).foldLeft( (acc,next) -> acc+next).stream().singleOrElse(null),is(Option.of(1500)));
    }

    @Test
    public void reduceIdentity() {
        assertThat(of(100,200,300,400,500).foldLeft( 0,(acc,next) -> acc+next).stream().singleOrElse(null),is(1500));
    }

    @Test
    public void reduceCombiner() {
        assertThat(of(100,200,300,400,500).foldLeft( 0,
                (acc, next) -> acc+next,
                Integer::sum).singleOrElse(null),equalTo(1500));
    }

    @Test
    public void testReduceStreamOfQextendsMonoidOfT() {

        assertThat(of("hello","2","world","4").foldLeft(ListX.of(Reducers.toString(","))).singleOrElse(Seq.of("--")).singleOrElse(null).length(),
                equalTo(",hello,2,world,4".length()));
    }

    @Test
    public void testReduceIterableOfQextendsMonoidOfT() {
        assertThat(of("hello","2","world","4").foldLeft(ListX.of(Reducers.toString(",")))
                    .singleOrElse(Seq.of("booo")).singleOrElse(null).length(),
                equalTo(",hello,2,world,4".length()));
    }

    @Test
    public void foldRight() {

        assertThat(of("hello","2","world","4").foldRight(Reducers.toString(",")).singleOrElse(null).length(),
                equalTo(",hello,2,world,4".length()));

    }


    @Test
    public void foldRightIdentity() {
        assertThat(of(100,200,300,400,500).foldRight( 0,(acc,next) -> acc+next).stream().singleOrElse(null),is(1500));
    }


    @Test
    public void foldRightMapToType() {
        assertThat(of(1,2,3,4).foldMapRight(ReactiveReducers.toLinkedListX()).singleOrElse(null).size(),
                equalTo(4));
    }


    @Test
    public void testJoin() {
        assertEquals("123".length(),of(1, 2, 3).join().singleOrElse(null).length());


    }

    @Test
    public void testJoinString() {
        assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").singleOrElse(null).length());
    }

    @Test
    public void testJoinStringStringString() {
        assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").singleOrElse(null).length());
    }

    @Test
    public void testStream() {
        assertThat(of(1,2,3).stream().toList().size(),equalTo(3));
        assertThat(of(1,2,3).stream().toList(),hasItems(1,2,3));
    }

    @Test
    public void testPrintPrintStream() {
        PrintStream str = System.out;
        of(1,2,3).print(str );
        assertThat(sout.getLog(),containsString("1"));
        assertThat(sout.getLog(),containsString("2"));
        assertThat(sout.getLog(),containsString("3"));
    }

    @Test
    public void testPrintPrintWriter() {
        PrintStream writer = System.out;
        of(1,2,3).print(writer );
        assertThat(sout.getLog(),containsString("1"));
        assertThat(sout.getLog(),containsString("2"));
        assertThat(sout.getLog(),containsString("3"));
    }

    @Test
    public void testPrintOut() {
        of(1,2,3).printOut();
        assertThat(sout.getLog(),containsString("1"));
        assertThat(sout.getLog(),containsString("2"));
        assertThat(sout.getLog(),containsString("3"));
    }

    @Test
    public void testPrintErr() {
        of(1,2,3).printErr();
        assertThat(serr.getLog(),containsString("1"));
        assertThat(serr.getLog(),containsString("2"));
        assertThat(serr.getLog(),containsString("3"));
    }

    @Test
    public void testGroupBy() {
        HashMap<Integer, Vector<Integer>> mapA =of(1, 2, 3, 4).groupBy(i -> i % 2).singleOrElse(null);
        Map<Integer, List<Integer>> map1 = mapA.map(v->v.toList()).javaMap();
        assertThat(map1.get(0),hasItem(2));
        assertThat(map1.get(0),hasItem(4));
        assertThat(map1.get(1),hasItem(1));
        assertThat(map1.get(1),hasItem(3));

        assertEquals(2, map1.size());
    }


    @Test
    public void testFindFirst() {
        assertThat(of(1,2,3,4,5).headOption().stream().singleOrElse(null).orElse(-1),lessThan(6));
    }

    @Test
    public void testStartsWithIterable() {
        assertTrue(of(1,2,3,4)
                .startsWith(Arrays.asList(1,2,3)).singleOrElse(null));
    }


    @Test
    public void testEndsWithIterable() {
        assertTrue(of(1,2,3,4,5,6)
                .endsWith(Arrays.asList(5,6)).singleOrElse(null));
    }

    @Test
    public void testEndsWith() {
        assertTrue(of(1,2,3,4,5,6)
                .endsWith(ReactiveSeq.of(5,6)).singleOrElse(null));
    }

    @Test
    public void testToLazyCollection() {
        Collection<Integer> col = of(1,2,3,4,5)
                                    .toNested(s->s.lazyCollection())
                                  .singleOrElse(null);
        System.out.println("takeOne!");
        col.forEach(System.out::println);
        assertThat(col.size(),equalTo(5));
    }


    @Test
    public void testFirstValue() {
        assertThat(of(1,2,3).firstValue(-1).singleOrElse(null),anyOf(equalTo(1),equalTo(2),equalTo(3)));
    }

    @Test
    public void testSingle() {
        assertThat(of(1).singleOrElse(-1).singleOrElse(null),equalTo(1));
    }

    @Test
    public void testSinglePredicateOfQsuperT() {
        assertThat(of(1,11).single(i->i>10).singleOrElse(null),equalTo(Maybe.just(11)));
    }

    @Test
    public void testSingleOptional() {

        assertThat(of(1,11).single().stream().to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(Maybe.nothing())));
    }

    @Test
    public void testGet() {
        assertThat(of(1).get(0).stream().singleOrElse(null),equalTo(Maybe.of(1)));
    }

    @Test
    public void testSchedule() {
        assertThat(of(1,2,3,4)

                .schedule("* * * * * ?", ex)
                .connect()
                .debounce(1,TimeUnit.DAYS)
                .peek(System.out::println)
                .toList().size(),lessThan(3));
    }

    @Test
    public void testScheduleFixedDelay() {
        assertThat(of(1,2,3,4)
                .scheduleFixedDelay(1000, ex)
                .connect()
                .debounce(1,TimeUnit.DAYS)
                .peek(System.out::println)
                .toList(),equalTo(Arrays.asList(1)));
    }
    ScheduledExecutorService ex =Executors.newScheduledThreadPool(1);
    AtomicInteger count = new AtomicInteger(0);
    @Test
    public void testScheduleFixedRate() {
        assertThat(of(1,2,3,4)
                .scheduleFixedRate(1000, ex)
                .connect()
                .debounce(1,TimeUnit.DAYS)
                .peek(System.out::println)
                .toList(),equalTo(Arrays.asList(1)));
    }


}

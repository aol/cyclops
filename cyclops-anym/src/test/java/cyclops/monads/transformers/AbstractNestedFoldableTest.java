package cyclops.monads.transformers;

import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import static java.util.Arrays.asList;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.oath.anym.transformers.FoldableTransformerSeq;

import cyclops.monads.WitnessType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import cyclops.companion.Reducers;
import cyclops.control.Maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.collections.mutable.ListX;

public abstract class AbstractNestedFoldableTest<W extends WitnessType<W>> {
    @Rule
    public final SystemOutRule sout = new SystemOutRule().enableLog();
    @Rule
    public final SystemErrRule serr = new SystemErrRule().enableLog();

    public abstract <T> FoldableTransformerSeq<W,T> of(T...elements);
    public abstract <T> FoldableTransformerSeq<W,T> empty();



    @Test
    public void visitPresent() {

        assertThat(of(1,2,3,4).visit((a, b)->"world",()->"hello" ).singleOrElse(null),equalTo("world"));
    }
    @Test
    public void visitEmpty() {
        System.out.println(empty().visit((a, b)->"world",()->"hello" ));
        assertThat(empty().visit((a, b)->"world",()->"hello" ).singleOrElse(null),equalTo("hello"));
    }
    @Test
    public void visitPresentHead() {
        assertThat(of(1,2,3,4).visit((a, b)->a,()->-1 ).singleOrElse(null),equalTo(1));
    }
    @Test
    public void visitPresentTail() {
        assertThat(of(1,2,3,4).visit((a, b)->b.toList().size(),()->Arrays.asList().size()).singleOrElse(null),equalTo(3));
    }
    @Test
    public void visitMaybe() {
        assertThat(of(1,2,3,4).visit((a, b)->"world",()->"hello").singleOrElse(null),equalTo("world"));
    }
    @Test
    public void visitMaybeEmpty() {
        assertThat(this.<Integer>empty().visit((a, b)->a,()->10).singleOrElse(null),equalTo(10));
    }


    @Test
    public void mapReduce() {
        assertThat(of("hello","2","world","4").mapReduce(Reducers.toCountInt()).singleOrElse(null),equalTo(4));
    }

    @Test
    public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
        assertThat(of("replaceWith","two","three","four").mapReduce(this::toInt,Reducers.toTotalInt()).singleOrElse(null),
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
        assertThat(of("hello","2","world","4").reduce(Reducers.toString(",")).singleOrElse(null).length(),
                equalTo(",hello,2,world,4".length()));
    }

    @Test
    public void reduceBinaryOperator() {
        assertThat(of(100,200,300,400,500).reduce( (acc,next) -> acc+next).stream().singleOrElse(null),is(Optional.of(1500)));
    }

    @Test
    public void reduceIdentity() {
        assertThat(of(100,200,300,400,500).reduce( 0,(acc,next) -> acc+next).stream().singleOrElse(null),is(1500));
    }

    @Test
    public void reduceCombiner() {
        assertThat(of(100,200,300,400,500).reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum).singleOrElse(null),equalTo(1500));
    }

    @Test
    public void testReduceStreamOfQextendsMonoidOfT() {
        assertThat(of("hello","2","world","4").reduce(Stream.of(Reducers.toString(","))).singleOrElse().singleOrElse(null).length(),
                equalTo(",hello,2,world,4".length()));
    }

    @Test
    public void testReduceIterableOfQextendsMonoidOfT() {
        assertThat(of("hello","2","world","4").reduce(ListX.of(Reducers.toString(",")))
                    .singleOrElse().singleOrElse(null).length(),
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
        assertThat(of(1,2,3,4).foldRightMapToType(Reducers.toLinkedListX()).singleOrElse(null).size(),
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
        Map<Integer, ListX<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2).singleOrElse(null);

        assertThat(map1.get(0),hasItem(2));
        assertThat(map1.get(0),hasItem(4));
        assertThat(map1.get(1),hasItem(1));
        assertThat(map1.get(1),hasItem(3));

        assertEquals(2, map1.size());
    }

    @Test
    public void testHeadAndTail() {
        assertEquals(Optional.empty(), of().headAndTail().singleOrElse(null).headOptional());
        assertEquals(asList(), of().headAndTail().singleOrElse(null).tail().toList());

        assertEquals(Optional.of(1), of(1).headAndTail().singleOrElse(null).headOptional());
        assertEquals(asList(), of(1).headAndTail().singleOrElse(null).tail().toList());

        assertEquals(Maybe.of(1), of(1, 2).headAndTail().singleOrElse(null).headMaybe());
        assertEquals(asList(2), of(1, 2).headAndTail().singleOrElse(null).tail().toList());

        assertEquals(Arrays.asList(1), of(1, 2, 3).headAndTail().singleOrElse(null).headStream().toList());
        assertEquals((Integer)2, of(1, 2, 3).headAndTail().singleOrElse(null).tail().headAndTail().head());
        assertEquals(Optional.of(3), of(1, 2, 3).headAndTail().singleOrElse(null).tail().headAndTail().tail().headAndTail().headOptional());
        assertEquals(asList(2, 3), of(1, 2, 3).headAndTail().singleOrElse(null).tail().toList());
        assertEquals(asList(3), of(1, 2, 3).headAndTail().singleOrElse(null).tail().headAndTail().tail().toList());
        assertEquals(asList(), of(1, 2, 3).headAndTail().singleOrElse(null).tail().headAndTail().tail().headAndTail().tail().toList());
    }

    @Test
    public void testFindFirst() {
        assertThat(of(1,2,3,4,5).findFirst().stream().singleOrElse(null).get(),lessThan(6));
    }

    @Test
    public void testStartsWithIterable() {
        assertTrue(of(1,2,3,4)
                .startsWithIterable(Arrays.asList(1,2,3)).singleOrElse(null));
    }

    @Test
    public void testStartsWith() {
        assertTrue(of(1,2,3,4)
                .startsWith(ReactiveSeq.of(1,2,3)).singleOrElse(null));
    }

    @Test
    public void testEndsWithIterable() {
        assertTrue(of(1,2,3,4,5,6)
                .endsWithIterable(Arrays.asList(5,6)).singleOrElse(null));
    }

    @Test
    public void testEndsWith() {
        assertTrue(of(1,2,3,4,5,6)
                .endsWithIterable(ReactiveSeq.of(5,6)).singleOrElse(null));
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
    public void testToConcurrentLazyCollection() {
        Collection<Integer> col = of(1,2,3,4,5).toNested(s->s.lazyCollectionSynchronized())

                                    .singleOrElse(null);
        System.out.println("takeOne!");
        col.forEach(System.out::println);
        assertThat(col.size(),equalTo(5));
    }
/**
    @Test
    public void testToConcurrentLazyStreamable() {
        StreamableT<Integer> repeat = of(1,2,3,4,5,6)
                                        .lazyStreamableSynchronized();

        assertThat(repeat.reactiveSeq().toList(),hasItems(1,2,3,4,5,6));
        assertThat(repeat.reactiveSeq().toList(),hasItems(1,2,3,4,5,6));

    }
 **/

    @Test
    public void testFirstValue() {
        assertThat(of(1,2,3).firstValue().singleOrElse(null),anyOf(equalTo(1),equalTo(2),equalTo(3)));
    }

    @Test
    public void testSingle() {
        assertThat(of(1).singleOrElse().singleOrElse(null),equalTo(1));
    }

    @Test
    public void testSinglePredicateOfQsuperT() {
        assertThat(of(1,11).single(i->i>10).singleOrElse(null),equalTo(Maybe.just(11)));
    }

    @Test
    public void testSingleOptional() {

        assertThat(of(1,11).single().stream().toListX(),equalTo(ListX.of(Maybe.nothing())));
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

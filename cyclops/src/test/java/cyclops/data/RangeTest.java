package cyclops.data;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.EnumerationTest.Days;
import cyclops.data.Ord;
import org.junit.Test;

import static cyclops.data.Enumeration.enums;
import static cyclops.data.EnumerationTest.Days.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class RangeTest {

    Range<Integer> five =Range.range(1,5);
    Range<Days> days = Range.range(Tuesday, Saturday, enums(Days.class));


    @Test
    public void enumeration() throws Exception {
        assertNotNull(days.enumeration());
    }

    @Test
    public void startsBefore() throws Exception {
        assertTrue(days.startsBefore(Range.range(Wednesday, Saturday, enums(Days.class))));
        assertFalse(days.startsBefore(Range.range(Monday, Saturday, enums(Days.class))));
    }

    @Test
    public void endsAfter() throws Exception {
        assertTrue(days.startsBefore(Range.range(Wednesday, Friday, enums(Days.class))));
        assertFalse(days.startsBefore(Range.range(Monday, Saturday, enums(Days.class))));
    }

    @Test
    public void map() throws Exception {
        Range<Days> r  = five.map(d->Days.values()[d],days.enumeration(),Comparators.naturalOrderIdentityComparator());
        assertThat(r.start,equalTo(Days.Tuesday));
        assertThat(r.end,equalTo(Days.Saturday));
    }

    @Test
    public void ordering() throws Exception {
        Ord.Ordering x = five.ordering().compareOrder(2, 4);
        assertThat(x,equalTo(Ord.Ordering.LESS));
        Ord.Ordering x2 = five.ordering().compareOrder(2, 2);
        assertThat(x2,equalTo(Ord.Ordering.EQUAL));
        Ord.Ordering x3 = five.ordering().compareOrder(4, 2);
        assertThat(x3,equalTo(Ord.Ordering.MORE));
    }

    @Test
    public void reverse() throws Exception {
        Range<Days> reversedDays = days.reverse();
        assertFalse(reversedDays.startsBefore(Range.range(Wednesday, Saturday, enums(Days.class))));

        assertTrue(reversedDays.startsBefore(Range.range(Sunday, Saturday, enums(Days.class))));
    }

    @Test
    public void contains() throws Exception {
        assertTrue(days.contains(Range.range(Wednesday,Thursday,enums(Days.class))));
        assertFalse(days.contains(Range.range(Wednesday,Sunday,enums(Days.class))));
    }

    @Test
    public void contains1() throws Exception {
        assertTrue(days.contains(Wednesday));
        assertFalse(days.contains(Sunday));
    }

    @Test
    public void plusAll() throws Exception {
        Tuple2<Range<Days>,Option<Range<Days>>> toSunday = days.plusAll(Range.range(Saturday, Sunday,enums(Days.class)));

        Range<Days> rday = toSunday._1();
        System.out.println(rday);
        assertThat(rday.start,equalTo(Days.Tuesday));
        assertThat(rday.end,equalTo(Days.Sunday));
    }

    @Test
    public void minusAll() throws Exception {
        Option<Tuple2<Range<Days>, Option<Range<Days>>>> toFriday = days.minusAll(Range.range(Friday, Sunday, enums(Days.class)));

        assertThat(toFriday.map(t2->t2._1().end).orElse(Monday),equalTo(Friday));
    }

    @Test
    public void intersection() throws Exception {
        Option<Range<Days>> inter = days.intersection(Range.range(Friday, Sunday, enums(Days.class)));

       assertThat(inter.map(t2-> t2.start).orElse(Days.Monday),equalTo(Friday));
        assertThat(inter.map(t2-> t2.end).orElse(Days.Monday),equalTo(Saturday));
    }

    @Test
    public void stream() throws Exception {
        System.out.println(ReactiveSeq.range(1,5).toListX());
        assertThat(five.stream().toListX(),equalTo(ReactiveSeq.range(1,6).toListX()));
    }

    @Test
    public void lazySeq() throws Exception {
        System.out.println(ReactiveSeq.range(1,5).lazySeq().seq());
        System.out.println(five.lazySeq().seq());
        assertThat(five.lazySeq(),equalTo(ReactiveSeq.range(1,6).lazySeq()));
    }

}

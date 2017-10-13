package cyclops.data;

import cyclops.typeclasses.Enumeration;
import cyclops.typeclasses.EnumerationTest;
import cyclops.typeclasses.EnumerationTest.Days;
import cyclops.typeclasses.Ord;
import org.junit.Test;

import static cyclops.typeclasses.Enumeration.enums;
import static cyclops.typeclasses.EnumerationTest.Days.*;
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
    }

    @Test
    public void contains1() throws Exception {
    }

    @Test
    public void plusAll() throws Exception {
    }

    @Test
    public void minusAll() throws Exception {
    }

    @Test
    public void intersection() throws Exception {
    }

    @Test
    public void stream() throws Exception {
    }

    @Test
    public void lazySeq() throws Exception {
    }

}
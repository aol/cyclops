package cyclops.data;

import cyclops.function.Ordering;
import cyclops.typeclasses.Enumeration;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class DIETTest {
    DIET<Integer> diet = DIET.cons(DIET.cons(Range.range(1, 5)), Range.range(6, 7), DIET.cons(Range.range(10, 20)));
    @Test
    public void empty() throws Exception {
        assertTrue(DIET.empty().isEmpty());
    }

    @Test
    public void cons() throws Exception {
        assertFalse(DIET.cons(Range.range(10,20)).isEmpty());
    }

    @Test
    public void cons1() throws Exception {

        System.out.println(diet);
        assertFalse(diet.isEmpty());
    }

    @Test
    public void contains() throws Exception {
        assertTrue(diet.contains(4));
        assertTrue(diet.contains(11));
        assertFalse(diet.contains(8));
    }

    @Test
    public void contains1() throws Exception {
        assertTrue(diet.contains(Range.range(3,4)));
        assertTrue(diet.contains(Range.range(11,15)));
        assertFalse(diet.contains(Range.range(8,17)));
    }

    @Test
    public void add() throws Exception {
        DIET<Integer> diet2 = diet.add(Range.range(50, 60));
        assertTrue(diet2.contains(Range.range(3,4)));
        assertTrue(diet2.contains(Range.range(11,15)));
        assertFalse(diet2.contains(Range.range(8,17)));
        assertTrue(diet2.contains(Range.range(54,58)));
    }



    @Test
    public void add1() throws Exception {
        DIET<Integer> diet2 = diet.add(100, Enumeration.ints(), Ordering.of(Comparator.naturalOrder()));
        assertTrue(diet2.contains(Range.range(3,4)));
        assertTrue(diet2.contains(Range.range(11,15)));
        assertFalse(diet2.contains(Range.range(8,17)));
        assertTrue(diet2.contains(Range.range(100,101)));
    }


    @Test
    public void remove() throws Exception {
        System.out.println("DIET " + diet);
        System.out.println("A:"+diet.remove(Range.range(2,4)));
        System.out.println("B:"+diet.remove(Range.range(12,14)));
        System.out.println("C:"+diet.remove(Range.range(6,7)));
    }

    @Test
    public void remove1() throws Exception {
    }

    @Test
    public void map() throws Exception {
        System.out.println(diet.map(i->i*2));
    }

    @Test
    public void lazySeq() throws Exception {
    }

    @Test
    public void stream() throws Exception {
    }

    @Test
    public void iterator() throws Exception {
    }

    @Test
    public void isEmpty() throws Exception {
    }

}
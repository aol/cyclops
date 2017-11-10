package cyclops.data;

import cyclops.function.Ordering;
import cyclops.typeclasses.Enumeration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static cyclops.data.Range.range;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class DIETTest {
    DIET<Integer> diet = DIET.cons(DIET.cons(range(1, 5)), range(6, 7), DIET.cons(range(10, 20)));
    @Test
    public void empty() throws Exception {
        assertTrue(DIET.empty().isEmpty());
    }



    @Test
    public void cons() throws Exception {
        assertFalse(DIET.cons(range(10,20)).isEmpty());
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
        assertTrue(diet.contains(range(3,4)));
        assertTrue(diet.contains(range(11,15)));
        assertFalse(diet.contains(range(8,17)));
    }

    @Test
    public void add() throws Exception {
        DIET<Integer> diet2 = diet.add(range(50, 60));
        assertTrue(diet2.contains(range(3,4)));
        assertTrue(diet2.contains(range(11,15)));
        assertFalse(diet2.contains(range(8,17)));
        assertTrue(diet2.contains(range(54,58)));
    }



    @Test
    public void add1() throws Exception {
        DIET<Integer> diet2 = diet.add(100, Enumeration.ints(), Ordering.of(Comparator.naturalOrder()));
        assertTrue(diet2.contains(range(3,4)));
        assertTrue(diet2.contains(range(11,15)));
        assertFalse(diet2.contains(range(8,17)));
        assertTrue(diet2.contains(range(100,101)));
    }


    @Test
    public void remove() throws Exception {
    System.out.println("DIET " + diet);
        System.out.println("A:"+diet.remove(range(2,4)));
        System.out.println("B:"+diet.remove(range(12,14)));
        System.out.println("C:"+diet.remove(range(6,7)));

        DIET<Integer> d2 = diet.remove(range(2,4));
        assertFalse(d2.contains(2));
        assertFalse(d2.contains(3));
        assertTrue(d2.contains(4));
        assertFalse(d2.contains(5));
        assertTrue(d2.contains(1));
        assertTrue(d2.contains(10));

        DIET<Integer> d3 = diet.remove(range(12,14));
        assertFalse(d3.contains(12));
        assertFalse(d3.contains(13));
        assertTrue(d3.contains(14));
        assertTrue(d3.contains(1));
        assertTrue(d3.contains(10));

        DIET<Integer> d4 = diet.remove(range(6,7));
        assertFalse(d4.contains(5));
        assertFalse(d4.contains(6));
        assertFalse(d4.contains(7));
        assertTrue(d4.contains(14));
        assertTrue(d4.contains(1));
        assertTrue(d4.contains(10));
    }

    @Test
    public void remove1() throws Exception {
        DIET<Integer> d2 = diet.remove(2).remove(3);
        assertFalse(d2.contains(2));
        assertFalse(d2.contains(3));
        assertTrue(d2.contains(4));
        assertFalse(d2.contains(5));
        assertTrue(d2.contains(1));
        assertTrue(d2.contains(10));
    }

    @Test
    public void map() throws Exception {
        assertThat(diet.map(i->i*2).stream().toList(),equalTo(
                Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40)
        ));
        assertThat(diet.map(i->i*2,Enumeration.ints(),Comparator.naturalOrder()).stream().toList(),equalTo(
                Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40)
        ));
    }

    @Test
    public void flatMap(){
        DIET<Integer> d2 = diet.flatMap(i -> DIET.cons(range(500, 1000)));
        List<Integer> l = d2.stream().toListX();
        System.out.println(d2);

        System.out.println(range(500,1000).stream().size());

        assertThat(d2.stream().toListX().size(),equalTo(501));

    }


    @Test
    public void lazySeq() throws Exception {
        assertThat(diet.lazySeq().size(),equalTo(18));
    }

    @Test
    public void stream() throws Exception {
        assertThat(diet.stream().size(),equalTo(18));
    }


    @Test
    public void isEmpty() throws Exception {
        assertFalse(diet.isEmpty());
    }

}

package com.oath.cyclops.types;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.data.HashMap;
import cyclops.data.Vector;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;

public abstract class AbstractFoldableTest {
    public abstract <T> IterableX<T> of(T...elements);

    @Test
    public void get0(){
        assertTrue(of(1).elementAt(0).isPresent());
    }
    @Test
    public void getAtMultple(){
        assertThat(of(1,2,3,4,5).elementAt(2).toOptional().get(),equalTo(3));
    }
    @Test
    public void getAt1(){
        assertFalse(of(1).elementAt(1).isPresent());
    }
    @Test
    public void elementAtEmpty(){
        assertFalse(of().elementAt(0).isPresent());
    }
    @Test
    public void singleTest(){
        assertThat(of(1).singleOrElse(-1),equalTo(1));
    }
    @Test(expected=UnsupportedOperationException.class)
    public void singleEmpty(){
        of().singleOrElse(null);
    }
    @Test(expected=UnsupportedOperationException.class)
    public void single2(){
        of(1,2).singleOrElse(null);
    }
    @Test
    public void singleOptionalTest(){
        assertThat(of(1).single().toOptional().get(),equalTo(1));
    }
    @Test
    public void singleOptionalEmpty(){
        assertFalse(of().single().isPresent());
    }
    @Test
    public void singleOptonal2(){
        assertFalse(of(1,2).single().isPresent());
    }

    @Test
    public void elementAt0(){
        assertThat(of(1).elementAt(0).toOptional().get(),equalTo(1));
    }
    @Test
    public void getMultple(){
        assertThat(of(1,2,3,4,5).elementAt(2).toOptional().get(),equalTo(3));
    }
    @Test
    public void xMatch(){
        assertTrue(of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
    }
    @Test
    public void endsWith(){
        assertTrue(of(1,2,3,4,5,6)
                .endsWith(Arrays.asList(5,6)));
    }
    @Test
    public void endsWithFalse(){
        assertFalse(of(1,2,3,4,5,6)
                .endsWith(Arrays.asList(5,6,7)));
    }
    @Test
    public void endsWithToLong(){
        assertFalse(of(1,2,3,4,5,6)
                .endsWith(Arrays.asList(0,1,2,3,4,5,6)));
    }
    @Test
    public void endsWithEmpty(){
        assertTrue(of(1,2,3,4,5,6)
                .endsWith(Arrays.asList()));
    }
    @Test
    public void endsWithWhenEmpty(){
        assertFalse(of()
                .endsWith(Arrays.asList(1,2,3,4,5,6)));
    }
    @Test
    public void endsWithBothEmpty(){
        assertTrue(ReactiveSeq.<Integer>of()
                .endsWith(Arrays.asList()));
    }

    @Test
    public void testJoin() {
        assertEquals("123".length(),of(1, 2, 3).join().length());
        assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
        assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());


    }

    @Test
    public void testLazy(){
        Collection<Integer> col = of(1,2,3,4,5)
                                           .to()
                                            .lazyCollection();
        System.out.println("takeOne!");
        col.forEach(System.out::println);
        assertThat(col.size(),equalTo(5));
    }
    @Test
    public void testLazyCollection(){
        Collection<Integer> col = of(1,2,3,4,5)
                                            .to()
                                            .lazyCollection();
        System.out.println("takeOne!");
        col.forEach(System.out::println);
        assertThat(col.size(),equalTo(5));
    }
    public void testFoldLeft() {
        for(int i=0;i<100;i++){
            Supplier<Folds<String>> s = () -> of("a", "b", "c");

            assertTrue(s.get().foldLeft("", String::concat).contains("a"));
            assertTrue(s.get().foldLeft("", String::concat).contains("b"));
            assertTrue(s.get().foldLeft("", String::concat).contains("c"));

            assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));


            assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
        }
    }

    @Test
    public void testFoldRight(){
            Supplier<Folds<String>> s = () -> of("a", "b", "c");

            assertTrue(s.get().foldRight("", String::concat).contains("a"));
            assertTrue(s.get().foldRight("", String::concat).contains("b"));
            assertTrue(s.get().foldRight("", String::concat).contains("c"));
            assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
    }

    @Test
    public void testFoldLeftStringBuilder() {
        Supplier<Folds<String>> s = () -> of("a", "b", "c");


        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("a"));
        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("b"));
        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("c"));
        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("-"));


        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));


    }

    @Test
    public void testFoldRighttringBuilder() {
        Supplier<Folds<String>> s = () -> of("a", "b", "c");


        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("a"));
        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("b"));
        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("c"));
        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("-"));


    }
/**
    @Test
    public void findAny(){
        assertThat(of(1,2,3,4,5).findAny().get(),lessThan(6));
    }
    @Test
    public void findFirst(){
        assertThat(of(1,2,3,4,5).findFirst().get(),lessThan(6));
    }

    @Test
    public void visit(){

        String res= of(1,2,3).visit((x,xs)->
                                xs.join(x >2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2world3"));
    }
    @Test
    public void whenGreaterThan2(){
        String res= of(5,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertNotNull(res);

    }
    @Test
    public void when2(){

        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
    @Test
    public void whenNilOrNotJoinWithFirstElement(){


        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }
**/
    @Test
    public void testCollectable(){
        assertThat(of(1,2,3).stream().anyMatch(i->i==2),equalTo(true));
    }
    @Test
    public void testGroupByEager() {
        HashMap<Integer, Vector<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);

        assertThat(map1.getOrElse(0,Vector.empty()),hasItem(2));
        assertThat(map1.getOrElse(0,Vector.empty()),hasItem(4));
        assertThat(map1.getOrElse(1,Vector.empty()),hasItem(1));
        assertThat(map1.getOrElse(1,Vector.empty()),hasItem(3));

        assertEquals(2, map1.size());


    }


}

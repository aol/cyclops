package cyclops.collections.tuple;

import cyclops.companion.Monoids;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 04/10/2017.
 */
public class Tuple2Test {
    Tuple2<Integer,Integer> t2;
    @Before
    public void setUp() throws Exception {
        t2 = Tuple.tuple(5,10);
        called=  0;

    }

    @Test
    public void of() throws Exception {
        assertThat(Tuple2.of(5,10),equalTo(t2));
    }

    int called;
    @Test
    public void lazy() throws Exception {
        Tuple2<Integer,String> lazyT1 = Tuple.lazy(()->2,()->{
            called++;
            return "hello";
        });
        assertThat(called,equalTo(0));
        assertThat(lazyT1._2(),equalTo("hello"));
        assertThat(called++,equalTo(1));
    }

    @Test
    public void _1() throws Exception {

        assertThat(t2._1(),equalTo(5));
    }

    @Test
    public void _2() throws Exception {

        assertThat(t2._2(),equalTo(10));
    }





    @Test
    public void map() throws Exception {
        assertThat(t2.bimap(i->i+1,i->i+1),equalTo(Tuple.tuple(6,11)));
    }




    @Test
    public void eager() throws Exception {
        assertThat(Tuple.lazy(()->"boo!",()->"hello").eager(),equalTo(Tuple.tuple("boo!","hello")));
    }

    @Test
    public void memo(){
        Tuple2<Integer,String> lazy = Tuple.lazy(()->10,()->{
            called++;
            return "lazy";
        });
        lazy._2();
        lazy._2();
        assertThat(called,equalTo(2));
        called= 0;
        Tuple2<Integer,String> memo = lazy.memo();
        memo._2();
        memo._2();
        assertThat(called,equalTo(1));
    }


    @Test
    public void lazyMap() throws Exception {
        assertThat(t2.lazyBimap(i->i+1,i->i+1),equalTo(Tuple.tuple(6,11)));
        t2.lazyBimap(i->{
            called++;
            return i+1;
        },i->i);
        assertThat(called,equalTo(0));
    }


    @Test
    public void flatMap() throws Exception {
        assertThat( t2.flatMap(Monoids.intSum, i->Tuple.tuple(5,i+1)),equalTo(Tuple.tuple(10,11)));
    }



    @Test
    public void visit() throws Exception {
        assertThat(t2.visit((a,b)->a+b),equalTo(15));
    }

    @Test
    public void testToString() throws Exception {
        assertThat(t2.toString(),equalTo("[5,10]"));
    }

    @Test
    public void swap(){
        assertThat(t2.swap(),equalTo(Tuple.tuple(10,5)));
    }
    @Test
    public void lazySwap(){
        assertThat(t2.lazySwap(),equalTo(Tuple.tuple(10,5)));
        Tuple.lazy(()->{
            called++;
            return "hello";
        },()->10).lazySwap();
        assertThat(called,equalTo(0));
    }




}
package cyclops.data.tuple;

import cyclops.control.Trampoline;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 04/10/2017.
 */
public class Tuple1Test {
    Tuple1<Integer> t1;
    @Before
    public void setUp() throws Exception {
        t1 = Tuple.tuple(10);
        called=  0;
        value = 0;
    }

    @Test
    public void of() throws Exception {
        assertThat(Tuple1.of(10),equalTo(t1));
    }

    int called;
    @Test
    public void lazy() throws Exception {
        Tuple1<String> lazyT1 = Tuple.lazy(()->{
            called++;
            return "hello";
        });
        assertThat(called,equalTo(0));
        assertThat(lazyT1._1(),equalTo("hello"));
        assertThat(called++,equalTo(1));
    }

    @Test
    public void _1() throws Exception {

        assertThat(t1._1(),equalTo(10));
    }

    @Test
    public void toIdentity() throws Exception {
        assertThat(t1.toIdentity().get(),equalTo(10));
    }

    @Test
    public void map() throws Exception {
        assertThat(t1.map(i->i+1),equalTo(Tuple.tuple(11)));
    }

    int value =0;
    @Test
    public void peek() throws Exception {
        t1.peek(i->value=i);
        assertThat(value,equalTo(10));
    }



    @Test
    public void retry() throws Exception {
        assertThat(t1.retry(i->i+1),equalTo(Tuple.tuple(11)));
        assertThat(t1.retry(i->{
            if(called++==0)
                throw new RuntimeException("boo!");
        return i+1;
        }),equalTo(Tuple.tuple(11)));
    }

    @Test
    public void eager() throws Exception {
        assertThat(Tuple.lazy(()->"hello").eager(),equalTo(Tuple.tuple("hello")));
    }

    @Test
    public void memo(){
        Tuple1<String> lazy = Tuple.lazy(()->{
            called++;
            return "lazy";
        });
        lazy._1();
        lazy._1();
        assertThat(called,equalTo(2));
        called= 0;
        Tuple1<String> memo = lazy.memo();
        memo._1();
        memo._1();
        assertThat(called,equalTo(1));
    }

    @Test
    public void retry1() throws Exception {
        assertThat(t1.retry(i->i+1,5,10, TimeUnit.MILLISECONDS),equalTo(Tuple.tuple(11)));
        assertThat(t1.retry(i->{
            if(called++==0)
                throw new RuntimeException("boo!");
            return i+1;
        },5,10, TimeUnit.MILLISECONDS),equalTo(Tuple.tuple(11)));
    }

    @Test
    public void lazyMap() throws Exception {
        assertThat(t1.lazyMap(i->i+1),equalTo(Tuple.tuple(11)));
        t1.lazyMap(i->{
            called++;
            return i+1;
        });
        assertThat(called,equalTo(0));
    }

    @Test
    public void zip() throws Exception {
        assertThat(t1.zip(Tuple.tuple(2),(a,b)->a+b),equalTo(Tuple.tuple(12)));
    }

    @Test
    public void lazyZip() throws Exception {
        assertThat(t1.lazyZip(Tuple.tuple(2),(a,b)->a+b),equalTo(Tuple.tuple(12)));
        t1.lazyZip(Tuple.tuple(2),(a,b)->{
            called++;
            return a+b;
        });
        assertThat(called,equalTo(0));
    }


    @Test
    public void flatMap() throws Exception {
        assertThat( t1.flatMap(i->Tuple.tuple(i+1)),equalTo(Tuple.tuple(11)));
    }

    @Test
    public void lazyFlatMap() throws Exception {
        assertThat( t1.lazyFlatMap(i->Tuple.tuple(i+1)),equalTo(Tuple.tuple(11)));
        t1.lazyFlatMap(i->{
            called++;
            return Tuple.tuple(i+1);
        });
        assertThat(called,equalTo(0));
    }

    @Test
    public void visit() throws Exception {
        assertThat(t1.visit(i->i+1),equalTo(11));
    }

    @Test
    public void testToString() throws Exception {
        assertThat(t1.toString(),equalTo("[10]"));
    }



    @Test
    public void ofType() throws Exception {
        assertThat(t1.ofType(String.class).isPresent(),equalTo(false));
        assertThat(t1.ofType(Number.class).isPresent(),equalTo(true));
    }

    @Test
    public void filterNot() throws Exception {
        assertTrue(t1.filterNot(i->i<5).isPresent());
        assertFalse(t1.filterNot(i->i>5).isPresent());
    }

    @Test
    public void notNull() throws Exception {
        assertTrue(t1.notNull().isPresent());
    }




    @Test
    public void testFilter() {
        assertFalse(t1.filter(i->i<5).isPresent());
        assertTrue(t1.filter(i->i>5).isPresent());


    }


    private Trampoline<Integer> sum(int times, int sum){
        return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
    }
    @Test
    public void testTrampoline() {
        assertThat(t1.trampoline(n ->sum(10,n)),equalTo(Tuple1.of(65)));
    }





}
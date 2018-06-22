package cyclops.control.trampoline;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cyclops.data.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

import cyclops.control.Trampoline;

import lombok.val;


public class TrampolineTest {

	@Test
	public void trampolineTest(){

		assertThat(loop(500000,10).result(),equalTo(446198426));

	}

	@Test
  public void testMap(){
	  assertThat(Trampoline.done(10).map(i->i*2).get(),equalTo(20));
    assertThat(Trampoline.more(()->Trampoline.done(10)).map(i->i*2).get(),equalTo(20));
    assertThat(loop(500000,10).map(i->i*2).get(),equalTo(892396852));
  }
  @Test
  public void testFlatMap(){
    assertThat(Trampoline.done(10).flatMap(i->Trampoline.done(i*2)).get(),equalTo(20));
    assertThat(Trampoline.more(()->Trampoline.done(10)).flatMap(i->Trampoline.done(i*2)).get(),equalTo(20));
    assertThat(loop(500000,10).flatMap(i->Trampoline.done(i*2)).get(),equalTo(892396852));
  }


	@Test @Ignore
	public void trampolineTest1(){

		assertThat(loop1(500000,10),equalTo(446198426));

	}
	Integer loop1(int times,int sum){

		if(times==0)
			return sum;
		else
			return loop1(times-1,sum+times);
	}
    @Test
    public void interleave(){

	    Trampoline<Integer> algorithm1 = loop(50000,5);
        Trampoline<Integer> algorithm2 = loop2(50000,5);

        //interleaved execution via Zip!
        Tuple2<Integer, Integer> result = algorithm1.zip(algorithm2).get();

        System.out.println(result);
    }

    Trampoline<Integer> loop2(int times,int sum){

        System.out.println("Loop-B " + times + " : " + sum);
        if(times==0)
            return Trampoline.done(sum);
        else
            return Trampoline.more(()->loop2(times-1,sum+times));
    }

    Trampoline<Integer> loop(int times,int sum){
        System.out.println("Loop-A " + times + " : " + sum);
        if(times==0)
            return Trampoline.done(sum);
        else
            return Trampoline.more(()->loop(times-1,sum+times));
    }




    @Test
    public void interleave3(){
        Trampoline<Integer> looping = loop(50000,5);
        Trampoline<Integer> looping2 = loop2(50000,5);
        Trampoline<Integer> looping3 = loop2(50000,5);
        System.out.println(looping.zip(looping2,looping3).get());
    }

	List results;
	@Test
	public void coroutine(){
		results = new ArrayList();
		Iterator<String> it = Arrays.asList("hello","world","take").iterator();
		val coroutine = new Trampoline[1];
		coroutine[0] = Trampoline.more( ()-> it.hasNext() ? print(it.next(),coroutine[0]) : Trampoline.done(0));
		withCoroutine(coroutine[0]);

		assertThat(results,equalTo(Arrays.asList(0,"hello",1,"world",2,"take",3,4)));
	}

	private Trampoline<Integer> print(Object next, Trampoline trampoline) {
		System.out.println(next);
		results.add(next);
		return trampoline;
	}
	public void withCoroutine(Trampoline coroutine){

		for(int i=0;i<5;i++){
				print(i,coroutine);
				if(!coroutine.complete())
					coroutine= coroutine.bounce();

		}

	}
}

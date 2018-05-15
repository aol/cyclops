package cyclops.streams.observables;

import cyclops.companion.Streams;
import cyclops.companion.rx2.Observables;
import cyclops.reactive.ObservableReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class AsyncForEachTest {
	boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	protected <U> ReactiveSeq<U> of(U... array){

		ReactiveSeq<U> seq = Spouts.async(s->{
			Thread t = new Thread(()-> {
				for (U next : array) {
					s.onNext(next);
				}
				s.onComplete();
			});
			t.start();
		});
		return ObservableReactiveSeq.reactiveSeq(Observables.observableFrom(seq));
	}
	@Test
	public void forEachX(){
		Subscription s = Streams.forEach(of(1,2,3), 2, System.out::println);
		System.out.println("first batch");
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = Streams.forEach(of(1,2,3), 2, i->list.add(i));
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	volatile Throwable error;
	@Test
	public void forEachXWithErrors(){

		List<Integer> list = new ArrayList<>();

		Stream<Integer> stream = of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);
		Subscription s = Streams.forEach(stream, 2, i->list.add(i),
								e->error=e);

		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		System.out.println("first batch");
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		assertThat(error,nullValue());
		s.request(2);
	//	assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachXWithEvents(){

		List<Integer> list = new ArrayList<>();

		Stream<Integer> stream = of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);
		Subscription s = Streams.forEach(stream, 2, i->list.add(i),
								e->error=e,()->complete=true);

		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		System.out.println("first batch");
		s.request(1);
		assertFalse(complete);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		assertThat(error,nullValue());
		s.request(2);
	//	assertThat(error,instanceOf(RuntimeException.class));

		assertTrue(complete);
	}


	@Test
	public void forEachWithErrors(){

		List<Integer> list = new ArrayList<>();
		assertThat(error,nullValue());
		Stream<Integer> stream = of(()->1,()->2,()->3,
				(Supplier<Integer>)()->{ throw new RuntimeException();})
                .map(Supplier::get);
		Streams.forEach(stream, i->list.add(i),
								e->error=e);

		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));

		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));


	//	assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachWithEvents(){

		List<Integer> list = new ArrayList<>();
		assertFalse(complete);
		assertThat(error,nullValue());
		Stream<Integer> stream = of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);
		Streams.forEach(stream, i->list.add(i), e->error=e,()->complete=true);



		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));


//		assertThat(error,instanceOf(RuntimeException.class));

		assertTrue(complete);
	}
}

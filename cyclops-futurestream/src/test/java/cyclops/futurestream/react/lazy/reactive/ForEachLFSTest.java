package cyclops.futurestream.react.lazy.reactive;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import cyclops.async.LazyReact;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import cyclops.reactive.FutureStream;

public class ForEachLFSTest {
	boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	@Test
	public void forEachX(){
		Subscription s = LazyReact.sequentialBuilder().of(1,2,3).forEach( 2, System.out::println);
		System.out.println("takeOne batch");
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = LazyReact.sequentialBuilder().of(1,2,3).forEach( 2, i->list.add(i));
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	@Test
	public void forEachXTestIsComplete(){
		List<Integer> list = new ArrayList<>();
		Subscription s = LazyReact.sequentialBuilder().of(1,2,3).forEach( 2, i->list.add(i));
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	Throwable error;
	@Test
	public void forEachXWithErrors(){

		List<Integer> list = new ArrayList<>();

		Subscription s = LazyReact.sequentialBuilder().of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
							.map(Supplier::get)
							.forEach( 2, i->list.add(i),
								e->error=e);

		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		System.out.println("takeOne batch");
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		assertThat(error,nullValue());
		s.request(2);
		assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachXWithEvents(){

		List<Integer> list = new ArrayList<>();

		Subscription s = LazyReact.sequentialBuilder().of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
						.forEach( 2, i->list.add(i),
								e->error=e,()->complete=true);

		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		System.out.println("takeOne batch");
		s.request(1);
		assertFalse(complete);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		assertThat(error,nullValue());
		s.request(2);
		assertThat(error,instanceOf(RuntimeException.class));

		assertTrue(complete);
	}


	@Test
	public void forEachWithErrors() throws InterruptedException{

		List<Integer> list = new ArrayList<>();
		assertThat(error,nullValue());
		LazyReact.sequentialBuilder().of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
											.forEach(i->list.add(i),
															e->error=e);

		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));

		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));



		assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachWithErrorsStream(){

		List<Integer> list = new ArrayList<>();
		assertThat(error,nullValue());
		FutureStream<Integer> stream = LazyReact.sequentialBuilder().of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);
		stream.forEach(i->list.add(i),
								e->error=e);

		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));

		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));


		assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachWithEvents(){

		List<Integer> list = new ArrayList<>();
		assertFalse(complete);
		assertThat(error,nullValue());
		LazyReact.sequentialBuilder().of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
				.map(Supplier::get)
				 .forEach(i->list.add(i), e->error=e,()->complete=true);



		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));


		assertThat(error,instanceOf(RuntimeException.class));

		assertTrue(complete);
	}
}

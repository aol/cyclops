package cyclops.futurestream.react.lazy.futures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import cyclops.async.LazyReact;
import org.junit.Test;

public class AccessTest {
	@Test
	public void get0(){
		assertThat(LazyReact.sequentialBuilder().of(1).actOnFutures().get(0)._1(),equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5).actOnFutures().get(2)._1(),equalTo(3));
	}
	@Test
	public void elementAt0(){
		assertTrue(LazyReact.sequentialBuilder().of(1).actOnFutures().elementAt(0).isPresent());
	}
	@Test
	public void elementAtMultple(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5).actOnFutures().elementAt(2).get(),equalTo(3));
	}
	@Test
	public void elementAt1(){
		assertFalse(LazyReact.sequentialBuilder().of(1).actOnFutures().elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(LazyReact.sequentialBuilder().of().actOnFutures().elementAt(0).isPresent());
	}


}

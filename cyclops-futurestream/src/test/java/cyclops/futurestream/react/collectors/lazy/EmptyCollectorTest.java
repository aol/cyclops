package cyclops.futurestream.react.collectors.lazy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.oath.cyclops.internal.react.async.future.FastFuture;
import org.junit.Before;
import org.junit.Test;

public class EmptyCollectorTest {

	EmptyCollector collector;
	@Before
	public void setup(){
		collector = new EmptyCollector();
	}
	@Test
	public void testAccept() {
		for(int i=0;i<1000;i++){
			collector.accept(FastFuture.completedFuture(10l));
		}
	}
	@Test
	public void testAcceptMock() {
		FastFuture cf = Mockito.mock(FastFuture.class);
		BDDMockito.given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		Mockito.verify(cf, Mockito.atLeastOnce()).isDone();
	}
	@Test
	public void testAcceptMock495() {
		collector = new EmptyCollector<>(new MaxActive(500,5),cf -> cf.join());
		FastFuture cf = Mockito.mock(FastFuture.class);
		BDDMockito.given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		Mockito.verify(cf, Mockito.times(501)).isDone();
	}
	@Test
	public void testAcceptMock50() {
		collector = new EmptyCollector<>(new MaxActive(500,450),cf -> cf.join());
		FastFuture cf = Mockito.mock(FastFuture.class);
		BDDMockito.given(cf.isDone()).willReturn(true);
		for(int i=0;i<1000;i++){
			collector.accept(cf);
		}
		Mockito.verify(cf, Mockito.times(501)).isDone();
	}

	@Test
	public void testWithResults() {

		collector = collector.withMaxActive(new MaxActive(4,3));
		assertThat(collector.withResults(null).getMaxActive().getMaxActive(),is(4));
	}

	@Test
	public void testGetResults() {
		assertTrue(collector.getResults().isEmpty());
	}

	@Test
	public void testGetMaxActive() {
		assertThat(collector.getMaxActive().getMaxActive(),is(MaxActive.IO.getMaxActive()));
	}




}

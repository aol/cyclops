package cyclops.reactive.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cyclops.reactive.collections.mutable.ListX;

public class SchedulingTest {

	ScheduledExecutorService ex =Executors.newScheduledThreadPool(1);
	AtomicInteger count = new AtomicInteger(0);
	@Test
	public void cronTest() throws InterruptedException{
		ListX.of(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
                .stream()
				.schedule("* * * * * ?", ex);

		Thread.sleep(5000);

	}
	@Test
	public void cronDebounceTest() throws InterruptedException{
		assertThat(ListX.of(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
                .stream()
                .schedule("* * * * * ?", ex)
				.connect()
				.debounce(1,TimeUnit.DAYS)
				.peek(System.out::println)
				.toList(),equalTo(Arrays.asList(1)));


	}
	@Test
	public void fixedRateTest() throws InterruptedException{
		ListX.of(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
                 .stream()
				.scheduleFixedRate(1000, ex)
				.connect()
                .forEach(e->System.out.println("Result  " + e));

		System.out.println("***************");
		assertThat(ListX.of(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
                 .stream()
				.scheduleFixedRate(1000, ex)
				.connect()
				.debounce(1,TimeUnit.DAYS)
				.peek(System.out::println)
				.toList(),equalTo(Arrays.asList(1)));


	}
	@Test
	public void fixedRateDelay() throws InterruptedException{
		assertThat(ListX.of(1,2,3,4)
				.peek(i->count.incrementAndGet())
				.peek(System.out::println)
                .stream()
				.scheduleFixedDelay(1000, ex)
				.connect()
				.debounce(1,TimeUnit.DAYS)
				.peek(System.out::println)
				.toList(),equalTo(Arrays.asList(1)));


	}
}

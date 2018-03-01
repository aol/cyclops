package cyclops.futurestream.react.lazy.sequence;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import cyclops.data.Seq;
import cyclops.data.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.oath.cyclops.react.ThreadPools;
import cyclops.data.Vector;
import cyclops.futurestream.react.lazy.DuplicationTest;
import com.oath.cyclops.util.SimpleTimer;
import cyclops.futurestream.LazyReact;
import org.junit.Test;

import cyclops.reactive.collections.mutable.ListX;
import lombok.Value;
public class BatchingTest {
	@Test
	public void batchUntil(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().size(),equalTo(2));
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().get(0),equalTo(Vector.of(1,2,3)));
	}
	@Test
	public void batchWhile(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList()
				.size(),equalTo(2));
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList(),equalTo(Arrays.asList(Vector.of(1,2,3),Vector.of(4,5,6))));
	}
	@Test
	public void batchUntilCollection(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0,()-> Vector.empty())
				.toList().size(),equalTo(2));
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0,()->Vector.empty())
				.toList().get(0),equalTo(Vector.of(1,2,3)));
	}
	@Test
	public void batchWhileCollection(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0,()->Vector.empty())
				.toList().size(),equalTo(2));
		assertThat(DuplicationTest.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0,()->Vector.empty())
				.toList(),equalTo(Arrays.asList(Vector.of(1,2,3),Vector.of(4,5,6))));
	}
	@Test
	public void batchByTime2(){
		for(int i=0;i<5;i++){
			System.out.println(i);
			assertThat(DuplicationTest.of(1,2,3,4,5, 6)
							.map(n-> n==6? sleep(1) : n)
							.groupedByTime(10,TimeUnit.MICROSECONDS)
							.toList()
							.get(0)
							,not(hasItem(6)));
		}
	}
	private Integer sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	@Test
	public void windowwByTime2(){
		for(int i=0;i<5;i++){
			System.out.println(i);
			assertThat(DuplicationTest.of(1,2,3,4,5, 6)
							.map(n-> n==6? sleep(1) : n)
							.groupedByTime(10,TimeUnit.MICROSECONDS)
							.toList()
							.get(0)
							,not(hasItem(6)));
		}
	}



	@Value
	static class Status{
		long id;
	}
	private String saveStatus(Status s) {
		//if (count++ % 2 == 0)
		//	throw new RuntimeException();

		return "Status saved:" + s.getId();
	}


	AtomicInteger count2;
	volatile int otherCount;
	volatile int count3;
	volatile int peek;
	@Test
	public void windowByTimeFiltered() {

		for(int x=0;x<10;x++){
			count2=new AtomicInteger(0);
			List<Collection<Map>> result = new ArrayList<>();

					new LazyReact(ThreadPools.getCommonFreeThread()).iterate("", last -> "hello")
					.limit(1000)

					.peek(i->System.out.println(++otherCount))

					.groupedByTime(1, TimeUnit.MICROSECONDS)

					.peek(batch -> System.out.println("batched : " + batch + ":" + (++peek)))

					.peek(batch->count3= count3+(int)batch.stream().count())

					.forEach(next -> {
					count2.getAndAdd((int)next.stream().count());});


			System.out.println("In flight count " + count3 + " :" + otherCount);
			System.out.println(result.size());
			System.out.println(result);
			System.out.println("x" +x);
			assertThat(count2.get(),equalTo(1000));

		}
	}
	@Test
	public void batchByTimex() {


		new LazyReact(ThreadPools.getCommonFreeThread()).iterate("", last -> "next")
				.limit(100)


				.peek(next->System.out.println("Counter " +count2.incrementAndGet()))
				.groupedByTime(10, TimeUnit.MICROSECONDS)
				.peek(batch -> System.out.println("batched : " + batch))
				.filter(c->!c.isEmpty())


				.forEach(System.out::println);


	}
	@Test
	public void windowByTimex() {


		new LazyReact(ThreadPools.getCommonFreeThread()).iterate("", last -> "next")
				.limit(100)


				.peek(next->System.out.println("Counter " +count2.incrementAndGet()))
				.groupedByTime(10, TimeUnit.MICROSECONDS)
				.peek(batch -> System.out.println("x " + batch))
				.filter(c->! (c.stream().count()==0))


				.forEach(System.out::println);


	}

	@Test
	public void batchBySize3(){
		System.out.println(DuplicationTest.of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(DuplicationTest.of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void batchBySizeAndTimeSizeCollection(){

		assertThat(DuplicationTest.of(1,2,3,4,5,6)
						.groupedBySizeAndTime(3,10,TimeUnit.SECONDS,()->Vector.empty())
						.toList().get(0)
						.size(),is(3));
	}
	@Test
	public void batchBySizeAndTimeSize(){

		assertThat(DuplicationTest.of(1,2,3,4,5,6)
						.groupedBySizeAndTime(3,10,TimeUnit.SECONDS)
						.toList().get(0)
						.size(),is(3));
	}
	@Test
	public void windowBySizeAndTimeSize(){

		assertThat(DuplicationTest.of(1,2,3,4,5,6)
						.groupedBySizeAndTime(3,10,TimeUnit.SECONDS)
						.toList().get(0).stream()
						.count(),is(3l));
	}
	@Test
	public void windowBySizeAndTimeSizeEmpty(){

		assertThat(DuplicationTest.of()
						.groupedBySizeAndTime(3,10,TimeUnit.SECONDS)
						.toList()
						.size(),is(0));
	}
	@Test
	public void batchBySizeAndTimeTime(){

		for(int i=0;i<10;i++){
			System.out.println(i);
			List<Vector<Integer>> list = DuplicationTest.of(1,2,3,4,5,6)
					.groupedBySizeAndTime(10,1,TimeUnit.MICROSECONDS)
					.toList();

			assertThat(list
							.get(0)
							,not(hasItem(6)));
		}
	}
	@Test
	public void batchBySizeAndTimeTimeCollection(){

		for(int i=0;i<10;i++){
			System.out.println(i);
			List<Vector<Integer>> list = DuplicationTest.of(1,2,3,4,5,6)
					.groupedBySizeAndTime(10,1,TimeUnit.MICROSECONDS,()->Vector.empty())
					.toList();

			assertThat(list
							.get(0)
							,not(hasItem(6)));
		}
	}
	@Test
	public void windowBySizeAndTimeTime(){

		for(int i=0;i<10;i++){
			System.out.println(i);
			List<Vector<Integer>> list = DuplicationTest.of(1,2,3,4,5,6)
					.map(n-> n==6? sleep(1) : n)
					.groupedBySizeAndTime(10,1,TimeUnit.MICROSECONDS)

					.toList();

			assertThat(list.get(0)
							,not(hasItem(6)));
		}
	}


	@Test
	public void batchBySizeSet(){

		assertThat(DuplicationTest.of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().get(0).size(),is(1));
		assertThat(DuplicationTest.of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().size(),is(1));
	}
	@Test
	public void batchBySizeSetEmpty(){

		assertThat(DuplicationTest.<Integer>of().grouped(3,()->TreeSet.empty()).toList().size(),is(0));
	}
	@Test
	public void batchBySizeInternalSize(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).get(0).size(),is(3));
	}
	@Test
	public void fixedDelay(){
		SimpleTimer timer = new SimpleTimer();

		assertThat(DuplicationTest.of(1,2,3,4,5,6).fixedDelay(10000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
	}
	@Test
	public void judder(){
		SimpleTimer timer = new SimpleTimer();

		assertThat(DuplicationTest.of(1,2,3,4,5,6).jitter(10000).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(20000l));
	}
	@Test
	public void debounce(){
		SimpleTimer timer = new SimpleTimer();


		assertThat(DuplicationTest.of(1,2,3,4,5,6).debounce(1000,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));

	}
	@Test
	public void debounceOk(){
		System.out.println(DuplicationTest.of(1,2,3,4,5,6).debounce(1,TimeUnit.NANOSECONDS).toList());
		assertThat(DuplicationTest.of(1,2,3,4,5,6).debounce(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));

	}
	@Test
	public void onePer(){
		SimpleTimer timer = new SimpleTimer();
		System.out.println(DuplicationTest.of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()));
		assertThat(DuplicationTest.of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(600l));
	}
	@Test
	public void xPer(){
		SimpleTimer timer = new SimpleTimer();
		System.out.println(DuplicationTest.of(1,2,3,4,5,6).xPer(6,1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()));
		assertThat(DuplicationTest.of(1,2,3,4,5,6).xPer(6,100000000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),lessThan(60000000l));
	}
	@Test
	public void batchByTime(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6).groupedByTime(1,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
	}
	@Test
	public void batchByTimeSet(){

		assertThat(DuplicationTest.of(1,1,1,1,1,1).groupedByTime(1500,TimeUnit.MICROSECONDS,()->TreeSet.empty()).toList().get(0).size(),is(1));
	}
	@Test
	public void batchByTimeInternalSize(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6).groupedByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	}
	@Test
	public void batchByTimeInternalSizeCollection(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6).groupedByTime(1,TimeUnit.NANOSECONDS,()-> Seq.empty()).collect(Collectors.toList()).size(),greaterThan(5));
	}
	@Test
	public void windowByTimeInternalSize(){
		assertThat(DuplicationTest.of(1,2,3,4,5,6).groupedByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	}

}

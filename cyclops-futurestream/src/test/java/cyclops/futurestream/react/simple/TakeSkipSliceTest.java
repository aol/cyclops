package cyclops.futurestream.react.simple;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.oath.cyclops.types.futurestream.BaseSimpleReactStream;
import com.oath.cyclops.types.futurestream.SimpleReactStream;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import cyclops.futurestream.SimpleReact;

public class TakeSkipSliceTest {

	@Test
	public void skipUntil() {
		SimpleReactStream<Boolean> stoppingStream = SimpleReact
				.sequentialCommonBuilder().ofAsync(() -> 1000)
				.then(this::sleep)
				.peek(System.out::println);
		System.out.println(SimpleReact.sequentialCommonBuilder()
				.from(IntStream.range(0, 1000000))
				// .peek(System.out::println)
				.skipUntil(stoppingStream)
				.peek(System.out::println)
				.toList()
				.size());
	}

	public <T> SimpleReactStream<T> react(Supplier<T>... s){
		return BaseSimpleReactStream.react(s);
	}
	public <T> SimpleReactStream<T> of(T... s){
		return new SimpleReact().of(s);
	}
	@Test
	public void skipUntil2(){

		System.out.println(react(()->1,()->2,()->3,()->4,()->value2())
				.skipUntil(react(()->value())).collect(Collectors.toList()));
		assertTrue(react(()->1,()->2,()->3,()->4,()->value2()).skipUntil(react(()->value())).allMatch(it-> it==200));
		assertThat(react(()->1,()->2,()->3,()->4,()->value2()).skipUntil(react(()->value())).count(),is(1l));
	}
	@Test
	public void takeUntil(){
	  for(int i=0;i<20;i++) {
      System.out.println(react(() -> 1, () -> 2, () -> 3, () -> 4, () -> value2())
        .takeUntil(react(() -> value())).collect(Collectors.toList()));
      assertTrue(react(() -> 1, () -> 2, () -> 3, () -> 4, () -> value2()).takeUntil(react(() -> value())).noneMatch(it -> it == 200));
      assertTrue(react(() -> 1, () -> 2, () -> 3, () -> 4, () -> value2()).takeUntil(react(() -> value())).anyMatch(it -> it == 1));
    }
	}
	@Test
	public void testLimitFutures(){
		assertThat(of(1,2,3,4,5).limit(2).block().size(),is(2));

	}
	@Test
	public void testSkipFutures(){
		assertThat(of(1,2,3,4,5).skip(2).block().size(),is(3));
	}
	@Test
	public void testSliceFutures(){
		assertThat(of(1,2,3,4,5).slice(3,4).block().size(),is(1));
	}
	@Test
	public void testSplitFuturesAt(){
		assertThat(of(1,2,3,4,5).splitAt(2)._1().block().size(),is(asList(1,2).size()));
	}
	@Test
	public void testSplitFuturesAt2(){
		assertThat(sortedList(of(1,2,3,4,5).splitAt(2)
											._2()
											.block()).size(),
											is(asList(3,4,5).size()));
	}
	@Test
	public void duplicateFuturesa(){
		List<String> list = of("a","b").duplicate()._1().block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	private <T> List<T> sortedList(List<T> list) {
		return list.stream().sorted().collect(Collectors.toList());
	}

	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").duplicate()._2().block();
		assertThat(sortedList(list),is(asList("a","b")));

	}
	@Test
	public void testZipWithFutures(){
		SimpleReactStream stream = of("a","b");
		List<Tuple2<Integer,String>> result = of(1,2).zip(stream).block();

		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}
	@Test
	public void testZipWithFuturesStream(){
		Stream stream = Stream.of("a","b");
		List<Tuple2<Integer,String>> result = of(1,2).zip(stream).block();

		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}


	@Test
	public void testZipFuturesWithIndex(){

		List<Tuple2<String,Long>> result  = of("a","b").zipWithIndex().block();

		assertThat(result.size(),is(asList(tuple("a",0l),tuple("b",1l)).size()));
	}
	@Test
	public void duplicateFutures(){
		List<String> list = of("a","b").duplicate()._1().block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	private boolean sleep(int i) {

		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		return true;

	}
	protected int value2() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 200;
	}
	protected Object value() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "jello";
	}
}

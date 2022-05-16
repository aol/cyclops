package cyclops.futurestream.react.lazy.sequence;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import cyclops.data.Seq;
import cyclops.futurestream.LazyReact;
import org.junit.Test;

import cyclops.companion.Reducers;


public class ReductionTest {

	@Test
	public void reduceWithMonoid(){

		assertThat(LazyReact.sequentialBuilder().of("hello","2","world","4").foldMap(Reducers.toCountInt()),equalTo(4));
	}
	@Test
	public void reduceWithMonoid2(){

		assertThat(LazyReact.sequentialBuilder().of("replaceWith","two","three","four").foldMap(this::toInt,Reducers.toTotalInt()),
						equalTo(10));
	}

	int toInt(String s){
		if("replaceWith".equals(s))
			return 1;
		if("two".equals(s))
			return 2;
		if("three".equals(s))
			return 3;
		if("four".equals(s))
			return 4;
		return -1;
	}
	@Test
	public void foldLeftMapToTypeWithMonoidJoin(){
		LazyReact.sequentialBuilder().of("hello","2","world","4").join(",");
		assertThat(LazyReact.sequentialBuilder().of("hello","2","world","4").reduce(Reducers.toString(",")),
				equalTo(",hello,2,world,4"));
	}
	@Test
	public void foldLeftWithMonoidJoin(){
		LazyReact.sequentialBuilder().of("hello","2","world","4").join(",");
		assertThat(LazyReact.sequentialBuilder().of("hello","2","world","4").reduce(Reducers.toString(",")),
				equalTo(",hello,2,world,4"));
	}
	@Test
	public void reduceWithMonoidJoin(){
		LazyReact.sequentialBuilder().of("hello","2","world","4").join(",");
		assertThat(LazyReact.sequentialBuilder().of("hello","2","world","4").reduce(Reducers.toString(",")),
				equalTo(",hello,2,world,4"));
	}
	@Test
	public void reduceWithMonoidStreamJoin(){
		LazyReact.sequentialBuilder().of("hello","2","world","4").join(",");
		assertThat(LazyReact.sequentialBuilder().of("hello","2","world","4").reduce(List.of(Reducers.toString(","))),
				equalTo(Seq.of(",hello,2,world,4")));
	}
	@Test
	public void reduceWithMonoidListJoin(){
		LazyReact.sequentialBuilder().of("hello","2","world","4").join(",");
		assertThat(LazyReact.sequentialBuilder().of("hello","2","world","4").reduce(Arrays.asList(Reducers.toString(","))),
				equalTo(Seq.of(",hello,2,world,4")));
	}


}

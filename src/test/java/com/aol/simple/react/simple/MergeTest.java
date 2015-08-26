package com.aol.simple.react.simple;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.EagerSimpleReactStream;

public class MergeTest {

	@Test
	public void testMerge() throws InterruptedException, ExecutionException {

		EagerSimpleReactStream<String> stage1 = new SimpleReact().<Integer> react(() -> 1,
				() -> 2, () -> 3).then(it -> "*" + it);
		EagerSimpleReactStream<String> stage2 = new SimpleReact().<Integer> react(() -> 4,
				() -> 5, () -> 6).then(it -> "*" + it);

		List<String> result = stage1.merge(stage2).block();

		assertThat(result.size(), is(6));

	}

	@Test
	public void testMergeTypes() throws InterruptedException,
			ExecutionException {

		EagerSimpleReactStream<Integer> stage1 = new SimpleReact().<Integer> react(() -> 1,
				() -> 2, () -> 3);
		EagerSimpleReactStream<String> stage2 = new SimpleReact().<Integer> react(() -> 4,
				() -> 5, () -> 6).then(it -> "*" + it);

		List<Object> result = EagerSimpleReactStream.<Object> merge(stage1, stage2).block();

		assertThat(result.size(), is(6));

	}

	@Test
	public void testSplitAndMerge() throws InterruptedException,
			ExecutionException {

		EagerSimpleReactStream<String> stage = new SimpleReact().<Integer> react(() -> 1,
				() -> 2, () -> 3).then(it -> "*" + it);
		EagerSimpleReactStream<String> stage1 = stage.filter(it -> it.startsWith("*1"));
		EagerSimpleReactStream<String> stage2 = stage.filter(it -> it.startsWith("*2"));
		EagerSimpleReactStream<String> stage3 = stage.filter(it -> it.startsWith("*3"));

		stage1 = stage1.then(it -> it + "!");
		stage2 = stage2.then(it -> it + "*");
		stage3 = stage3.then(it -> it + "%");

		List<String> result = stage1.merge(stage2).merge(stage3).block();

		assertThat(result.size(), is(3));
		assertThat(result, hasItem("*1!"));
		assertThat(result, hasItem("*2*"));
		assertThat(result, hasItem("*3%"));

	}

	@Test
	public void mergeAndContinueProcessing() {
		EagerSimpleReactStream<String> stage1 = new SimpleReact().<Integer> react(() -> 1,
				() -> 2, () -> 3).then(it -> "*" + it);
		EagerSimpleReactStream<String> stage2 = new SimpleReact().<Integer> react(() -> 4,
				() -> 5, () -> 6).then(it -> "*" + it);

		List<String> result = stage1.merge(stage2).then(it -> it +"*").block();
		
		result.stream().forEach( it-> assertThat(it,endsWith("*")));
	}
	@Test
	public void mergeAndForkProcessing() {
		EagerSimpleReactStream<String> stage1 = new SimpleReact().<Integer> react(() -> 1,
				() -> 2, () -> 3).then(it -> "*" + it);
		EagerSimpleReactStream<String> stage2 = new SimpleReact().<Integer> react(() -> 4,
				() -> 5, () -> 6).then(it -> "*" + it);

		List<String> result1 = stage1.merge(stage2).then(it -> it +"*")
				.peek(it -> System.out.println(it)).block();
		List<String> result2 = stage1.merge(stage2).then(it -> it +"-").block();
		
		result1.stream().forEach( it-> assertThat(it,endsWith("*")));
		result2.stream().forEach( it-> assertThat(it,endsWith("-")));
		
			
		
		
	}
}

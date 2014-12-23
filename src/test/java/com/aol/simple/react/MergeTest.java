package com.aol.simple.react;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class MergeTest {

	@Test
	public void testMerge() throws InterruptedException,
			ExecutionException {
		 
		Stage<String> stage1 = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> "*" + it);
		Stage<String> stage2 = new SimpleReact()
				.<Integer> react(() -> 4, () -> 5, () -> 6)
				.then(it -> "*" + it);
		
		List<String> result = stage1.merge(stage2).block();
				

		assertThat(result.size(), is(6));

	}
	@Test
	public void testMergeTypes() throws InterruptedException,
			ExecutionException {
		 
		Stage<Integer> stage1 = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3);
		Stage<String> stage2 = new SimpleReact()
				.<Integer> react(() -> 4, () -> 5, () -> 6)
				.then(it -> "*" + it);
		
		List<Object> result = Stage.<Object>merge(stage1, stage2).block();
				

		assertThat(result.size(), is(6));

	}
	
	
	@Test
	public void testSplitAndMerge() throws InterruptedException,
			ExecutionException {
		 
		Stage<String> stage = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> "*" + it);
		Stage<String> stage1 = stage.filter(it -> it.startsWith("*1"));
		Stage<String> stage2 = stage.filter(it -> it.startsWith("*2"));
		Stage<String> stage3 = stage.filter(it -> it.startsWith("*3"));
		
		stage1 = stage1.then(it -> it+"!");
		stage2 = stage2.then(it -> it+"*");
		stage3 = stage3.then(it -> it+"%");
		
		List<String> result = stage1.merge(stage2).merge(stage3).block();
				
		
		assertThat(result.size(), is(3));
		assertThat(result, hasItem("*1!"));
		assertThat(result, hasItem("*2*"));
		assertThat(result, hasItem("*3%"));
		

	}
}

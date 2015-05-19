package com.aol.cyclops.lambda.tuple;
import static org.hamcrest.Matchers.equalTo;
import static com.aol.cyclops.lambda.tuple.LazySwap.lazySwap;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LazySwapTest {


	@Test
	public void swap2(){
		assertThat(lazySwap(PowerTuples.tuple(1,2)),equalTo(PowerTuples.tuple(2,1)));
	}
	@Test
	public void swap2_larger(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3)),equalTo(PowerTuples.tuple(2,1)));
	}
	@Test
	public void swap3(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3)),equalTo(PowerTuples.tuple(3,2,1)));
	}
	@Test
	public void swap3_larger(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4)),equalTo(PowerTuples.tuple(3,2,1)));
	}
	@Test
	public void swap4(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4)),equalTo(PowerTuples.tuple(4,3,2,1)));
	}
	@Test
	public void swap4_larger(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5)),equalTo(PowerTuples.tuple(4,3,2,1)));
	}
	@Test
	public void swap5(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5)),equalTo(PowerTuples.tuple(5,4,3,2,1)));
	}
	@Test
	public void swap5_larger(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5,6)),equalTo(PowerTuples.tuple(5,4,3,2,1)));
	}
	@Test
	public void swap6(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5,6)),equalTo(PowerTuples.tuple(6,5,4,3,2,1)));
	}
	@Test
	public void swap6_larger(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5,6,7)),equalTo(PowerTuples.tuple(6,5,4,3,2,1)));
	}
	@Test
	public void swap7(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5,6,7)),equalTo(PowerTuples.tuple(7,6,5,4,3,2,1)));
	}
	@Test
	public void swap7_larger(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5,6,7,8)),equalTo(PowerTuples.tuple(7,6,5,4,3,2,1)));
	}
	@Test
	public void swap8(){
		assertThat(lazySwap(PowerTuples.tuple(1,2,3,4,5,6,7,8)),equalTo(PowerTuples.tuple(8,7,6,5,4,3,2,1)));
	}

	
	
}

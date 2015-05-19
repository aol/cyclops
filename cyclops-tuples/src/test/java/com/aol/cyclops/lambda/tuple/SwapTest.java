package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SwapTest {
@Test
	public void swap1(){
		assertThat(PowerTuples.tuple(1).swap1(),equalTo(PowerTuples.tuple(1)));
	}
	@Test
	public void swap1_larger(){
		assertThat(PowerTuples.tuple(1,2).swap1(),equalTo(PowerTuples.tuple(1)));
	}
	@Test
	public void swap2(){
		assertThat(PowerTuples.tuple(1,2).swap2(),equalTo(PowerTuples.tuple(2,1)));
	}
	@Test
	public void swap2_larger(){
		assertThat(PowerTuples.tuple(1,2,3).swap2(),equalTo(PowerTuples.tuple(2,1)));
	}
	@Test
	public void swap3(){
		assertThat(PowerTuples.tuple(1,2,3).swap3(),equalTo(PowerTuples.tuple(3,2,1)));
	}
	@Test
	public void swap3_larger(){
		assertThat(PowerTuples.tuple(1,2,3,4).swap3(),equalTo(PowerTuples.tuple(3,2,1)));
	}
	@Test
	public void swap4(){
		assertThat(PowerTuples.tuple(1,2,3,4).swap4(),equalTo(PowerTuples.tuple(4,3,2,1)));
	}
	@Test
	public void swap4_larger(){
		assertThat(PowerTuples.tuple(1,2,3,4,5).swap4(),equalTo(PowerTuples.tuple(4,3,2,1)));
	}
	@Test
	public void swap5(){
		assertThat(PowerTuples.tuple(1,2,3,4,5).swap5(),equalTo(PowerTuples.tuple(5,4,3,2,1)));
	}
	@Test
	public void swap5_larger(){
		assertThat(PowerTuples.tuple(1,2,3,4,5,6).swap5(),equalTo(PowerTuples.tuple(5,4,3,2,1)));
	}
	@Test
	public void swap6(){
		assertThat(PowerTuples.tuple(1,2,3,4,5,6).swap6(),equalTo(PowerTuples.tuple(6,5,4,3,2,1)));
	}
	@Test
	public void swap6_larger(){
		assertThat(PowerTuples.tuple(1,2,3,4,5,6,7).swap6(),equalTo(PowerTuples.tuple(6,5,4,3,2,1)));
	}
	@Test
	public void swap7(){
		assertThat(PowerTuples.tuple(1,2,3,4,5,6,7).swap7(),equalTo(PowerTuples.tuple(7,6,5,4,3,2,1)));
	}
	@Test
	public void swap7_larger(){
		assertThat(PowerTuples.tuple(1,2,3,4,5,6,7,8).swap7(),equalTo(PowerTuples.tuple(7,6,5,4,3,2,1)));
	}
	@Test
	public void swap8(){
		assertThat(PowerTuples.tuple(1,2,3,4,5,6,7,8).swap7(),equalTo(PowerTuples.tuple(8,7,6,5,4,3,2,1)));
	}	
}

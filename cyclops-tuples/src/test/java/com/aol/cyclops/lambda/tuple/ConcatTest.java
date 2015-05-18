package com.aol.cyclops.lambda.tuple;

import static org.junit.Assert.assertThat;
import static com.aol.cyclops.lambda.tuple.Concatenate.*;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.lambda.tuple.PowerTuples;
import static org.hamcrest.Matchers.equalTo;
public class ConcatTest {



	@Test
	public void testConcatPTuple1_1(){
		assertThat(concat(PowerTuples.tuple(10),PowerTuples.tuple(100)).toList()
				,equalTo(Arrays.asList(10,100)));
	}
    

	@Test
	public void testConcatPTuple1_2(){
		assertThat(concat(PowerTuples.tuple(10),PowerTuples.tuple(100,101)).toList()
				,equalTo(Arrays.asList(10,100,101)));
	}
    

	@Test
	public void testConcatPTuple1_3(){
		assertThat(concat(PowerTuples.tuple(10),PowerTuples.tuple(100,101,102)).toList()
				,equalTo(Arrays.asList(10,100,101,102)));
	}
    

	@Test
	public void testConcatPTuple1_4(){
		assertThat(concat(PowerTuples.tuple(10),PowerTuples.tuple(100,101,102,103)).toList()
				,equalTo(Arrays.asList(10,100,101,102,103)));
	}
    

	@Test
	public void testConcatPTuple1_5(){
		assertThat(concat(PowerTuples.tuple(10),PowerTuples.tuple(100,101,102,103,104)).toList()
				,equalTo(Arrays.asList(10,100,101,102,103,104)));
	}
    

	@Test
	public void testConcatPTuple1_6(){
		assertThat(concat(PowerTuples.tuple(10),PowerTuples.tuple(100,101,102,103,104,105)).toList()
				,equalTo(Arrays.asList(10,100,101,102,103,104,105)));
	}
    

	@Test
	public void testConcatPTuple1_7(){
		assertThat(concat(PowerTuples.tuple(10),PowerTuples.tuple(100,101,102,103,104,105,106)).toList()
				,equalTo(Arrays.asList(10,100,101,102,103,104,105,106)));
	}
    


	@Test
	public void testConcatPTuple2_1(){
		assertThat(concat(PowerTuples.tuple(10,11),PowerTuples.tuple(100)).toList()
				,equalTo(Arrays.asList(10,11,100)));
	}
    

	@Test
	public void testConcatPTuple2_2(){
		assertThat(concat(PowerTuples.tuple(10,11),PowerTuples.tuple(100,101)).toList()
				,equalTo(Arrays.asList(10,11,100,101)));
	}
    

	@Test
	public void testConcatPTuple2_3(){
		assertThat(concat(PowerTuples.tuple(10,11),PowerTuples.tuple(100,101,102)).toList()
				,equalTo(Arrays.asList(10,11,100,101,102)));
	}
    

	@Test
	public void testConcatPTuple2_4(){
		assertThat(concat(PowerTuples.tuple(10,11),PowerTuples.tuple(100,101,102,103)).toList()
				,equalTo(Arrays.asList(10,11,100,101,102,103)));
	}
    

	@Test
	public void testConcatPTuple2_5(){
		assertThat(concat(PowerTuples.tuple(10,11),PowerTuples.tuple(100,101,102,103,104)).toList()
				,equalTo(Arrays.asList(10,11,100,101,102,103,104)));
	}
    

	@Test
	public void testConcatPTuple2_6(){
		assertThat(concat(PowerTuples.tuple(10,11),PowerTuples.tuple(100,101,102,103,104,105)).toList()
				,equalTo(Arrays.asList(10,11,100,101,102,103,104,105)));
	}
    


	@Test
	public void testConcatPTuple3_1(){
		assertThat(concat(PowerTuples.tuple(10,11,12),PowerTuples.tuple(100)).toList()
				,equalTo(Arrays.asList(10,11,12,100)));
	}
    

	@Test
	public void testConcatPTuple3_2(){
		assertThat(concat(PowerTuples.tuple(10,11,12),PowerTuples.tuple(100,101)).toList()
				,equalTo(Arrays.asList(10,11,12,100,101)));
	}
    

	@Test
	public void testConcatPTuple3_3(){
		assertThat(concat(PowerTuples.tuple(10,11,12),PowerTuples.tuple(100,101,102)).toList()
				,equalTo(Arrays.asList(10,11,12,100,101,102)));
	}
    

	@Test
	public void testConcatPTuple3_4(){
		assertThat(concat(PowerTuples.tuple(10,11,12),PowerTuples.tuple(100,101,102,103)).toList()
				,equalTo(Arrays.asList(10,11,12,100,101,102,103)));
	}
    

	@Test
	public void testConcatPTuple3_5(){
		assertThat(concat(PowerTuples.tuple(10,11,12),PowerTuples.tuple(100,101,102,103,104)).toList()
				,equalTo(Arrays.asList(10,11,12,100,101,102,103,104)));
	}
    


	@Test
	public void testConcatPTuple4_1(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13),PowerTuples.tuple(100)).toList()
				,equalTo(Arrays.asList(10,11,12,13,100)));
	}
    

	@Test
	public void testConcatPTuple4_2(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13),PowerTuples.tuple(100,101)).toList()
				,equalTo(Arrays.asList(10,11,12,13,100,101)));
	}
    

	@Test
	public void testConcatPTuple4_3(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13),PowerTuples.tuple(100,101,102)).toList()
				,equalTo(Arrays.asList(10,11,12,13,100,101,102)));
	}
    

	@Test
	public void testConcatPTuple4_4(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13),PowerTuples.tuple(100,101,102,103)).toList()
				,equalTo(Arrays.asList(10,11,12,13,100,101,102,103)));
	}
    


	@Test
	public void testConcatPTuple5_1(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13,14),PowerTuples.tuple(100)).toList()
				,equalTo(Arrays.asList(10,11,12,13,14,100)));
	}
    

	@Test
	public void testConcatPTuple5_2(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13,14),PowerTuples.tuple(100,101)).toList()
				,equalTo(Arrays.asList(10,11,12,13,14,100,101)));
	}
    

	@Test
	public void testConcatPTuple5_3(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13,14),PowerTuples.tuple(100,101,102)).toList()
				,equalTo(Arrays.asList(10,11,12,13,14,100,101,102)));
	}


	@Test
	public void testConcatPTuple6_1(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13,14,15),PowerTuples.tuple(100)).toList()
				,equalTo(Arrays.asList(10,11,12,13,14,15,100)));
	}
    

	@Test
	public void testConcatPTuple6_2(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13,14,15),PowerTuples.tuple(100,101)).toList()
				,equalTo(Arrays.asList(10,11,12,13,14,15,100,101)));
	}
    


	@Test
	public void testConcatPTuple7_1(){
		assertThat(concat(PowerTuples.tuple(10,11,12,13,14,15,16),PowerTuples.tuple(100)).toList()
				,equalTo(Arrays.asList(10,11,12,13,14,15,16,100)));
	}
}

package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.val;

import org.junit.Test;

public class ReorderTest {
	int called =0;
	@Test
	public void tuple1(){
		assertThat(PTuple1.of("hello").reorder(t->t.v1()+"2"),equalTo(PowerTuples.tuple("hello2")));
	}
	@Test
	public void tuple2(){
		assertThat(PTuple2.of("hello", "world").reorder(t->t.v2(),t->t.v1()),equalTo(PowerTuples.tuple("world","hello")));
		
	}
	@Test
	public void tuple3(){
		assertThat(PTuple3.of("hello", "world","3").reorder(t->t.v3(),t->t.v2(),t->t.v1()),equalTo(PowerTuples.tuple("3","world","hello")));
		
	}
	@Test
	public void tuple4(){
		assertThat(PTuple4.of("hello", "world","3","4").reorder(t->t.v4(),t->t.v3(),t->t.v2(),t->t.v1()),equalTo(PowerTuples.tuple("4","3","world","hello")));
		
	}
	@Test
	public void tuple5(){
		assertThat(PTuple5.of("hello", "world","3","4","5").reorder(t->t.v5(),t->t.v4(),t->t.v3(),t->t.v2(),t->t.v1()),equalTo(PowerTuples.tuple("5","4","3","world","hello")));
		
	}
	@Test
	public void tuple6(){
		assertThat(PTuple6.of("hello", "world","3","4","5","6").reorder(t->t.v6(),t->t.v5(),t->t.v4(),t->t.v3(),t->t.v2(),t->t.v1()),equalTo(PowerTuples.tuple("6","5","4","3","world","hello")));
		
	}
	@Test
	public void tuple7(){
		assertThat(PTuple7.of("hello", "world","3","4","5","6","7").reorder(t->t.v7(),t->t.v6(),t->t.v5(),t->t.v4(),t->t.v3(),t->t.v2(),t->t.v1()),equalTo(PowerTuples.tuple("7","6","5","4","3","world","hello")));
		
	}
	@Test
	public void tuple8(){
		assertThat(PTuple8.of("hello", "world","3","4","5","6","7","8").reorder(t->t.v8(), t->t.v7(),t->t.v6(),t->t.v5(),t->t.v4(),t->t.v3(),t->t.v2(),t->t.v1()),equalTo(PowerTuples.tuple("8","7","6","5","4","3","world","hello")));
		
	}
	
}

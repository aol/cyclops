package com.aol.cyclops.control;

import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class EvalTest {

	@Test
	public void now(){
		assertThat(Eval.now(1).map(i->i+2).get(),equalTo(3));
	}
	@Test
	public void later(){
		assertThat(Eval.later(()->1).map(i->i+2).get(),equalTo(3));
	}
	int count = 0;
	@Test
	public void laterCaches(){
		count = 0;
		Eval<Integer> eval = Eval.later(()->{ 
			count++;
			return 1;
		});
		eval.map(i->i+2).get();
		eval.map(i->i+2).get();
		assertThat(eval.map(i->i+2).get(),equalTo(3));
		assertThat(count,equalTo(1));
	}
	@Test
	public void always(){
		assertThat(Eval.always(()->1).map(i->i+2).get(),equalTo(3));
	}
	@Test
	public void alwaysDoesNotCache(){
		count = 0;
		Eval<Integer> eval = Eval.always(()->{ 
			count++;
			return 1;
		});
		eval.map(i->i+2).get();
		eval.map(i->i+2).get();
		assertThat(eval.map(i->i+2).get(),equalTo(3));
		assertThat(count,equalTo(3));
	}
	@Test
	public void nowFlatMap(){
		assertThat(Eval.now(1).map(i->i+2).flatMap(i->Eval.later(()->i*3)).get(),equalTo(9));
	}
	@Test
	public void laterFlatMap(){
		assertThat(Eval.later(()->1).map(i->i+2)
						.flatMap(i->Eval.now(i*3)).get(),equalTo(9));
	}
	@Test
	public void alwaysFlatMap(){
		assertThat(Eval.always(()->1).map(i->i+2)
						.flatMap(i->Eval.now(i*3)).get(),equalTo(9));
	}
}

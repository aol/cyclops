package com.aol.cyclops2.control;

import cyclops.control.Eval;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class EvalTest {
    
    @Test
    public void laterExample(){
        
      Eval<Integer> delayed =   Eval.later(()->loadData())
                                    .map(this::process);
        
        
    }

    @Test
    public void testZip(){
        assertThat(Eval.now(10).zip(Eval.now(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zipP(Eval.now(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zipS(Stream.of(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zip(Seq.of(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zip(Seq.of(20)).get(),equalTo(Tuple.tuple(10,20)));
        assertThat(Eval.now(10).zipS(Stream.of(20)).get(),equalTo(Tuple.tuple(10,20)));
        assertThat(Eval.now(10).zip(Eval.now(20)).get(),equalTo(Tuple.tuple(10,20)));
    }
    
    private String loadData(){
        return "data";
    }
    private Integer process(String process){
        return 2;
    }
    
    @Test
    public void odd(){
        System.out.println(even(Eval.now(200000)).get());
    }
    public Eval<String> odd(Eval<Integer> n )  {
       
       return n.flatMap(x->even(Eval.now(x-1)));
    }
    public Eval<String> even(Eval<Integer> n )  {
        return n.flatMap(x->{
            return x<=0 ? Eval.now("done") : odd(Eval.now(x-1));
        });
     }
   
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
	public int addOne(Integer i){
		return i+1;
	}
	public int add(Integer i, Integer b){
		return i+b;
	}
	public String concat(String a, String b, String c){
		return a+b+c;
	}
	public String concat4(String a, String b, String c,String d){
		return a+b+c+d;
	}
	public String concat5(String a, String b, String c,String d,String e){
		return a+b+c+d+e;
	}




	
}

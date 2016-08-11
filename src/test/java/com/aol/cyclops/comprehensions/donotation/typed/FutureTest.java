package com.aol.cyclops.comprehensions.donotation.typed;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.junit.Test;

import com.aol.cyclops.control.For;

public class FutureTest {

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void forComprehension(){
		CompletableFuture<Integer> two = CompletableFuture.completedFuture(2);
		CompletableFuture<Integer> three = CompletableFuture.completedFuture(3);
		CompletableFuture<Integer> four = CompletableFuture.completedFuture(4);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	
		CompletableFuture<Integer> result =  For.future(two)
										.future(a->four)
										.future(a->b->three)
										.yield(v1->v2->v3 -> f2.apply(v1, v2)).unwrap();
		
		assertThat(result.join(),equalTo(CompletableFuture.completedFuture(8).join()));

	}
	@Test
	public void testForComphrensions4Null(){
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
		CompletableFuture<Integer> empty = null;
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> {
		  
		    return a * b;
		};

		CompletableFuture<Integer> result = For.future(one)
							                    .future(a->empty)
							                    .future(v1->v2->CompletableFuture.completedFuture(1))
							                    .future(a->b->c->CompletableFuture.completedFuture(1))
							                    .yield(v1->v2->v3->v4-> f2.apply(v1, v2)).unwrap();
		
		assertNull(result.join());

	}
	@Test
	public void testForComphrensions4(){
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
		CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		CompletableFuture<Integer> result =  For.future(one)
							.future(v1->empty)
							.future(v1->v2->CompletableFuture.completedFuture(1))
							.future(v1->v2->v3->CompletableFuture.completedFuture(1))
							.yield(v1->v2->v3->v4-> f2.apply(v1, v2)).unwrap();
		
		assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

	}
	@Test
    public void testForComphrensions5(){
        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

        CompletableFuture<Integer> result =  For.future(one)
                            .future(v1->empty)
                            .future(v1->v2->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
                            .yield(v1->v2->v3->v4-> v5->f2.apply(v1, v2)).unwrap();
        
        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

    }
	@Test
    public void testForComphrensions6(){
        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

        CompletableFuture<Integer> result =  For.future(one)
                            .future(v1->empty)
                            .future(v1->v2->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->v5->CompletableFuture.completedFuture(1))
                            .yield(v1->v2->v3->v4-> v5->v6->f2.apply(v1, v2)).unwrap();
        
        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

    }
	@Test
    public void testForComphrensions7(){
        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

        CompletableFuture<Integer> result =  For.future(one)
                            .future(v1->empty)
                            .future(v1->v2->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->v5->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->v5->v6->CompletableFuture.completedFuture(1))
                            .yield(v1->v2->v3->v4-> v5->v6->v7->f2.apply(v1, v2)).unwrap();
        
        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

    }
	@Test
    public void testForComphrensions8(){
        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

        CompletableFuture<Integer> result =  For.future(one)
                            .future(v1->empty)
                            .future(v1->v2->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->v5->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->v5->v6->CompletableFuture.completedFuture(1))
                            .future(v1->v2->v3->v4->v5->v6->v7->CompletableFuture.completedFuture(1))
                            .yield(v1->v2->v3->v4-> v5->v6->v7->v8->f2.apply(v1, v2)).unwrap();
        
        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

    }
	
	   @Test
	    @SuppressWarnings({"unchecked", "rawtypes"})
	    public void forComprehensionYield(){
	        CompletableFuture<Integer> two = CompletableFuture.completedFuture(2);
	        CompletableFuture<Integer> three = CompletableFuture.completedFuture(3);
	        CompletableFuture<Integer> four = CompletableFuture.completedFuture(4);
	        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	    
	        CompletableFuture<Integer> result =  For.future(two)
	                                        .future(a->four)
	                                        .future(a->b->three)
	                                        .yield3((v1,v2,v3)->f2.apply(v1, v2)).unwrap();
	        
	        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(8).join()));

	    }
	   
	    @Test
	    public void testForComphrensions4Yield(){
	        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
	        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
	        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	        CompletableFuture<Integer> result =  For.future(one)
	                            .future(v1->empty)
	                            .future(v1->v2->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
	                            .yield4((v1,v2,v3,v4)->f2.apply(v1, v2)).unwrap();
	        
	        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

	    }
	    @Test
	    public void testForComphrensions5Yield(){
	        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
	        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
	        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	        CompletableFuture<Integer> result =  For.future(one)
	                            .future(v1->empty)
	                            .future(v1->v2->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
	                            .yield5((v1,v2,v3,v4,v5)->f2.apply(v1, v2)).unwrap();
	        
	        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

	    }
	    @Test
	    public void testForComphrensions6Yield(){
	        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
	        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
	        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	        CompletableFuture<Integer> result =  For.future(one)
	                            .future(v1->empty)
	                            .future(v1->v2->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->v5->CompletableFuture.completedFuture(1))
	                            .yield6((v1,v2,v3,v4,v5,v6)->f2.apply(v1, v2)).unwrap();
	        
	        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

	    }
	    @Test
	    public void testForComphrensions7Yeild(){
	        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
	        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
	        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	        CompletableFuture<Integer> result =  For.future(one)
	                            .future(v1->empty)
	                            .future(v1->v2->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->v5->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->v5->v6->CompletableFuture.completedFuture(1))
	                            .yield7((v1,v2,v3,v4,v5,v6,v7)->f2.apply(v1, v2)).unwrap();
	        
	        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

	    }
	    @Test
	    public void testForComphrensions8Yield(){
	        CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
	        CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
	        BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	        CompletableFuture<Integer> result =  For.future(one)
	                            .future(v1->empty)
	                            .future(v1->v2->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->v5->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->v5->v6->CompletableFuture.completedFuture(1))
	                            .future(v1->v2->v3->v4->v5->v6->v7->CompletableFuture.completedFuture(1))
	                            .yield8((v1,v2,v3,v4,v5,v6,v7,v8)->f2.apply(v1, v2)).unwrap();
	        
	        assertThat(result.join(),equalTo(CompletableFuture.completedFuture(3).join()));

	    }
	@Test
	public void test1(){
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(1);
		CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		
		CompletableFuture<Integer> result = For.future(one)
						   .yield(v->f2.apply(v, 10)).unwrap();

		assertThat(result.join(),equalTo(CompletableFuture.completedFuture(10).join()));

	}
	@Test
	public void test2(){
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(3);
		CompletableFuture<Integer> empty = CompletableFuture.completedFuture(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
				
		
		CompletableFuture<Integer> result =  For.future(one)
							.future(v1-> { System.out.println(v1); return CompletableFuture.completedFuture(v1);})
							.filter(v1->v2->v1>2)
							.yield(v1->v2-> f2.apply(v1, v2)).unwrap();
		
		assertThat(result.join(),equalTo(CompletableFuture.completedFuture(9).join()));

	}
}

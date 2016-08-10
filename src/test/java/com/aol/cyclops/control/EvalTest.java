package com.aol.cyclops.control;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import com.aol.cyclops.types.ConvertableFunctor;
public class EvalTest {
    
    @Test
    public void laterExample(){
        
      Eval<Integer> delayed =   Eval.later(()->loadData())
                                    .map(this::process);
        
        
    }

    @Test
    public void testZip(){
        assertThat(Eval.now(10).zip(Eval.now(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zip((a,b)->a+b,Eval.now(20)).get(),equalTo(30));
        assertThat(Eval.now(10).zip(Stream.of(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zip(Seq.of(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Eval.now(10).zip(Seq.of(20)).get(),equalTo(Tuple.tuple(10,20)));
        assertThat(Eval.now(10).zip(Stream.of(20)).get(),equalTo(Tuple.tuple(10,20)));
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
	@Test
	public void apExample(){
		
	//	System.out.println(Maybe.Applicatives.applicative(this::addOne).ap(Eval.now(10)).ap(Eval.now(30)).get());
	//	ListX.ZippingApplicatives.applicative(this::addOne);
		//ApplicativeBuilder.<Integer>of(t->Eval.now(t)).applicative2(this::addOne);
		System.out.println(Maybe.of(10).applyFunctions().ap2(this::add).ap(Eval.now(20)).get());
		System.out.println(Maybe.of(10).applyFunctions().ap2(this::add).ap(Optional.of(20)).get());
		System.out.println(Eval.now(10).applyFunctions().ap2(this::add).ap(Eval.now(20)).get());
		
	//	Eval.now(10).ap2(Maybe.applicativeBuilder().applicative2B(this::addOne).ap(Eval.now(20));
	}
	
	
	@Test
	public void ap1Function(){
		assertThat(Maybe.of(10).applyFunctions().ap1(this::addOne).get(),equalTo(11));

	}
	@Test
	public void ap1NoneFunction(){
		assertFalse(Maybe.<Integer>none().applyFunctions().ap1(this::addOne).toOptional().isPresent());

	}
	@Test
	public void ap2Function(){
		assertThat(Maybe.of(10).applyFunctions().ap2(this::add).ap(Maybe.of(20)).get(),equalTo(30));
		assertThat(Maybe.of(10).applyFunctions().ap2(this::add).ap(Optional.of(20)).get(),equalTo(30));
		
		ConvertableFunctor<Integer> maybe = Maybe.of(10).applyFunctions().ap2(this::add).ap(Maybe.of(20));
		maybe.toMaybe();

	}
	@Test
	public void ap2OptionalEmptyFunction(){
		assertFalse(Maybe.of(1).applyFunctions().ap2(this::add).ap(Optional.empty()).toOptional().isPresent());

	}
	@Test
	public void ap3Function(){
		assertThat(Maybe.of("hello").applyFunctions().ap3(this::concat).ap(Optional.of("world")).ap(CompletableFuture.supplyAsync(()->"boo!")).get(),equalTo("helloworldboo!"));

	}
	@Test
	public void ap3OptionalEmptyFunction(){
		System.out.println("r="+Maybe.of("hello").applyFunctions().ap3(this::concat)
									.ap(Optional.empty())
									.ap(CompletableFuture.supplyAsync(()->"boo!")));
		
		assertFalse(Maybe.of("hello").applyFunctions().ap3(this::concat).ap(Optional.empty()).ap(CompletableFuture.supplyAsync(()->"boo!")).toOptional().isPresent());

	}
	@Test
	public void ap4Function(){
		
		assertThat(Maybe.of("hello").applyFunctions()
						.ap4(this::concat4)
						.ap(Optional.of("world"))
						.ap(CompletableFuture.supplyAsync(()->"boo!"))
						.ap(Eval.now("done?")).get(),equalTo("helloworldboo!done?"));

	}
	@Test
	public void ap4OptionalEmptyFunction(){
		assertFalse(Maybe.of("hello").applyFunctions()
				.ap4(this::concat4)
				.ap(Optional.of("world"))
				.ap(Maybe.none())
				.ap(Eval.now("done?")).toOptional().isPresent());

	}
	
	@Test
	public void ap5Function(){
		
		assertThat(Maybe.of("hello").applyFunctions()
						.ap5(this::concat5)
						.ap(Optional.of("world"))
						.ap(CompletableFuture.supplyAsync(()->"boo!"))
						.ap(Maybe.of("hello"))
						.ap(Eval.now("done?")).get(),equalTo("helloworldboo!hellodone?"));

	}
	@Test
	public void ap5OptionalEmptyFunction(){
		assertFalse(Maybe.of("hello").applyFunctions()
				.ap5(this::concat5)
				.ap(Optional.of("world"))
				.ap(CompletableFuture.supplyAsync(()->"boo!"))
				.ap(Maybe.of("hello"))
				.ap(Maybe.none()).toOptional().isPresent());

	}
	
}

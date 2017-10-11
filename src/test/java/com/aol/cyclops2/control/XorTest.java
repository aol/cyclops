package com.aol.cyclops2.control;

import cyclops.collectionx.immutable.LinkedListX;
import cyclops.companion.Monoids;
import cyclops.companion.Semigroups;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Either;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class XorTest {

	private String concat(String a,String b){
		return a+b;
	}
	static class Base{ }
    static class One extends Base{ }
    static class Two extends Base{}
    @Test
    public void visitAny(){
       
        Either<One,Two> test = Either.right(new Two());
        test.to(Either::applyAny).apply(b->b.toString());
        Either.right(10).to(Either::consumeAny).accept(System.out::println);
        Either.right(10).to(e-> Either.visitAny(System.out::println,e));
        Object value = Either.right(10).to(e-> Either.visitAny(e, x->x));
        assertThat(value,equalTo(10));
    }
	

	
	@Test
	public void test2() {
	    
	
	    
	    
	    
	   
	    
	    
		assertThat(Either.accumulateLeft(Monoids.stringConcat,ListX.of(Either.left("failed1"),
													Either.left("failed2"),
													Either.right("success"))
													).orElse(":"),equalTo("failed1failed2"));
		
	}






	@Test
	public void applicative(){
	    Either<String,String> fail1 =  Either.left("failed1");
	    Either<String,String> result = fail1.combine(Either.left("failed2"), Semigroups.stringConcat,(a, b)->a+b);
	    assertThat(result.leftOrElse(":"),equalTo("failed2failed1"));
	}
	@Test
    public void applicativeColleciton(){
        Either<String,String> fail1 =  Either.left("failed1");
        Either<LinkedListX<String>,String> result = fail1.list().combine(Either.left("failed2").list(), Semigroups.collectionXConcat(),(a, b)->a+b);
        assertThat(result.leftOrElse(LinkedListX.empty()),equalTo(LinkedListX.of("failed1","failed2")));
    }
	@Test
    public void applicativePStack(){
        Either<String,String> fail1 =  Either.left("failed1");
        Either<LinkedListX<String>,String> result = fail1.combineToList(Either.<String,String>left("failed2"),(a, b)->a+b);
        assertThat(result.leftOrElse(LinkedListX.empty()),equalTo(LinkedListX.of("failed1","failed2")));
    }
	

}

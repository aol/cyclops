package com.aol.cyclops.control;

import cyclops.Monoids;
import cyclops.Semigroups;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
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
       
        Xor<One,Two> test = Xor.primary(new Two());
        test.to(Xor::applyAny).apply(b->b.toString());
        Xor.primary(10).to(Xor::consumeAny).accept(System.out::println);
        Xor.primary(10).to(e->Xor.visitAny(System.out::println,e));
        Object value = Xor.primary(10).to(e->Xor.visitAny(e,x->x));
        assertThat(value,equalTo(10));
    }
	

	
	@Test
	public void test2() {
	    
	
	    
	    
	    
	   
	    
	    
		assertThat(Xor.accumulateSecondary(Monoids.stringConcat,ListX.of(Xor.secondary("failed1"),
													Xor.secondary("failed2"),
													Xor.primary("success"))
													).get(),equalTo("failed1failed2"));
		
	}






	@Test
	public void applicative(){
	    Xor<String,String> fail1 =  Xor.secondary("failed1");
	    Xor<String,String> result = fail1.combine(Xor.secondary("failed2"), Semigroups.stringConcat,(a,b)->a+b);
	    assertThat(result.secondaryGet(),equalTo("failed2failed1"));
	}
	@Test
    public void applicativeColleciton(){
        Xor<String,String> fail1 =  Xor.secondary("failed1");
        Xor<PStackX<String>,String> result = fail1.list().combine(Xor.secondary("failed2").list(), Semigroups.collectionXConcat(),(a,b)->a+b);
        assertThat(result.secondaryGet(),equalTo(PStackX.of("failed1","failed2")));
    }
	@Test
    public void applicativePStack(){
        Xor<String,String> fail1 =  Xor.secondary("failed1");
        Xor<PStackX<String>,String> result = fail1.combineToList(Xor.<String,String>secondary("failed2"),(a,b)->a+b);
        assertThat(result.secondaryGet(),equalTo(PStackX.of("failed1","failed2")));
    }
	

}

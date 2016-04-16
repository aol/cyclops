package com.aol.cyclops.control;

import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import com.aol.cyclops.Semigroups;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import static org.junit.Assert.assertThat;
public class XorTest {

	private String concat(String a,String b){
		return a+b;
	}
	@Test
	public void test() {
		Xor<String,String> fail1 = Xor.secondary("failed1");
		String s = fail1.swap().ap2(this::concat).ap(Xor.secondary("failed2").swap()).get();
		System.out.println(s);
	}
	
	@Test
	public void test2() {
	    
	
	    
	    
	    
	   
	    
	    
		assertThat(Xor.accumulateSecondary(ListX.of(Xor.secondary("failed1"),
													Xor.secondary("failed2"),
													Xor.primary("success")),
													Semigroups.stringConcat).get(),equalTo("failed1failed2"));
		
	}
	@Test
	public void accumulate(){
	    Xor<String,String> fail1 = Xor.secondary("failed1");
	    assertThat(fail1.swap()
	                    .ap(Semigroups.stringConcat)
	                    .ap(Xor.secondary("failed2").swap()).ap(Xor.<String,String>primary("success").swap())
	                                .convertable().get(),equalTo("failed1failed2"));
	}
	@Test
    public void accumulate2(){
        Xor<String,String> fail1 = Xor.secondary("failed1");
        assertThat(fail1.swap().ap((a,b)->a+b).ap(Xor.secondary("failed2").swap()).ap(Xor.<String,String>primary("success").swap())
                                    .convertable().get(),equalTo("failed1failed2"));
    }
	@Test
    public void accumulate3(){
        Xor<String,String> fail1 = Xor.secondary("failed1");
        assertThat(fail1.swap().ap((a,b)->a+b)
                                .ap(Xor.secondary("failed2").swap())
                                .ap(Xor.secondary("failed3").swap())
                                .ap(Xor.<String,String>primary("success").swap())
                                    .convertable().get(),equalTo("failed1failed2failed3"));
    }
	@Test
    public void accumulateNone(){
        Xor<String,String> fail1 = Xor.secondary("failed1");
        assertThat(fail1.swap().ap((a,b)->a+b).ap(Xor.secondary("failed2"))
                                    .convertable().get(),equalTo("failed1"));
    }

}

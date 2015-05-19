package com.aol.cyclops.lambda.tuple;

import static com.aol.cyclops.lambda.tuple.PowerTuples.tuple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

public class CompareTest {
	 @Test
	    public void equalsAndHash() {
	        Set<PTuple2<Integer, String>> set = new HashSet(){{
	        	add(tuple(1,2,3));
	        	add(tuple(1,2,3));
	        	add(tuple("hello",1,"world"));
	        }};

	       

	        assertThat(set.size(),equalTo(2));
	      
	    }

	    @Test
	    public void compare() {
	    	assertThat(tuple(1,2,3).compareTo(tuple(1,2,3)),equalTo(0));
	    }
	    @Test
	    public void compare2() {
	    	assertThat(tuple(1,2,4).compareTo(tuple(1,2,3)),equalTo(1));
	    }
	    @Test
	    public void compare3() {
	    	assertThat(tuple(1,2,1).compareTo(tuple(1,2,3)),equalTo(-1));
	    }
	    @Test
	    public void compare4() {
	    	assertThat(tuple(1,2,null).compareTo(tuple(1,2,3)),equalTo(-1));
	    }
	    
	    @Test
	    public void compare5() {
	    	assertThat(tuple(1,2,null).compareTo(tuple(1,2,null)),equalTo(0));
	    }
	    @Test
	    public void compare6() {
	    	assertThat(tuple(1,2,4).compareTo(tuple(1,2,null)),equalTo(1));
	    }
	    
}

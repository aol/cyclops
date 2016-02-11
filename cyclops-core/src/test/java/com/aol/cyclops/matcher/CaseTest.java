package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher2.Predicates.__;
import static com.aol.cyclops.matcher2.Predicates.values;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.matcher2.Case;
import com.aol.cyclops.matcher2.EmptyCase;
import com.aol.cyclops.matcher2.Predicates;
import com.aol.cyclops.objects.Decomposable;

import lombok.Value;

public class CaseTest {
	Case<Integer,Integer> case1;
	Case<Integer,Integer> offCase;
	@Before
	public void setup(){
		
		case1 =Case.of(input->true,input->input+10);
		offCase = Case.of(input->false,input->input+10);
	}
	

	@Value static final class Person implements Decomposable{ String name; int age; Address address; }
	@Value static final  class Address implements Decomposable { int number; String city; String country;}
	
	}

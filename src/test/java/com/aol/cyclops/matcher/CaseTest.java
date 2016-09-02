package com.aol.cyclops.matcher;

import org.junit.Before;

import com.aol.cyclops.internal.matcher2.Case;
import com.aol.cyclops.types.Decomposable;

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

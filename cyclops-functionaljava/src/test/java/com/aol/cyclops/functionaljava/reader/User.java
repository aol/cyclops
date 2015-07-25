package com.aol.cyclops.functionaljava.reader;

import lombok.Value;

@Value
public class User {
	int id;
	String name;
	String email;
	User supervisor;
	
}

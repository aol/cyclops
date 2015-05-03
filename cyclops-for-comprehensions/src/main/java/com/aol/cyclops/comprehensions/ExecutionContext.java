package com.aol.cyclops.comprehensions;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExecutionContext  {
	

		private final Map values;

		
		public Object getProperty(String name){
			return values.get(name);
		}

}

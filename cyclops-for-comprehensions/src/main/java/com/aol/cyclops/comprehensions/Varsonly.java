package com.aol.cyclops.comprehensions;

import lombok.Setter;


public  class Varsonly<T1,T2,T3,T4,T5> implements Initialisable<Varsonly>{
	@Setter
	private BaseComprehensionData data;
	
	public Varsonly init(BaseComprehensionData data){
		this.data = data;
		return this;
	}
	public <T> T $(String name){
		return data.$Internal(name);
	
	}
	public  T1 $1(){
		return data.$Internal("$1");
	
	}
	public  T2 $2(){
		return data.$Internal("$2");
	
	}
	public  T3 $3(){
		return data.$Internal("$3");
	
	}
	public T4  $4(){
		return data.$Internal("$4");
	
	}
	public T5  $5(){
		return data.$Internal("$5");
	
	}
}
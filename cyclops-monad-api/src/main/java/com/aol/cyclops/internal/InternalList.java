package com.aol.cyclops.internal;

import java.util.List;

import lombok.AllArgsConstructor;

import com.aol.cyclops.sequence.SequenceM;


@AllArgsConstructor
public class InternalList<T>  {
	
	private List<T> list;
	public SequenceM<T> sequence(){
		return SequenceM.fromList(list);
	}

	
	public String toString(){
		return list.toString();
	}
}


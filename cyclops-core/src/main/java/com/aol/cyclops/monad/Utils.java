package com.aol.cyclops.monad;

import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

	static <T> T firstOrNull(List<T> list){
		if(list==null || list.size()==0)
			return null;
		return list.get(0);
	}
}

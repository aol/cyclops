package com.aol.cyclops.react.stream.traits;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Val<T>{
	enum Pos { left,right};
	Pos pos;
	T val;
}

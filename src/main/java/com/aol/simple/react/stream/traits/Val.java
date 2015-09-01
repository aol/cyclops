package com.aol.simple.react.stream.traits;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Val<T>{
	enum Pos { left,right};
	Pos pos;
	T val;
}

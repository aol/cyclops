package com.aol.simple.react.stream.traits;

import lombok.AllArgsConstructor;

import com.aol.simple.react.stream.traits.FutureStream.Val.Pos;

@AllArgsConstructor
public class Val<T>{
	enum Pos { left,right};
	Pos pos;
	T val;
}

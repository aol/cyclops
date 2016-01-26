package com.aol.cyclops.javaslang.streams;


public interface PausableJavaslangHotStream<T> extends JavaslangHotStream<T>{
	void unpause();
	void pause();
}

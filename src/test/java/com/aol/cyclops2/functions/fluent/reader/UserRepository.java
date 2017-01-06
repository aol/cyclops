package com.aol.cyclops2.functions.fluent.reader;

public interface UserRepository {

	public User get(int id);
	public User find(String username);
}

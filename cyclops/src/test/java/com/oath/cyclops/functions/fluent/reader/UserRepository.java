package com.oath.cyclops.functions.fluent.reader;

public interface UserRepository {

	public User get(int id);
	public User find(String username);
}

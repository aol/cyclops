package com.aol.cyclops.functionaljava.reader;

public interface UserRepository {

	public User get(int id);
	public User find(String username);
}

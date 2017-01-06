package com.aol.cyclops2.functions.fluent.reader;

import java.util.Map;

import cyclops.function.Reader;



public class Application {

	UserRepositoryImpl repo = new UserRepositoryImpl();
	
	
	public Map<String,String> userInfo(String username) {
		return run(new UserInfo().userInfo(username));
	 }
	private Map<String,String>  run( Reader<UserRepository, Map<String,String> > reader){
			return reader.apply(repo);
	}
	static class UserRepositoryImpl implements UserRepository{
		int count = 0;
		User boss = new User(10,"boss","boss@user.com",null);
		@Override
		public User get(int id) {
			if(id==boss.getId())
				return boss;
			return new User(id,"user"+id,"user"+id+"@user.com",boss);
		}

		@Override
		public User find(String username) {
			return new User(count++,username,username+"@user.com",boss);
		}
		
	}
}

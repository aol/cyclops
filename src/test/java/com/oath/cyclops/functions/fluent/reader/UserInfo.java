package com.oath.cyclops.functions.fluent.reader;


import java.util.HashMap;
import java.util.Map;

import cyclops.control.Reader;



public class UserInfo implements Users {

	public Reader<UserRepository,Map<String,String>> userInfo(String username) {

		findUser(username).forEach2(user ->getUser(user.getSupervisor().getId()),
				   (user,boss) -> "user:"+username+" boss is "+boss.getName());


		return findUser(username).forEach2(user -> getUser(user.getSupervisor().getId()),
				                           (user,boss) -> buildMap(user,boss));


	}

	private Map<String,String>  buildMap(User user, User boss) {
		return new HashMap<String,String> (){{
				put("fullname",user.getName());
				put("email",user.getEmail());
				put("boss",boss.getName());

		}};
	}
}

package com.aol.cyclops.functions.fluent.reader;


import java.util.HashMap;
import java.util.Map;

import com.aol.cyclops.control.Do;
import com.aol.cyclops.control.Reader;



public class UserInfo implements Users {

	public Reader<UserRepository,Map<String,String> > userInfo(String username) {
		
		Do.add(findUser(username).anyM())
		 		.withAnyM(user ->getUser(user.getSupervisor().getId()).anyM())
		 		.yield(user -> boss -> "user:"+username+" boss is "+boss.getName());
		
		
		return Do.add(findUser(username).anyM())
				 .withAnyM(user -> getUser(user.getSupervisor().getId()).anyM())
				 .yield(user -> boss -> buildMap(user,boss)).unwrap();
		
		
	}

	private Map<String,String>  buildMap(User user, User boss) {
		return new HashMap<String,String> (){{
				put("fullname",user.getName());
				put("email",user.getEmail());
				put("boss",boss.getName());
				
		}};
	}
}

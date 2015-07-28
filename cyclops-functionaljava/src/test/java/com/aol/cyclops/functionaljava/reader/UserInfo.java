package com.aol.cyclops.functionaljava.reader;

import static com.aol.cyclops.functionaljava.FJ.anyM;

import java.util.HashMap;
import java.util.Map;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.functionaljava.FJ;
import com.aol.cyclops.lambda.monads.AnyM;

import fj.data.Reader;

public class UserInfo implements Users {

	public Reader<UserRepository,Map<String,String> > userInfo(String username) {
		
		Do.add(anyM(findUser(username)))
		 		.withAnyM(user ->anyM(getUser(user.getSupervisor().getId())))
		 		.yield(user -> boss -> "user:"+username+" boss is "+boss.getName());
		
		
		return Do.add(anyM(findUser(username)))
				 .withAnyM(user -> anyM(getUser(user.getSupervisor().getId())))
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

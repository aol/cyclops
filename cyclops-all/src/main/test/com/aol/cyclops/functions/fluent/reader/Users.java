package com.aol.cyclops.functions.fluent.reader;

import com.aol.cyclops.functions.fluent.FluentFunctions;
import com.aol.cyclops.monad.Reader;

public interface Users {


	 default  Reader<UserRepository,User> getUser(Integer id){
	    return FluentFunctions.of( userRepository -> userRepository.get(id));
	 }

	 default Reader<UserRepository,User> findUser(String username) {
		 return FluentFunctions.of(userRepository ->  userRepository.find(username));
	 }
	   
	  
	 
}

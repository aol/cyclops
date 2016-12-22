package com.aol.cyclops.functions.fluent.reader;

import cyclops.function.FluentFunctions;
import com.aol.cyclops.control.Reader;

public interface Users {


	 default  Reader<UserRepository,User> getUser(Integer id){
	    return FluentFunctions.of( userRepository -> userRepository.get(id));
	 }

	 default Reader<UserRepository,User> findUser(String username) {
		 return FluentFunctions.of(userRepository ->  userRepository.find(username));
	 }
	   
	  
	 
}

package com.aol.cyclops.functionaljava.reader;

import fj.data.Reader;

public interface Users {


	 default  Reader<UserRepository,User> getUser(Integer id){
	    return Reader.unit( userRepository -> userRepository.get(id));
	 }

	 default Reader<UserRepository,User> findUser(String username) {
		 return Reader.unit(userRepository ->  userRepository.find(username));
	 }
	   
	  
	 
}

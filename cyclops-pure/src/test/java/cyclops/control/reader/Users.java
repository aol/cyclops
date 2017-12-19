package cyclops.control.reader;

import cyclops.function.FluentFunctions;
import cyclops.control.Reader;

public interface Users {


	 default  Reader<UserRepository,User> getUser(Integer id){
	    return Reader.of( userRepository -> userRepository.get(id));
	 }

	 default Reader<UserRepository,User> findUser(String username) {
		 return Reader.of(userRepository ->  userRepository.find(username));
	 }



}

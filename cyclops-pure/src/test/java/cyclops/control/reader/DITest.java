package cyclops.control.reader;

import cyclops.function.FluentFunctions;
import cyclops.control.Reader;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

//Reader Monad for Dependency Injection converted to Java8
//http://blog.originate.com/blog/2013/10/21/reader-monad-for-dependency-injection/
public class DITest {

	@SuppressWarnings("serial")
	Map<String,String> map = new HashMap<String,String> (){{
									put("fullname","bob");
									put("email","bob@user.com");
									put("boss","boss");
								}};



	@Test
	public void test(){
		assertThat(new Application().userInfo("bob"), equalTo(map));
	}

	@Test
	public void depth(){
	    assertThat(depth8("bob").apply(new Application.UserRepositoryImpl()), equalTo(8));
	}
	@Test
    public void forComp(){
	 Reader<UserRepository,Integer> res = depth1("bob")
			 								.forEach4(a->depth2("bob"),
			 									      (a,b)->depth3("bob"),
													  (a,b,c)->depth3("bob"),
													  (a,b,c,d)->(a+b+c+d));
        assertThat(res.apply(new Application.UserRepositoryImpl()), equalTo(9));
    }
	private Reader<UserRepository,Integer> depth8(String name){
        return repo->depth7(name).apply(repo)+1;
    }
	private Reader<UserRepository,Integer> depth7(String name){
        return repo->depth6(name).apply(repo)+1;
    }
	private Reader<UserRepository,Integer> depth6(String name){
        return repo->depth5(name).apply(repo)+1;
    }
	private Reader<UserRepository,Integer> depth5(String name){
        return repo->depth4(name).apply(repo)+1;
    }
	private Reader<UserRepository,Integer> depth4(String name){
        return repo->depth3(name).apply(repo)+1;
    }
	private Reader<UserRepository,Integer> depth3(String name){
        return repo->depth2(name).apply(repo)+1;
    }
	private Reader<UserRepository,Integer> depth2(String name){
	    return repo->depth1(name).apply(repo)+1;
	}
	private Reader<UserRepository,Integer> depth1(String name){
	    return Reader.of(repo-> 1);
	}
}

/** NOTES : See UserInfo class where current main compromise occurs.
UserRepository fold info temporarily lost & locally restored. This could be solved
by a custom For Comprehension builder for FJ. Would also make construction of For Comphrensions
a little less clunky**/

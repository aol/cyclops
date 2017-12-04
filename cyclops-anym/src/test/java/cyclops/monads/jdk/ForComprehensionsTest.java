package cyclops.monads.jdk;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Test;

import cyclops.monads.AnyM;

public class ForComprehensionsTest {

	@Test
	public void forEach2(){

	assertThat(AnyM.fromArray(1,2,3)
		         .forEach2(a->AnyM.fromIntStream(IntStream.range(0,10)),
		        		  (a,b)-> a+b)
		         .toList(),equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8,
		        		 9, 10, 11, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}

	@Test
	public void forEach2Filter(){



		assertThat(AnyM.fromArray(1,2,3)
		         	.forEach2(a->AnyM.fromIntStream(IntStream.range(0,10)),
		        		  (a,b)->a>2 && b<8,
		        		  (a,b)-> a+b)
		         .toList(),equalTo(Arrays.asList(3,4,5,6,7,8,9,10)));
	}
	@Test
	public void forEach3(){


		assertThat(AnyM.fromArray(1,2,3)
		         .forEach3(a->AnyM.fromIntStream(IntStream.range(6,9)),
		        		   (a,b)->AnyM.fromIntStream(IntStream.range(100,105)),
		        		   (a,b,c)-> a+b+c)
		         .toList(),equalTo(Arrays.asList(107, 108, 109, 110, 111, 108, 109, 110, 111, 112, 109, 110, 111,
		        		 112, 113, 108, 109, 110, 111, 112, 109, 110, 111, 112, 113, 110, 111, 112, 113, 114, 109, 110,
		        		 111, 112, 113, 110, 111, 112, 113, 114, 111, 112, 113, 114, 115)));

	}
	@Test
	public void forEach3Filter(){


		assertThat(AnyM.fromArray(2,3)
		         .forEach3(a->AnyM.fromIntStream(IntStream.range(6,9)),
		        		   (a,b)->AnyM.fromIntStream(IntStream.range(100,105)),
		        		    (a,b,c) -> a==3,
		        		    (a,b,c)-> a+b+c)
		         .toList(),equalTo(Arrays.asList(109, 110, 111, 112, 113, 110, 111, 112, 113, 114, 111, 112, 113, 114, 115)));

	}
}

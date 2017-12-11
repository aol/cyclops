package cyclops.futurestream.react.lazy;

import com.oath.cyclops.async.LazyReact;
import org.junit.Test;

public class ForEachParallelTest {

	@Test
	 public void testOnEmptyThrows(){
		new LazyReact().of(1,2,3,4)
						.peek(i-> System.out.println("A"+Thread.currentThread().getId()))
						.peekSync(i->sleep(i*100)).forEach(i-> System.out.println(Thread.currentThread().getId()));


	  }

	private Object sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
}

package cyclops.futurestream.react.async.vertx;

import com.oath.cyclops.async.LazyReact;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class Starter extends AbstractVerticle {

	  @Override
	  public void start() throws Exception {
	    super.start();
	    //this can't work as the tasks from cyclops2-react are being passed back into the
	    //event loop that is executing this task, LazyReact needs to pass tasks to a different
	    //event loop
	    LazyReact react = new LazyReact(new VertxExecutor(getVertx()));

	    int number = react.of(1, 2, 3).map(i -> i + 1).reduce((a,b) -> a + b).orElse(Integer.MIN_VALUE);
	    System.out.println("sum = " + number); // 2 + 3 + 4 = 9

	  }

	  public static void main(String[] args) {
	    Vertx.vertx().deployVerticle(Starter.class.getCanonicalName());
	  }
	}

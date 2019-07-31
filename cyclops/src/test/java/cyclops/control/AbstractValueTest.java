package cyclops.control;

import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.util.box.Mutable;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractValueTest {

  protected  abstract  <T> MonadicValue<T> of(T value);
  protected  abstract  <T> MonadicValue<T> empty();
  protected  abstract  <T> MonadicValue<T> fromPublisher(Publisher<T> p);
  @Test
  public void fromPublisher(){
      assertTrue(fromPublisher(of(1)).isPresent());
      assertThat(fromPublisher(of(1)).fold(i->i,()->-10),equalTo(1));
  }

  @Test
  public void fromPublisherEmpty(){
    assertFalse(fromPublisher(empty()).isPresent());
    assertThat(fromPublisher(empty()).fold(i->i,()->-10),equalTo(-10));
  }
  @Test
  public void testForEach() {
    Mutable<Integer> capture = Mutable.of(null);
    this.<Integer>empty().forEach(c -> capture.set(c));
    assertNull(capture.get());
    of(10).forEach(c -> capture.set(c));
    Assert.assertThat(capture.get(), equalTo(10));
  }



}

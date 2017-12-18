package cyclops.data.tuple;

import cyclops.control.Identity;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class Tuple1Test {
  Tuple1<Integer> t1;
  @Before
  public void setUp() throws Exception {
    t1 = Tuple.tuple(10);
  }
  @Test
  public void toIdentity() throws Exception {
    assertThat(Identity.fromTuple(t1).get(),equalTo(10));
  }
}

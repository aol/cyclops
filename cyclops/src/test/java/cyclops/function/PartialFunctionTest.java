package cyclops.function;


import cyclops.control.Option;
import org.junit.Test;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartialFunctionTest {
  PartialFunction<String,Integer> pf = in ->{
    if("hello".equals(in))
      return Option.some(10);
    return Option.none();
  };

  @Test
  public void partial(){
    assertThat(pf.apply("hello"),equalTo(Option.some(10)));
    assertThat(pf.apply("hello",-1),equalTo(10));
    assertThat(pf.apply("hello",()->-1),equalTo(10));
    assertThat(pf.apply("bob"),equalTo(Option.none()));
    assertThat(pf.apply("bob",100),equalTo(100));
    assertThat(pf.apply("bob",()->100),equalTo(100));
  }

  @Test
  public void asFunction(){
    Function<? super String, ? extends Option<Integer>> fn = pf.asFunction();
    assertThat(fn.apply("hello"),equalTo(pf.apply("hello")));
    assertThat(fn.apply("bob"),equalTo(pf.apply("bob")));
  }

  @Test
  public void map(){
    assertThat(pf.mapFn(i->i+1).apply("hello",-1),equalTo(11));
  }
  @Test
  public void flatMap(){
    assertThat(pf.flatMapFn(i->in->i+1).apply("hello",-1),equalTo(11));
  }
}

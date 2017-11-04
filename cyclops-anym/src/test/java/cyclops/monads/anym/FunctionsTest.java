package cyclops.monads.anym;

import cyclops.function.FluentFunctions;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class FunctionsTest {
  @Before
  public void setup(){
    this.called =0;
  }
  int called;
  public int getOne(){
    called++;
    return 1;
  }

  public int addOne(Integer i ){
    called++;
    return i+1;
  }
  public int add(Integer a,Integer b ){
    called++;
    return a+b;
  }
  public int add(Integer a, Integer b, Integer c) {
    called++;
    return a + b + c;
  }
  @Test
  public void testLiftMSupplier(){

    AnyM<Witness.stream,Integer> result = FluentFunctions.of(this::getOne)
      .<Witness.stream>liftF(Witness.stream.INSTANCE)
      .get();

    assertThat(result.stream().toList(),
      equalTo(Arrays.asList(1)));
  }
  @Test
  public void testLiftMFn1(){

    AnyM<Witness.stream,Integer> result = FluentFunctions.of(this::addOne)
      .<Witness.stream>liftF()
      .apply(AnyM.streamOf(1,2,3,4));

    assertThat(result.stream().toList(),
      equalTo(Arrays.asList(2,3,4,5)));
  }
  @Test
  public void testLiftMFn2(){

    AnyM<Witness.list,Integer> result = FluentFunctions.of(this::add)
      .<Witness.list>liftF()
      .apply(AnyM.listOf(1,2,3,4),AnyM.listOf(1));

    assertThat(result.stream().toList(),
      equalTo(Arrays.asList(2,3,4,5)));
  }
  @Test
  public void testLiftMFn3() {

    AnyM<Witness.list,Integer> result = FluentFunctions.of(this::add)
      .<Witness.list>liftF3()
      .apply(AnyM.listOf(1, 2, 3, 4), AnyM.listOf(1), AnyM.listOf(10));

    assertThat(result.stream()
        .toList(),
      equalTo(Arrays.asList(12, 13, 14, 15)));
  }
}

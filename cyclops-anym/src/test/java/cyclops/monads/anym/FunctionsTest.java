package cyclops.monads.anym;

import cyclops.function.Curry;
import cyclops.function.CurryVariance;
import cyclops.function.FluentFunctions;
import cyclops.function.Uncurry;
import cyclops.monads.AnyM;
import cyclops.monads.function.AnyMFunction0;
import cyclops.monads.function.AnyMFunction1;
import cyclops.monads.function.AnyMFunction2;
import cyclops.monads.function.AnyMFunction3;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;

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
  public int add3(Integer a, Integer b, Integer c) {
    called++;
    return a + b + c;
  }
  @Test
  public void testLiftMSupplier(){

    AnyM<stream,Integer> result = FluentFunctions.of(this::getOne)
      .toType(AnyMFunction0::<stream,Integer>liftF)
      .apply(stream.INSTANCE)
      .get();

    assertThat(result.stream().toList(),
      equalTo(Arrays.asList(1)));
  }
  @Test
  public void testLiftMFn1(){

    AnyM<stream,Integer> result = FluentFunctions.of(this::addOne)
      .toType(AnyMFunction1::<stream,Integer,Integer>liftF)
      .apply(AnyM.streamOf(1,2,3,4));

    assertThat(result.stream().toList(),
      equalTo(Arrays.asList(2,3,4,5)));
  }
  @Test
  public void testLiftMFn2(){

    AnyM<list,Integer> result = FluentFunctions.of(this::add)
      .to(AnyMFunction2::<list,Integer,Integer,Integer>liftF)
      .apply(AnyM.listOf(1,2,3,4),AnyM.listOf(1));

    assertThat(result.stream().toList(),
      equalTo(Arrays.asList(2,3,4,5)));
  }
  @Test
  public void testLiftMFn3() {

    AnyM<list,Integer> result = FluentFunctions.of(this::add3)
      .toType3(AnyMFunction3::<list,Integer,Integer,Integer,Integer>liftF)
      .apply(AnyM.listOf(1, 2, 3, 4), AnyM.listOf(1), AnyM.listOf(10));

    assertThat(result.stream()
        .toList(),
      equalTo(Arrays.asList(12, 13, 14, 15)));
  }
}

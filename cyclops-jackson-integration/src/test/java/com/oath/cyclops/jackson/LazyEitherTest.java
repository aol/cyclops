package com.oath.cyclops.jackson;

import cyclops.control.Either;
import cyclops.control.LazyEither;
import cyclops.control.Option;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LazyEitherTest {

   @Test
  public void left(){
      assertThat(JacksonUtil.serializeToJson(LazyEither.left(10)),equalTo("{\"left\":10}"));

   }

  @Test
  public void right(){
    assertThat(JacksonUtil.serializeToJson(LazyEither.right(10)),equalTo("{\"right\":10}"));

  }
    @Test
    public void infiniteEither() {
        Either<Exception,String> result = Option.<String>none().toTry(new Exception("asdf")).asEither();
        for (int i = 0; i < 10; i++) {
            result = result.flatMapLeftToRight(e -> Either.left(new Exception("asdf")));
        }
        System.out.println(result.toString());
    }
    @Test
    public void infiniteEitherRight() {
        Either<Exception,String> result = Option.<String>none().toTry(new Exception("asdf")).asEither();
        //for (int i = 0; i < 10; i++)
        {
            result = result.flatMapLeft(e -> Either.right("asdf"));
        }
        System.out.println(result.toString());
    }
    @Test
    public void infiniteEitherFlatMap() {
        Either<Exception,String> result = Option.<String>none().toTry(new Exception("asdf")).asEither();
        for (int i = 0; i < 10; i++) {
            result = result.flatMap(e -> Either.left(new Exception("asdf")));
        }
        System.out.println(result.toString());
    }
  @Test
  public void roundTripLeft(){

    String json  =JacksonUtil.serializeToJson(LazyEither.left(10));
    System.out.println("Json " +  json);
    LazyEither<Integer,String> des = JacksonUtil.convertFromJson(json,LazyEither.class);

    assertThat(des,equalTo(LazyEither.left(10)));
  }

  @Test
  public void roundTripRight(){

    String json  =JacksonUtil.serializeToJson(LazyEither.right(10));
    System.out.println("Json " +  json);
    LazyEither<String,Integer> des = JacksonUtil.convertFromJson(json,LazyEither.class);

    assertThat(des,equalTo(LazyEither.right(10)));
  }

}

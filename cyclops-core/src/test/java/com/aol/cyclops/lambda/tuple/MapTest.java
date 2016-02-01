package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MapTest {
	@Test
	public void tuple1_1(){
		
		assertThat(PTuple1.of("hello").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple2_1(){
		assertThat(PTuple2.of("hello", "world").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple2_2(){
		assertThat(PTuple2.of("hello", "world").map2(in->in+"2")._2(),equalTo("world2"));
	}
	@Test
	public void tuple3_1(){
		assertThat(PTuple3.of("hello", "world","woo!").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple3_2(){
		assertThat(PTuple3.of("hello", "world","woo!").map2(in->in+"2")._2(),equalTo("world2"));
	}
	@Test
	public void tuple3_3(){
		assertThat(PTuple3.of("hello", "world","woo!").map3(in->in+"2")._3(),equalTo("woo!2"));
	}
	@Test
	public void tuple4_1(){
		assertThat(PTuple4.of("hello", "world","woo!","hoo!").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple4_2(){
		assertThat(PTuple4.of("hello", "world","woo!","hoo!").map2(in->in+"2")._2(),equalTo("world2"));
	}
	@Test
	public void tuple4_3(){
		assertThat(PTuple4.of("hello", "world","woo!","hoo!").map3(in->in+"2")._3(),equalTo("woo!2"));
	}
	@Test
	public void tuple4_4(){
		assertThat(PTuple4.of("hello", "world","woo!","hoo!").map4(in->in+"2")._4(),equalTo("hoo!2"));
	}
	@Test
	public void tuple5_1(){
		assertThat(PTuple5.of("hello", "world","woo!","hoo!","5").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple5_2(){
		assertThat(PTuple5.of("hello", "world","woo!","hoo!","5").map2(in->in+"2")._2(),equalTo("world2"));
	}
	@Test
	public void tuple5_3(){
		assertThat(PTuple5.of("hello", "world","woo!","hoo!","5").map3(in->in+"2")._3(),equalTo("woo!2"));
	}
	@Test
	public void tuple5_4(){
		assertThat(PTuple5.of("hello", "world","woo!","hoo!","5").map4(in->in+"2")._4(),equalTo("hoo!2"));
	}
	@Test
	public void tuple5_5(){
		assertThat(PTuple5.of("hello", "world","woo!","hoo!","5").map5(in->in+"2")._5(),equalTo("52"));
	}
	@Test
	public void tuple6_1(){
		assertThat(PTuple6.of("hello", "world","woo!","hoo!","5","6").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple6_2(){
		assertThat(PTuple6.of("hello", "world","woo!","hoo!","5","6").map2(in->in+"2")._2(),equalTo("world2"));
	}
	@Test
	public void tuple6_3(){
		assertThat(PTuple6.of("hello", "world","woo!","hoo!","5","6").map3(in->in+"2")._3(),equalTo("woo!2"));
	}
	@Test
	public void tuple6_4(){
		assertThat(PTuple6.of("hello", "world","woo!","hoo!","5","6").map4(in->in+"2")._4(),equalTo("hoo!2"));
	}
	@Test
	public void tuple6_5(){
		assertThat(PTuple6.of("hello", "world","woo!","hoo!","5","6").map5(in->in+"2")._5(),equalTo("52"));
	}
	@Test
	public void tuple6_6(){
		assertThat(PTuple6.of("hello", "world","woo!","hoo!","5","6").map6(in->in+"2")._6(),equalTo("62"));
	}
	@Test
	public void tuple7_1(){
		assertThat(PTuple7.of("hello", "world","woo!","hoo!","5","6","7").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple7_2(){
		assertThat(PTuple7.of("hello", "world","woo!","hoo!","5","6","7").map2(in->in+"2")._2(),equalTo("world2"));
	}
	@Test
	public void tuple7_3(){
		assertThat(PTuple7.of("hello", "world","woo!","hoo!","5","6","7").map3(in->in+"2")._3(),equalTo("woo!2"));
	}
	@Test
	public void tuple7_4(){
		assertThat(PTuple7.of("hello", "world","woo!","hoo!","5","6","7").map4(in->in+"2")._4(),equalTo("hoo!2"));
	}
	@Test
	public void tuple7_5(){
		assertThat(PTuple7.of("hello", "world","woo!","hoo!","5","6","7").map5(in->in+"2")._5(),equalTo("52"));
	}
	@Test
	public void tuple7_6(){
		assertThat(PTuple7.of("hello", "world","woo!","hoo!","5","6","7").map6(in->in+"2")._6(),equalTo("62"));
	}
	@Test
	public void tuple7_7(){
		assertThat(PTuple7.of("hello", "world","woo!","hoo!","5","6","7").map7(in->in+"2")._7(),equalTo("72"));
	}
	@Test
	public void tuple8_1(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map1(in->in+"2")._1(),equalTo("hello2"));
	}
	@Test
	public void tuple8_2(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map2(in->in+"2")._2(),equalTo("world2"));
	}
	@Test
	public void tuple8_3(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map3(in->in+"2")._3(),equalTo("woo!2"));
	}
	@Test
	public void tuple8_4(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map4(in->in+"2")._4(),equalTo("hoo!2"));
	}
	@Test
	public void tuple8_5(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map5(in->in+"2")._5(),equalTo("52"));
	}
	@Test
	public void tuple8_6(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map6(in->in+"2")._6(),equalTo("62"));
	}
	@Test
	public void tuple8_7(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map7(in->in+"2")._7(),equalTo("72"));
	}
	@Test
	public void tuple8_8(){
		assertThat(PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").map8(in->in+"2")._8(),equalTo("82"));
	}
	 
}

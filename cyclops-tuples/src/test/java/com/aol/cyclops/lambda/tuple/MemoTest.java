package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import lombok.val;

import org.junit.Test;

public class MemoTest {
	int called =0;
	@Test
	public void tuple1_1(){
		val tuple = PTuple1.of("hello").lazyMap1(in->in+"2"+called++).memo();
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple2_1(){
		val tuple = PTuple2.of("hello", "world").lazyMap1(in->in+"2"+called++);
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple2_2(){
		val tuple = PTuple2.of("hello", "world").lazyMap2(in->in+"2"+called++);
		tuple._2();
		assertThat(tuple._2(),equalTo("world21"));
	}
	@Test
	public void tuple3_1(){
		val tuple = PTuple3.of("hello", "world","woo!").lazyMap1(in->in+"2"+called++);
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple3_2(){
		val tuple = PTuple3.of("hello", "world","woo!").lazyMap2(in->in+"2"+called++);
		tuple._2();
		assertThat(tuple._2(),equalTo("world21"));
	}
	@Test
	public void tuple3_3(){
		val tuple = PTuple3.of("hello", "world","woo!").lazyMap3(in->in+"2"+called++);
		
		tuple._3();
		
		assertThat(tuple._3(),equalTo("woo!21"));
	}
	@Test
	public void tuple4_1(){
		val tuple = PTuple4.of("hello", "world","woo!","hoo!").lazyMap1(in->in+"2"+called++);
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple4_2(){
		val tuple = PTuple4.of("hello", "world","woo!","hoo!").lazyMap2(in->in+"2"+called++);
		tuple._2();
		assertThat(tuple._2(),equalTo("world21"));
	}
	@Test
	public void tuple4_3(){
		val tuple = PTuple4.of("hello", "world","woo!","hoo!").lazyMap3(in->in+"2"+called++);
		tuple._3();
		assertThat(tuple._3(),equalTo("woo!21"));
	}
	@Test
	public void tuple4_4(){
		val tuple = PTuple4.of("hello", "world","woo!","hoo!").lazyMap4(in->in+"2"+called++);
		tuple._4();
		assertThat(tuple._4(),equalTo("hoo!21"));
	}
	@Test
	public void tuple5_1(){
		val tuple = PTuple5.of("hello", "world","woo!","hoo!","5").lazyMap1(in->in+"2"+called++);
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple5_2(){
		val tuple = PTuple5.of("hello", "world","woo!","hoo!","5").lazyMap2(in->in+"2"+called++);
		tuple._2();
		assertThat(tuple._2(),equalTo("world21"));
	}
	@Test
	public void tuple5_3(){
		val tuple = PTuple5.of("hello", "world","woo!","hoo!","5").lazyMap3(in->in+"2"+called++);
		tuple._3();
		assertThat(tuple._3(),equalTo("woo!21"));
	}
	@Test
	public void tuple5_4(){
		val tuple = PTuple5.of("hello", "world","woo!","hoo!","5").lazyMap4(in->in+"2"+called++);
		tuple._4();
		assertThat(tuple._4(),equalTo("hoo!21"));
	}
	@Test
	public void tuple5_5(){
		val tuple = PTuple5.of("hello", "world","woo!","hoo!","5").lazyMap5(in->in+"2"+called++);
		tuple._5();
		assertThat(tuple._5(),equalTo("521"));
	}
	@Test
	public void tuple6_1(){
		val tuple = PTuple6.of("hello", "world","woo!","hoo!","5","6").lazyMap1(in->in+"2"+called++);
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple6_2(){
		val tuple = PTuple6.of("hello", "world","woo!","hoo!","5","6").lazyMap2(in->in+"2"+called++);
		tuple._2();
		assertThat(tuple._2(),equalTo("world21"));
	}
	@Test
	public void tuple6_3(){
		val tuple = PTuple6.of("hello", "world","woo!","hoo!","5","6").lazyMap3(in->in+"2"+called++);
		tuple._3();
		assertThat(tuple._3(),equalTo("woo!21"));
	}
	@Test
	public void tuple6_4(){
		val tuple = PTuple6.of("hello", "world","woo!","hoo!","5","6").lazyMap4(in->in+"2"+called++);
		tuple._4();
		assertThat(tuple._4(),equalTo("hoo!21"));
	}
	@Test
	public void tuple6_5(){
		val tuple = PTuple6.of("hello", "world","woo!","hoo!","5","6").lazyMap5(in->in+"2"+called++);
		tuple._5();
		assertThat(tuple._5(),equalTo("521"));
	}
	@Test
	public void tuple6_6(){
		val tuple = PTuple6.of("hello", "world","woo!","hoo!","5","6").lazyMap6(in->in+"2"+called++);
		tuple._6();
		assertThat(tuple._6(),equalTo("621"));
	}
	@Test
	public void tuple7_1(){
		val tuple = PTuple7.of("hello", "world","woo!","hoo!","5","6","7").lazyMap1(in->in+"2"+called++);
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple7_2(){
		val tuple = PTuple7.of("hello", "world","woo!","hoo!","5","6","7").lazyMap2(in->in+"2"+called++);
		tuple._2();
		assertThat(tuple._2(),equalTo("world21"));
	}
	@Test
	public void tuple7_3(){
		val tuple = PTuple7.of("hello", "world","woo!","hoo!","5","6","7").lazyMap3(in->in+"2"+called++);
		tuple._3();
		assertThat(tuple._3(),equalTo("woo!21"));
	}
	@Test
	public void tuple7_4(){
		val tuple = PTuple7.of("hello", "world","woo!","hoo!","5","6","7").lazyMap4(in->in+"2"+called++);
		tuple._4();
		assertThat(tuple._4(),equalTo("hoo!21"));
	}
	@Test
	public void tuple7_5(){
		val tuple = PTuple7.of("hello", "world","woo!","hoo!","5","6","7").lazyMap5(in->in+"2"+called++);
		tuple._5();
		assertThat(tuple._5(),equalTo("521"));
	}
	@Test
	public void tuple7_6(){
		val tuple = PTuple7.of("hello", "world","woo!","hoo!","5","6","7").lazyMap6(in->in+"2"+called++);
		tuple._6();
		assertThat(tuple._6(),equalTo("621"));
	}
	@Test
	public void tuple7_7(){
		val tuple = PTuple7.of("hello", "world","woo!","hoo!","5","6","7").lazyMap7(in->in+"2"+called++);
		tuple._7();
		assertThat(tuple._7(),equalTo("721"));
	}
	@Test
	public void tuple8_1(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap1(in->in+"2"+called++);
		tuple._1();
		assertThat(tuple._1(),equalTo("hello21"));
	}
	@Test
	public void tuple8_2(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap2(in->in+"2"+called++);
		tuple._2();
		assertThat(tuple._2(),equalTo("world21"));
	}
	@Test
	public void tuple8_3(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap3(in->in+"2"+called++);
		tuple._3();
		assertThat(tuple._3(),equalTo("woo!21"));
	}
	@Test
	public void tuple8_4(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap4(in->in+"2"+called++);
		tuple._4();
		assertThat(tuple._4(),equalTo("hoo!21"));
	}
	@Test
	public void tuple8_5(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap5(in->in+"2"+called++);
		tuple._5();
		assertThat(tuple._5(),equalTo("521"));
	}
	@Test
	public void tuple8_6(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap6(in->in+"2"+called++);
		tuple._6();
		assertThat(tuple._6(),equalTo("621"));
	}
	@Test
	public void tuple8_7(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap7(in->in+"2"+called++);
		tuple._7();
		assertThat(tuple._7(),equalTo("721"));
	}
	@Test
	public void tuple8_8(){
		val tuple = PTuple8.of("hello", "world","woo!","hoo!","5","6","7","8").lazyMap8(in->in+"2"+called++);
		tuple._8();
		assertThat(tuple._8(),equalTo("821"));
	}
}

package com.aol.cyclops2.value;

public class AsStreamableValueTest {
/**
	@Test
	public void testAsStreamableValue() {
		double total = AsStreamableValue.<Double>asStreamableValue(new BaseData(10.00,5.00,100.30))
									.stream().collect(CyclopsCollectors.summingDouble(t->t));
		
		assertThat(total,equalTo(115.3));
	}
	@Test
	public void testAsStreamableValueDo() {
		
		Stream<Double> withBonus = AsStreamableValue.<Double>asStreamableValue(new BaseData(10.00,5.00,100.30))
									.doWithThisAnd(d->AsStreamableValue.<Double>asStreamableValue(new Bonus(2.0)))
									.yield((Double base)->(Double bonus)-> base*(1.0+bonus));
		
		
		//withBonus.forEach(System.out::println);
		val total = withBonus.collect(CyclopsCollectors.summingDouble(t->t));
		
		assertThat(total,equalTo(345.9));
	}
	
	@Value
	static class BaseData{
		double salary;
		double pension;
		double socialClub;
	}
	@Value
	static class Bonus{
		double bonus;
		
	}
**/

}

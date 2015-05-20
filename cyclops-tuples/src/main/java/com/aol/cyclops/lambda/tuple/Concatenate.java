package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;



public interface Concatenate{

	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10),(PowerTuples.tuple(100));
     *   // [10,100] 
     *  }
     *  
     **/
	public static <T1,NT1> PTuple2<T1,NT1> concat(PTuple1<T1> first, PTuple1<NT1> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),2){
			
			
			public T1 v1(){
				return first.v1();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),concatWith.v1());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10),(PowerTuples.tuple(100,101));
     *   // [10,100,101] 
     *  }
     *  
     **/
	public static <T1,NT1,NT2> PTuple3<T1,NT1,NT2> concat(PTuple1<T1> first, PTuple2<NT1,NT2> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),3){
			
			
			public T1 v1(){
				return first.v1();
			}

			public NT2 v2(){
				return concatWith.v2();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),concatWith.v1(),concatWith.v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10),(PowerTuples.tuple(100,101,102));
     *   // [10,100,101,102] 
     *  }
     *  
     **/
	public static <T1,NT1,NT2,NT3> PTuple4<T1,NT1,NT2,NT3> concat(PTuple1<T1> first, PTuple3<NT1,NT2,NT3> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),4){
			
			
			public T1 v1(){
				return first.v1();
			}

			public NT2 v2(){
				return concatWith.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),concatWith.v1(),concatWith.v2(),concatWith.v3());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10),(PowerTuples.tuple(100,101,102,103));
     *   // [10,100,101,102,103] 
     *  }
     *  
     **/
	public static <T1,NT1,NT2,NT3,NT4> PTuple5<T1,NT1,NT2,NT3,NT4> concat(PTuple1<T1> first, PTuple4<NT1,NT2,NT3,NT4> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),5){
			
			
			public T1 v1(){
				return first.v1();
			}

			public NT2 v2(){
				return concatWith.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10),(PowerTuples.tuple(100,101,102,103,104));
     *   // [10,100,101,102,103,104] 
     *  }
     *  
     **/
	public static <T1,NT1,NT2,NT3,NT4,NT5> PTuple6<T1,NT1,NT2,NT3,NT4,NT5> concat(PTuple1<T1> first, PTuple5<NT1,NT2,NT3,NT4,NT5> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),6){
			
			
			public T1 v1(){
				return first.v1();
			}

			public NT2 v2(){
				return concatWith.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}

			public NT5 v5(){
				return concatWith.v5();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4(),concatWith.v5());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10),(PowerTuples.tuple(100,101,102,103,104,105));
     *   // [10,100,101,102,103,104,105] 
     *  }
     *  
     **/
	public static <T1,NT1,NT2,NT3,NT4,NT5,NT6> PTuple7<T1,NT1,NT2,NT3,NT4,NT5,NT6> concat(PTuple1<T1> first, PTuple6<NT1,NT2,NT3,NT4,NT5,NT6> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),7){
			
			
			public T1 v1(){
				return first.v1();
			}

			public NT2 v2(){
				return concatWith.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}

			public NT5 v5(){
				return concatWith.v5();
			}

			public NT6 v6(){
				return concatWith.v6();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4(),concatWith.v5(),concatWith.v6());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10),(PowerTuples.tuple(100,101,102,103,104,105,106));
     *   // [10,100,101,102,103,104,105,106] 
     *  }
     *  
     **/
	public static <T1,NT1,NT2,NT3,NT4,NT5,NT6,NT7> PTuple8<T1,NT1,NT2,NT3,NT4,NT5,NT6,NT7> concat(PTuple1<T1> first, PTuple7<NT1,NT2,NT3,NT4,NT5,NT6,NT7> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T1 v1(){
				return first.v1();
			}

			public NT2 v2(){
				return concatWith.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}

			public NT5 v5(){
				return concatWith.v5();
			}

			public NT6 v6(){
				return concatWith.v6();
			}

			public NT7 v7(){
				return concatWith.v7();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4(),concatWith.v5(),concatWith.v6(),concatWith.v7());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}



	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11),(PowerTuples.tuple(100));
     *   // [10,11,100] 
     *  }
     *  
     **/
	public static <T1,T2,NT1> PTuple3<T1,T2,NT1> concat(PTuple2<T1,T2> first, PTuple1<NT1> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),3){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),concatWith.v1());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11),(PowerTuples.tuple(100,101));
     *   // [10,11,100,101] 
     *  }
     *  
     **/
	public static <T1,T2,NT1,NT2> PTuple4<T1,T2,NT1,NT2> concat(PTuple2<T1,T2> first, PTuple2<NT1,NT2> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),4){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),concatWith.v1(),concatWith.v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11),(PowerTuples.tuple(100,101,102));
     *   // [10,11,100,101,102] 
     *  }
     *  
     **/
	public static <T1,T2,NT1,NT2,NT3> PTuple5<T1,T2,NT1,NT2,NT3> concat(PTuple2<T1,T2> first, PTuple3<NT1,NT2,NT3> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),5){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),concatWith.v1(),concatWith.v2(),concatWith.v3());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11),(PowerTuples.tuple(100,101,102,103));
     *   // [10,11,100,101,102,103] 
     *  }
     *  
     **/
	public static <T1,T2,NT1,NT2,NT3,NT4> PTuple6<T1,T2,NT1,NT2,NT3,NT4> concat(PTuple2<T1,T2> first, PTuple4<NT1,NT2,NT3,NT4> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),6){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11),(PowerTuples.tuple(100,101,102,103,104));
     *   // [10,11,100,101,102,103,104] 
     *  }
     *  
     **/
	public static <T1,T2,NT1,NT2,NT3,NT4,NT5> PTuple7<T1,T2,NT1,NT2,NT3,NT4,NT5> concat(PTuple2<T1,T2> first, PTuple5<NT1,NT2,NT3,NT4,NT5> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),7){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}

			public NT5 v5(){
				return concatWith.v5();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4(),concatWith.v5());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11),(PowerTuples.tuple(100,101,102,103,104,105));
     *   // [10,11,100,101,102,103,104,105] 
     *  }
     *  
     **/
	public static <T1,T2,NT1,NT2,NT3,NT4,NT5,NT6> PTuple8<T1,T2,NT1,NT2,NT3,NT4,NT5,NT6> concat(PTuple2<T1,T2> first, PTuple6<NT1,NT2,NT3,NT4,NT5,NT6> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public NT3 v3(){
				return concatWith.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}

			public NT5 v5(){
				return concatWith.v5();
			}

			public NT6 v6(){
				return concatWith.v6();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4(),concatWith.v5(),concatWith.v6());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}



	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12),(PowerTuples.tuple(100));
     *   // [10,11,12,100] 
     *  }
     *  
     **/
	public static <T1,T2,T3,NT1> PTuple4<T1,T2,T3,NT1> concat(PTuple3<T1,T2,T3> first, PTuple1<NT1> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),4){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),concatWith.v1());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12),(PowerTuples.tuple(100,101));
     *   // [10,11,12,100,101] 
     *  }
     *  
     **/
	public static <T1,T2,T3,NT1,NT2> PTuple5<T1,T2,T3,NT1,NT2> concat(PTuple3<T1,T2,T3> first, PTuple2<NT1,NT2> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),5){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),concatWith.v1(),concatWith.v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12),(PowerTuples.tuple(100,101,102));
     *   // [10,11,12,100,101,102] 
     *  }
     *  
     **/
	public static <T1,T2,T3,NT1,NT2,NT3> PTuple6<T1,T2,T3,NT1,NT2,NT3> concat(PTuple3<T1,T2,T3> first, PTuple3<NT1,NT2,NT3> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),6){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),concatWith.v1(),concatWith.v2(),concatWith.v3());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12),(PowerTuples.tuple(100,101,102,103));
     *   // [10,11,12,100,101,102,103] 
     *  }
     *  
     **/
	public static <T1,T2,T3,NT1,NT2,NT3,NT4> PTuple7<T1,T2,T3,NT1,NT2,NT3,NT4> concat(PTuple3<T1,T2,T3> first, PTuple4<NT1,NT2,NT3,NT4> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),7){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12),(PowerTuples.tuple(100,101,102,103,104));
     *   // [10,11,12,100,101,102,103,104] 
     *  }
     *  
     **/
	public static <T1,T2,T3,NT1,NT2,NT3,NT4,NT5> PTuple8<T1,T2,T3,NT1,NT2,NT3,NT4,NT5> concat(PTuple3<T1,T2,T3> first, PTuple5<NT1,NT2,NT3,NT4,NT5> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public NT4 v4(){
				return concatWith.v4();
			}

			public NT5 v5(){
				return concatWith.v5();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4(),concatWith.v5());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}



	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13),(PowerTuples.tuple(100));
     *   // [10,11,12,13,100] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,NT1> PTuple5<T1,T2,T3,T4,NT1> concat(PTuple4<T1,T2,T3,T4> first, PTuple1<NT1> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),5){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),concatWith.v1());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13),(PowerTuples.tuple(100,101));
     *   // [10,11,12,13,100,101] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,NT1,NT2> PTuple6<T1,T2,T3,T4,NT1,NT2> concat(PTuple4<T1,T2,T3,T4> first, PTuple2<NT1,NT2> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),6){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),concatWith.v1(),concatWith.v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13),(PowerTuples.tuple(100,101,102));
     *   // [10,11,12,13,100,101,102] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,NT1,NT2,NT3> PTuple7<T1,T2,T3,T4,NT1,NT2,NT3> concat(PTuple4<T1,T2,T3,T4> first, PTuple3<NT1,NT2,NT3> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),7){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),concatWith.v1(),concatWith.v2(),concatWith.v3());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13),(PowerTuples.tuple(100,101,102,103));
     *   // [10,11,12,13,100,101,102,103] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,NT1,NT2,NT3,NT4> PTuple8<T1,T2,T3,T4,NT1,NT2,NT3,NT4> concat(PTuple4<T1,T2,T3,T4> first, PTuple4<NT1,NT2,NT3,NT4> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),concatWith.v1(),concatWith.v2(),concatWith.v3(),concatWith.v4());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}



	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13,14),(PowerTuples.tuple(100));
     *   // [10,11,12,13,14,100] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,T5,NT1> PTuple6<T1,T2,T3,T4,T5,NT1> concat(PTuple5<T1,T2,T3,T4,T5> first, PTuple1<NT1> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),6){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}

			public T5 v5(){
				return first.v5();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),first.v5(),concatWith.v1());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13,14),(PowerTuples.tuple(100,101));
     *   // [10,11,12,13,14,100,101] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,T5,NT1,NT2> PTuple7<T1,T2,T3,T4,T5,NT1,NT2> concat(PTuple5<T1,T2,T3,T4,T5> first, PTuple2<NT1,NT2> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),7){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}

			public T5 v5(){
				return first.v5();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),first.v5(),concatWith.v1(),concatWith.v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13,14),(PowerTuples.tuple(100,101,102));
     *   // [10,11,12,13,14,100,101,102] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,T5,NT1,NT2,NT3> PTuple8<T1,T2,T3,T4,T5,NT1,NT2,NT3> concat(PTuple5<T1,T2,T3,T4,T5> first, PTuple3<NT1,NT2,NT3> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}

			public T5 v5(){
				return first.v5();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),first.v5(),concatWith.v1(),concatWith.v2(),concatWith.v3());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}



	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13,14,15),(PowerTuples.tuple(100));
     *   // [10,11,12,13,14,15,100] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,T5,T6,NT1> PTuple7<T1,T2,T3,T4,T5,T6,NT1> concat(PTuple6<T1,T2,T3,T4,T5,T6> first, PTuple1<NT1> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),7){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}

			public T5 v5(){
				return first.v5();
			}

			public T6 v6(){
				return first.v6();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),first.v5(),first.v6(),concatWith.v1());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}


	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13,14,15),(PowerTuples.tuple(100,101));
     *   // [10,11,12,13,14,15,100,101] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,T5,T6,NT1,NT2> PTuple8<T1,T2,T3,T4,T5,T6,NT1,NT2> concat(PTuple6<T1,T2,T3,T4,T5,T6> first, PTuple2<NT1,NT2> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}

			public T5 v5(){
				return first.v5();
			}

			public T6 v6(){
				return first.v6();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),first.v5(),first.v6(),concatWith.v1(),concatWith.v2());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}



	
    /**
     * Concatenate two PTuples creating a new PTuple. 
     *
     *   {@code 
     *      concat(PowerTuples.tuple(10,11,12,13,14,15,16),(PowerTuples.tuple(100));
     *   // [10,11,12,13,14,15,16,100] 
     *  }
     *  
     **/
	public static <T1,T2,T3,T4,T5,T6,T7,NT1> PTuple8<T1,T2,T3,T4,T5,T6,T7,NT1> concat(PTuple7<T1,T2,T3,T4,T5,T6,T7> first, PTuple1<NT1> concatWith){
		
		
		return new TupleImpl(Arrays.asList(),8){
			
			
			public T1 v1(){
				return first.v1();
			}

			public T2 v2(){
				return first.v2();
			}

			public T3 v3(){
				return first.v3();
			}

			public T4 v4(){
				return first.v4();
			}

			public T5 v5(){
				return first.v5();
			}

			public T6 v6(){
				return first.v6();
			}

			public T7 v7(){
				return first.v7();
			}


			
			@Override
			public List<Object> getCachedValues() {
				return Arrays.asList(first.v1(),first.v2(),first.v3(),first.v4(),first.v5(),first.v6(),first.v7(),concatWith.v1());
			}

			@Override
			public Iterator iterator() {
				return getCachedValues().iterator();
			}

			
		};
		
	}
}

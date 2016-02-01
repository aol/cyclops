package com.aol.cyclops.lambda.tuple

import org.junit.Test



class LazySwapGenerator {
	
	@Test
	public void swapGen(){
		(2..8).forEach{
			println template(it)
		}
		//int size =2
		//println template(size)	
	}
	
	def methods(size){
		StringBuilder b = new StringBuilder()
		size.times{
			b.append ("""
				public T${size-it} v${it+1}(){
					return host.v${size-it}();
				}
""")
		}
		return b
	}
	
	def list(size){
		StringBuilder b = new StringBuilder()
		String sep  = ","
		size.times{
			if(it==size-1)
				sep=""
			b.insert(0,"""${sep}v${it+1}()""")
		}
		return b
	}
	def typesForwards(size) {
		StringBuilder b = new StringBuilder("<")
		String sep = "T"
		size.times{
			
			b.append(sep +(it+1))
			sep=",T"
			
		}
		return b.append(">").toString()
		
	}
	def types(size) {
		StringBuilder b = new StringBuilder("<>")
		String sep = ",T"
		size.times{
			if(it==size-1)
				sep="T"
			b.insert(1,sep +(it+1))
			
		}
		return b.toString()
		
	}
	def values(size){
		StringBuilder b = new StringBuilder()
		def sep =""
		size.times{
			
			b.append(sep+(it+10))
			sep =","
		}
		return b
	}
	
	def template (size) { 
		"""		
		/**
         * Lazily reverse the order of the supplied PTuple. Can be used in conjunction with lazy mapping to avoid unneccasary work.
         * {@code  
         *     lazySwap(PowerTuple.of(${values(size)}).lazyMap1(expensiveFunction));
         *  }
         *
         */
		public static ${typesForwards(size)} PTuple${size}${types(size)} lazySwap(PTuple${size}${typesForwards(size)} host){
			
			
			return new TupleImpl(Arrays.asList(),${size}){
				
				${methods(size)}

				
				@Override
				public List<Object> getCachedValues() {
					return Arrays.asList(${list(size)});
				}

				@Override
				public Iterator iterator() {
					return getCachedValues().iterator();
				}

				
			};
			
		}
	"""
	}
	
}

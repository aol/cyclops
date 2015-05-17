package com.aol.cyclops.lambda.tuple

import org.junit.Test



class LazySwapGenerator {
	
	@Test
	public void swapGen(){
		int size =2
		println template(size)	
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
	
	def template (size) { 
		"""		
		public PTuple${size}${types(size)} lazySwap(){
			
			val host = this;
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

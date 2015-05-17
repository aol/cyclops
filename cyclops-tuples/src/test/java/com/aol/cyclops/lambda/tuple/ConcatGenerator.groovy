package com.aol.cyclops.lambda.tuple

import org.junit.Test



class ConcatGenerator {
	
	@Test
	public void swapGen(){
		int size =2
		int size2 =2
		println template(size,size2)	
	}
	
	def methods(size,size2){
		StringBuilder b = new StringBuilder()
		(size).times{
			b.append ("""
				public T${it+1} v${it+1}(){
					return host.v${it+1}();
				}
""")
		}
		(size2).times{
			b.append ("""
				public T${it+1+size} v${it+1+size}(){
					return concatWith.v${it+1+size}();
				}
""")
		}
		return b
	}
	
	def list(size,size2){
		StringBuilder b = new StringBuilder()
		String sep  = ""
		(size).times{
			if(it>0)
				sep=","
			b.append("""${sep}host.v${it+1}()""")
		}
		(size2).times{
			if(it>0)
				sep=","
			b.append("""${sep}concatWith.v${it+1}()""")
		}
		return b
	}
	
	def types(size,int offset=0) {
		StringBuilder b = new StringBuilder("<")
		String sep = ""
		size.times{
			if(it>0)
				sep=","
			b.append(sep+"T" +(it+1+offset))
			
		}
		return b.append(">").toString()
		
	}
	
	def template (size,size2) { 
		"""		
		public PTuple${size}${types(size)} concat(PTuple${size2}${types(size2,size)} concatWith){
			
			val host = this;
			return new TupleImpl(Arrays.asList(),${size+size2}){
				
				${methods(size,size2)}

				
				@Override
				public List<Object> getCachedValues() {
					return Arrays.asList(${list(size,size2)});
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

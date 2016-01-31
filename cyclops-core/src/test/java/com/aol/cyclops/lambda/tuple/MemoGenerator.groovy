package com.aol.cyclops.lambda.tuple

import org.junit.Test



class MemoGenerator {
	
	@Test
	public void swapGen(){
		int size =8
		println template(size)	
	}
	
	def methods(size){
		StringBuilder b = new StringBuilder()
		size.times{
			b.append ("""
				public T${it+1} v${it+1}(){
					return ( T${it+1})values.computeIfAbsent(new Integer($it), key -> host.v${it+1}());
				}
""")
		}
		return b
	}
	
	def list(size){
		StringBuilder b = new StringBuilder()
		String sep  = ""
		size.times{
			if(it>0)
				sep=","
			b.append("${sep}v${it+1}()")
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
	
	def template (size) { 
		"""		
		default PTuple${size}${types(size)} memo(){
			if(arity()!=$size)
				return PTuple${size-1}.super.memo();
			val host = this;
			Map<Integer,Object> values = new ConcurrentHashMap<>();
			
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

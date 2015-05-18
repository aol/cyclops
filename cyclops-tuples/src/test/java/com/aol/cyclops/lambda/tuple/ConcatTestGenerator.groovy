package com.aol.cyclops.lambda.tuple

import org.junit.Test



class ConcatTestGenerator {
	
	@Test
	public void swapGen(){
		8.times{
			println genAll(it+1)
		}
	}
	
	def genAll(int size){
		(8-size).times{
			println template(size,it+1)
		}
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
		(size2-size).times{
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
			
			b.append("""${sep}host.v${it+1}()""")
			sep=","
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
		offset.times{
			
			b.append(sep+"?")
			sep=","
		}
		(size-offset).times{
			if(it>0)
				sep=","
			b.append(sep+"T" +(it+1+offset))
			
		}
		return b.append(">").toString()
		
	}
	def typesDef(size,int offset=0) {
		StringBuilder b = new StringBuilder("<")
		String sep = ""
		
		(size-offset).times{
			if(it>0)
				sep=","
			b.append(sep+"T" +(it+1+offset))
			
		}
		return b.append(">").toString()
		
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
	def values2(size2){
		StringBuilder b = new StringBuilder()
		def sep =""
		size2.times{
			
			b.append(sep+(it+100))
			sep =","
		}
		return b
	}
	def newValues(size,size2){
		StringBuilder b = new StringBuilder()
		def sep =""
		size.times{
			
			b.append(sep+(it+10))
			sep =","
		}
		size2.times{
			
			b.append(sep+(it+100))
			sep =","
		}
		return b
	}
	
	def template (size,size2) { 
		"""
		@Test
		public void testConcatPTuple${size}_${size2}(){
			assertThat(concat(PowerTuples.tuple(${values(size)}),PowerTuples.tuple(${values2(size2)})).toList()
					,equalTo(Arrays.asList(${newValues(size,size2)})));
		}
        """
	}
	
}

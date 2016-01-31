package com.aol.cyclops.lambda.tuple

import org.junit.Test



class ConcatGenerator {
	
	@Test
	public void swapGen(){
		8.times{
			println genAll(it+1)
		}
	//	int size =1
		
		
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
					return first.v${it+1}();
				}
""")
		}
		(size2-size).times{
			b.append ("""
				public NT${it+1+size} v${it+1+size}(){
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
			
			b.append("""${sep}first.v${it+1}()""")
			sep=","
		}
		(size2).times{
			if(it>0)
				sep=","
			b.append("""${sep}concatWith.v${it+1}()""")
		}
		return b
	}
	
	def types(size) {
		StringBuilder b = new StringBuilder("<")
		String sep = ""
		
		(size).times{
			
				
			b.append(sep+"NT" +(it+1))
			sep=","
			
		}
		return b.append(">").toString()
		
	}
	def typesOld(size) {
		StringBuilder b = new StringBuilder("<")
		String sep = ""
		
		(size).times{
			
				
			b.append(sep+"T" +(it+1))
			sep=","
			
		}
		return b.append(">").toString()
		
	}
	def concatTypes(size,size2) {
		StringBuilder b = new StringBuilder("<")
		String sep = ""
		(size).times{
			
				
			b.append(sep+"T" +(it+1))
			sep=","
			
		}
		(size2).times{
			
				
			b.append(sep+"NT" +(it+1))
			sep=","
			
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
		
        /**
         * Concatenate two PTuples creating a new PTuple. 
         *
         *   {@code 
         *      concat(PowerTuples.tuple(${values(size)}),PowerTuples.tuple(${values2(size2)}));
         *   // [${newValues(size,size2)}] 
         *  }
         *  
         **/
		public static ${concatTypes(size,size2)} PTuple${size+size2}${concatTypes(size,size2)} concat(PTuple${size}${typesOld(size)} first, PTuple${size2}${types(size2)} concatWith){
			
			
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

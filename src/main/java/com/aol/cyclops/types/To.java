package com.aol.cyclops.types;

import java.util.function.Function;

/**
 * Fluent interface for converting this type to another

<pre>
{@code 
   api.doThis(a->a*2)
      .doThat(a->a+2)
      .to(Stream::toStream)
      .peek(System.out::println);
      
 }
 </pre>      
       
 * 
 * @author johnmcclean
 *
 * @param <T> Self type used for conversion
 */
public interface To<T extends To<?>> {

    /**
     * Fluent api for type conversion
     *      
     * @param reduce Funtion to convert this type
     * @return Converted type
     */
    default <R> R to(Function<? super T,? extends R> reduce){
        return reduce.apply((T)this);
    }
}

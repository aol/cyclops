package com.aol.simple.react.extractors;

import java.util.Collection;
import java.util.List;

/**
 * Default extractors for determining return values from blocking stage.
 * 
 * @author johnmcclean
 *
 */
public class Extractors {
	
	/**
	 * @return First element of a collection
	 */
	public static <R> Extractor<Collection<R>,R> first(){
		return (results)-> results.iterator().next();
	}
	
	/**
	 * @return Last element of a list
	 */
	public static <R> Extractor<List<R>,R> last(){
		return (results)-> results.get(results.size()-1);
	}
	
}

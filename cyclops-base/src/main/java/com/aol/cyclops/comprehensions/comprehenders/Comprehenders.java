package com.aol.cyclops.comprehensions.comprehenders;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.lambda.api.Reducers;
import com.aol.cyclops.streams.StreamUtils;

/**
 * Registered Comprehenders
 * 
 * @author johnmcclean
 *
 */
public class Comprehenders {
	
	@AllArgsConstructor
	public static enum Companion{
		instance(new Comprehenders());
		@Getter
		private final Comprehenders comprehenders;
		
	/**	public Comprehenders withMoreComprehenders( Map<Class,Comprehender> comprehenders){
			return new Comprehenders(defaultComprehenders,comprehenders);
		}**/
	}
	
//	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
//	public static final Map<Class,Comprehender> defaultComprehenders;
	
	/** = new HashMap<Class,Comprehender>(){{
		put(Optional.class,new OptionalComprehender());
		put(Stream.class,new StreamComprehender());
		put(IntStream.class,new IntStreamComprehender());
		put(LongStream.class,new LongStreamComprehender());
		put(DoubleStream.class,new DoubleStreamComprehender());
		put(CompletableFuture.class, new CompletableFutureComprehender());
		try{
			Class cases = Class.forName("com.aol.cyclops.matcher.Cases");
			put(cases,(Comprehender)Class.forName("com.aol.cyclops.matcher.comprehenders.CasesComprehender").newInstance());
		}catch(Exception e){
			
		}
		try{
			Class caze = Class.forName("com.aol.cyclops.matcher.Case");
			put(caze,(Comprehender)Class.forName("com.aol.cyclops.matcher.comprehenders.CaseComprehender").newInstance());
		}catch(Exception e){
			
		}
		try{
			Class caze = Class.forName("com.aol.cyclops.enableswitch.Switch");
			put(caze,(Comprehender)Class.forName("com.aol.cyclops.enableswitch.SwitchComprehender").newInstance());
		}catch(Exception e){
			
		}
		try{
			Class caze = Class.forName("com.aol.cyclops.trycatch.Try");
			put(caze,(Comprehender)Class.forName("com.aol.cyclops.trycatch.TryComprehender").newInstance());
		}catch(Exception e){
			
		}
		
	}};**/
	private final static PMap<Class,Comprehender> comprehenders;
	static {
		
		val loader  = ServiceLoader.load(Comprehender.class);
	
	
		comprehenders = Reducers.<Class,Comprehender>toPMap().reduce(StreamUtils.stream(loader.iterator())
													.filter(c -> !(c instanceof InvokeDynamicComprehender))
													.map(comp->HashTreePMap.singleton(comp.getTargetClass(),comp)));
	
		
}
	private Comprehenders(){
	//	comprehenders = HashTreePMap.from(defaultComprehenders);
	}
	/**
	private Comprehenders(Map<Class,Comprehender> map,Map<Class,Comprehender> map2){
		comprehenders = HashTreePMap.from(map).plusAll(map2);
	}
	public Comprehenders(Map<Class,Comprehender> map){
		comprehenders = HashTreePMap.from(map);
	}
	
	**/
	/**
	 * @return Registered Comprehenders
	 */
	public Map<Class,Comprehender> getRegisteredComprehenders(){
		return new HashMap(comprehenders);
	}
	
}

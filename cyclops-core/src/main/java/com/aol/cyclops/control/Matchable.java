package com.aol.cyclops.control;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;

import com.aol.cyclops.internal.matcher2.CheckValues;
import com.aol.cyclops.internal.matcher2.MatchableCase;
import com.aol.cyclops.internal.matcher2.MatchingInstance;
import com.aol.cyclops.internal.matcher2.PatternMatcher;
import com.aol.cyclops.control.SequenceM;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AllArgsConstructor;

/**
 * Matchable
 * 
 * todo - add AsMatchable.asMatchable
 * 
 * @author johnmcclean
 *
 */
public interface Matchable<TYPE>{
	
	public static interface MatchSelf<TYPE> extends Matchable<TYPE>{
		default Object getMatchable(){
			return this;
		}
	}
	
	/**
	 * @return matchable
	 */
	Object getMatchable();
	
	/*
	 * Match against the values inside the matchable with a single case
	 * 
	 * <pre>
	 * {@code
	 * int result = Matchable.of(Optional.of(1))
								.matches(c->c.hasValues(1).then(i->2));
		//2						
	 * }</pre>
	 * 
	 * Note, it is possible to continue to chain cases within a single case, but cleaner
	 * to use the appropriate overloaded matches method that accepts two (or more) cases.
	 * 
	 * @param fn1 Describes the matching case
	 * @return Result - this method requires a match or an NoSuchElement exception is thrown
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R> Eval<R>  matches(Function<CheckValues<? super TYPE,R>,CheckValues<? super TYPE, R>> fn1){
		return Eval.later(()->(R) new MatchingInstance(new MatchableCase( fn1.apply( (CheckValues)
				new MatchableCase(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable()).get());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R> Maybe<R>  mayMatch(Function<CheckValues<? super TYPE,R>,CheckValues<? super TYPE,R>> fn1){
		return  new MatchingInstance(new MatchableCase( fn1.apply( (CheckValues)
				new MatchableCase(new PatternMatcher()).withType(getMatchable().getClass())).getPatternMatcher()))
					.match(getMatchable());
	}
	
	
	/**
	 * Create a new matchable that will match on the fields of the provided Object
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static<T> Matchable<T> of(T o){
		if(o instanceof Stream)
			return ofStream((Stream)o);
		return AsMatchable.asMatchable(o);
	}
	/**
	 * Create a new matchable that will match on the fields of the provided Stream
	 * 
	 * @param o Object to match on it's fields
	 * @return new Matchable
	 */
	public static <T> Matchable<T> ofStream(Stream<T> o){
		return AsMatchable.asMatchable(o.collect(Collectors.toList()));
	}
	/**
	 * Create a new matchable that will match on the fields of the provided Decomposable
	 * 
	 * @param o Decomposable to match on it's fields
	 * @return new Matchable
	 */
	public static <T extends Decomposable> Matchable<T> ofDecomposable(Decomposable o){
		return AsMatchable.asMatchable(o);
	}
	
	/**
	 * Create a matchable that matches on the provided Objects
	 * 
	 * @param o Objects to match on
	 * @return new Matchable
	 */
	public static <T>  Matchable<T> listOfValues(T... o){
		return AsMatchable.asMatchable(Arrays.asList(o));
	}
	public static <T> MatchableIterable<T> fromIterable(Iterable<T> it){
		return ()->it;
	}
	static String concat(String a, String b){
		return a+b;
	}
	public static <TYPE, T1 extends TYPE> MatchableTuple1<T1> from(Supplier<T1> s1){
		
		return ()-> Tuple.tuple(s1.get());
	}
	public static <TYPE, T1 extends TYPE> MatchableTuple1<T1> from(Tuple1<T1> t2){
		return ()-> t2;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE> MatchableTuple2<T1,T2> from(Supplier<T1> s1, Supplier<T2> s2){
		return ()-> Tuple.tuple(s1.get(),s2.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE> MatchableTuple2<T1,T2> from(Tuple2<T1,T2> t2){
		return ()-> t2;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE> MatchableTuple3<T1,T2,T3> from(Tuple3<T1,T2,T3> t3){
		return ()-> t3;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE> MatchableTuple3<T1,T2,T3> from(Supplier<T1> s1, Supplier<T2> s2, Supplier<T3> s3){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE> MatchableTuple4<T1,T2,T3,T4> from(Tuple4<T1,T2,T3,T4> t4){
		return ()-> t4;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE> MatchableTuple4<T1,T2,T3,T4> from(Supplier<T1> s1, Supplier<T2> s2, 
												Supplier<T3> s3,Supplier<T4> s4){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get(),s4.get());
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE,T5 extends TYPE> MatchableTuple5<T1,T2,T3,T4,T5> from(Tuple5<T1,T2,T3,T4,T5> t5){
		return ()-> t5;
	}
	public static <TYPE, T1 extends TYPE, T2 extends TYPE,T3 extends TYPE,T4 extends TYPE,T5 extends TYPE> MatchableTuple5<T1,T2,T3,T4,T5> from(Supplier<T1> s1, Supplier<T2> s2, 
												Supplier<T3> s3,Supplier<T4> s4,Supplier<T5> s5){
		return()-> Tuple.tuple(s1.get(),s2.get(),s3.get(),s4.get(),s5.get());
	}
	
	public static  MatchableIterable<Character> fromCharSequence(CharSequence chars){
		Iterable<Character> it = ()->chars.chars().boxed().map(i ->Character.toChars(i)[0]).iterator();
		return ()-> it;
	}
	
	public static interface MatchableIterable<TYPE> extends Matchable<TYPE>{
		
		default <R> Eval<R> visit(BiFunction<? super Maybe<TYPE>,? super SequenceM<TYPE>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Iterable<TYPE> it = (Iterable<TYPE>)getMatchable();
			return Eval.later(()->SequenceM.fromIterable(it).visit(match));	
		}	
		default Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> toTuple5(Object o){
			Iterator it = ((Iterable)o).iterator();
			return Tuple.tuple((TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null),
					(TYPE)(it.hasNext() ?it.next():null));
		}
		default MatchableTuple1<TYPE> on$1____(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v1);
		}
		default MatchableTuple1<TYPE> on$_2___(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v2);
		}
		default MatchableTuple1<TYPE> on$__3__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v3);
		}
		default MatchableTuple1<TYPE> on$___4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v4);
		}
		default MatchableTuple1<TYPE> on$____5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple1<TYPE>(it.v5);
		}
		default MatchableTuple2<TYPE,TYPE> on$12___(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v2);
		}
		default MatchableTuple2<TYPE,TYPE> on$1_3__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v3);
		}
		default MatchableTuple2<TYPE,TYPE> on$1__4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE> on$1___5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v1,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE> on$_23__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v3);
		}
		default MatchableTuple2<TYPE,TYPE> on$_2_4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE> on$_2__5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v2,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE> on$__34_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v3,it.v4);
		}
		default MatchableTuple2<TYPE,TYPE> on$__3_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v3,it.v5);
		}
		default MatchableTuple2<TYPE,TYPE> on$___45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple2<TYPE,TYPE>(it.v4,it.v5);
		}
		
		default MatchableTuple3<TYPE,TYPE,TYPE> on$123__(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$12_4_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$12__5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v2,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$1_34_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$1_3_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$1__45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v1,it.v4,it.v5);
		}
	
		default MatchableTuple3<TYPE,TYPE,TYPE> on$_234_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v2,it.v3,it.v4);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$_23_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v2,it.v3,it.v5);
		}
		default MatchableTuple3<TYPE,TYPE,TYPE> on$__345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple3<TYPE,TYPE,TYPE>(it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$1234_(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3,it.v4);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$123_5(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v3,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$12_45(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v2,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$1_345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v1,it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<TYPE,TYPE,TYPE,TYPE> on$_2345(){
			Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE> it = (Tuple5<TYPE,TYPE,TYPE,TYPE,TYPE>)toTuple5(getMatchable());
			return ()->new Tuple4<TYPE,TYPE,TYPE,TYPE>(it.v2,it.v3,it.v4,it.v5);
		}
		
	}
	public static interface MatchableTuple1<T1> extends Matchable<Object>{
		Tuple1<T1> getMatchable();
		default <R> R visit(Function<? super T1,? extends R> some,Supplier<? extends R> none ){
			@SuppressWarnings("unchecked")
			Tuple1<T1> it = (Tuple1<T1>)getMatchable();
			
			return  Maybe.ofNullable(it.v1).visit(some, none);
		}	
		
		
	}
	
	public static interface MatchableTuple2<T1,T2> extends Matchable<Object>{
		Tuple2<T1,T2> getMatchable();
		default <R> R visit(BiFunction<? super Maybe<T1>,? super Maybe<T2>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple2<T1,T2> it = (Tuple2<T1,T2>)getMatchable();
			return  match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2));
		}	
		default MatchableTuple1<T1> on$1_(){
			Tuple2<T1,T2> it = (Tuple2<T1,T2>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2(){
			Tuple2<T1,T2> it = (Tuple2<T1,T2>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		
	}
	
	public static interface MatchableTuple3<T1,T2,T3> extends Matchable<Object>{
		Tuple3<T1,T2,T3> getMatchable();
		default <R> Eval<R> visit(TriFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3)));
		}	
		
		default MatchableTuple1<T1> on$1__(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2_(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		default MatchableTuple1<T3> on$__3(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple1<T3>(it.v3);
		}
		default MatchableTuple2<T1,T2> on$12_(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<T1,T3> on$1_3(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<T2,T3> on$_23(){
			Tuple3<T1,T2,T3> it = (Tuple3<T1,T2,T3>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
	}
	public static interface MatchableTuple4<T1,T2,T3,T4> extends Matchable<Object>{
		Tuple4<T1,T2,T3,T4> getMatchable();
		default <R> Eval<R> visit(QuadFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? super Maybe<T4>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3),Maybe.ofNullable(it.v4)));
		}	
		default MatchableTuple1<T1> on$1___(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2__(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		default MatchableTuple1<T3> on$__3_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T3>(it.v3);
		}
		default MatchableTuple1<T4> on$___4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple1<T4>(it.v4);
		}
		default MatchableTuple2<T1,T2> on$12__(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<T1,T3> on$1_3_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<T1,T4> on$1__4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T1,T4>(it.v1,it.v4);
		}
		default MatchableTuple2<T2,T3> on$_23_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
		default MatchableTuple2<T2,T4> on$_2_4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T2,T4>(it.v2,it.v4);
		}
		default MatchableTuple2<T3,T4> on$__34(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple2<T3,T4>(it.v3,it.v4);
		}
		
		default MatchableTuple3<T1,T2,T3> on$123_(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T2,T3>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<T1,T2,T4> on$12_4(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T2,T4>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<T1,T3,T4> on$1_34(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T1,T3,T4>(it.v1,it.v3,it.v4);
		}
	
		default MatchableTuple3<T2,T3,T4> on$_234(){
			Tuple4<T1,T2,T3,T4> it = (Tuple4<T1,T2,T3,T4>)getMatchable();
			return ()->new Tuple3<T2,T3,T4>(it.v2,it.v3,it.v4);
		}

		
		
	}
	public static interface MatchableTuple5<T1,T2,T3,T4,T5> extends Matchable<Object>{
		Tuple5<T1,T2,T3,T4,T5> getMatchable();
		default <R> Eval<R> visit(QuintFunction<? super Maybe<T1>,? super Maybe<T2>,? super Maybe<T3>,? super Maybe<T4>,? super Maybe<T5>,? extends R> match ){
			@SuppressWarnings("unchecked")
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return  Eval.later(()->match.apply(Maybe.ofNullable(it.v1), Maybe.ofNullable(it.v2),Maybe.ofNullable(it.v3),Maybe.ofNullable(it.v4),Maybe.ofNullable(it.v5)));
		}	
		default MatchableTuple1<T1> on$1____(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T1>(it.v1);
		}
		default MatchableTuple1<T2> on$_2___(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T2>(it.v2);
		}
		default MatchableTuple1<T3> on$__3__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T3>(it.v3);
		}
		default MatchableTuple1<T4> on$___4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T4>(it.v4);
		}
		default MatchableTuple1<T5> on$____5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple1<T5>(it.v5);
		}
		default MatchableTuple2<T1,T2> on$12___(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T2>(it.v1,it.v2);
		}
		default MatchableTuple2<T1,T3> on$1_3__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T3>(it.v1,it.v3);
		}
		default MatchableTuple2<T1,T4> on$1__4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T4>(it.v1,it.v4);
		}
		default MatchableTuple2<T1,T5> on$1___5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T1,T5>(it.v1,it.v5);
		}
		default MatchableTuple2<T2,T3> on$_23__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T3>(it.v2,it.v3);
		}
		default MatchableTuple2<T2,T4> on$_2_4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T4>(it.v2,it.v4);
		}
		default MatchableTuple2<T2,T5> on$_2__5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T2,T5>(it.v2,it.v5);
		}
		default MatchableTuple2<T3,T4> on$__34_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T3,T4>(it.v3,it.v4);
		}
		default MatchableTuple2<T3,T5> on$__3_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T3,T5>(it.v3,it.v5);
		}
		default MatchableTuple2<T4,T5> on$___45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple2<T4,T5>(it.v4,it.v5);
		}
		
		default MatchableTuple3<T1,T2,T3> on$123__(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T3>(it.v1,it.v2,it.v3);
		}
		default MatchableTuple3<T1,T2,T4> on$12_4_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T4>(it.v1,it.v2,it.v4);
		}
		default MatchableTuple3<T1,T2,T5> on$12__5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T2,T5>(it.v1,it.v2,it.v5);
		}
		default MatchableTuple3<T1,T3,T4> on$1_34_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T3,T4>(it.v1,it.v3,it.v4);
		}
		default MatchableTuple3<T1,T3,T5> on$1_3_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T3,T5>(it.v1,it.v3,it.v5);
		}
		default MatchableTuple3<T1,T4,T5> on$1__45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T1,T4,T5>(it.v1,it.v4,it.v5);
		}
	
		default MatchableTuple3<T2,T3,T4> on$_234_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T2,T3,T4>(it.v2,it.v3,it.v4);
		}
		default MatchableTuple3<T2,T3,T5> on$_23_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T2,T3,T5>(it.v2,it.v3,it.v5);
		}
		default MatchableTuple3<T3,T4,T5> on$__345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple3<T3,T4,T5>(it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<T1,T2,T3,T4> on$1234_(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T3,T4>(it.v1,it.v2,it.v3,it.v4);
		}
		default MatchableTuple4<T1,T2,T3,T5> on$123_5(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T3,T5>(it.v1,it.v2,it.v3,it.v5);
		}
		default MatchableTuple4<T1,T2,T4,T5> on$12_45(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T2,T4,T5>(it.v1,it.v2,it.v4,it.v5);
		}
		default MatchableTuple4<T1,T3,T4,T5> on$1_345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T1,T3,T4,T5>(it.v1,it.v3,it.v4,it.v5);
		}
		default MatchableTuple4<T2,T3,T4,T5> on$_2345(){
			Tuple5<T1,T2,T3,T4,T5> it = (Tuple5<T1,T2,T3,T4,T5>)getMatchable();
			return ()->new Tuple4<T2,T3,T4,T5>(it.v2,it.v3,it.v4,it.v5);
		}
	}
	public class AsMatchable {
		
		
		/**
		 * Coerce / wrap an Object as a Matchable instance
		 * This adds match / _match methods for pattern matching against the object
		 * 
		 * @param toCoerce Object to convert into a Matchable
		 * @return Matchable that adds functionality to the supplied object
		 */
		public static  Matchable asMatchable(Object toCoerce){
			return new CoercedMatchable(toCoerce);
		}
		
		@AllArgsConstructor
		public static class CoercedMatchable<T> implements Matchable{
			private final Object matchable;

			@Override
			public Object getMatchable(){
				return matchable;
			}
			
		}
	}
}

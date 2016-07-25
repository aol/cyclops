package com.aol.cyclops;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.pcollections.PCollection;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;



/**
 * @author johnmcclean
 * A static class with a large number of Semigroups  or Combiners.
 * 
 * A semigroup is an Object that can be used to combine objects of the same type.
 *
 */
public interface Semigroups {

	/**
	 * To manage javac type inference first assign the semigroup
	 * <pre>
	 * {@code
	 *    
	 *    Semigroup<ListX<Integer>> listX = Semigroups.collectionXConcat();
	 *    Semigroup<SetX<Integer>> setX = Semigroups.collectionXConcat();
	 *    
	 *    
	 * 
	 * }
	 * </pre>
	 * @return A Semigroup that can combine any cyclops-react extended Collection type
	 */
	static <T, C extends FluentCollectionX<T>> Semigroup<C> collectionXConcat() {
		return  (a, b) -> (C) a.plusAll(b);
	}
	/**
	 * Concatenate mutable collections
	 * 
	 * To manage javac type inference first assign the semigroup
     * <pre>
     * {@code
     *    
     *    Semigroup<List<Integer>> list = Semigroups.collectionConcat();
     *    Semigroup<Set<Integer>> set = Semigroups.collectionConcat();
     *    
     *    
     * 
     * }
     * </pre> 
	 * @return A Semigroup that can combine any mutable collection type
	 */
	static <T, C extends Collection<T>> Semigroup<C> mutableCollectionConcat() {
        return  (a, b) -> {
           
            a.addAll(b);
            return a;
        };
    }
	/**
	 * This Semigroup will attempt to combine JDK Collections. If the Supplied are instances of cyclops-react extended Collections
	 * or a pCollection persisent collection a new Collection type is created that contains the entries from both supplied collections.
	 * If the supplied Collections are standard JDK mutable collections Colleciton b is appended to Collection a and a is returned.
	 * 
	 * 
	 * To manage javac type inference first assign the semigroup
     * <pre>
     * {@code
     *    
     *    Semigroup<List<Integer>> list = Semigroups.collectionConcat();
     *    Semigroup<Set<Integer>> set = Semigroups.collectionConcat();
     *    
     *    
     * 
     * }
     * </pre>
     * @return A Semigroup that attempts to combine the supplied Collections
     */
    static <T, C extends Collection<T>> Semigroup<C> collectionConcat() {
        return  (a, b) -> {
            if(a instanceof FluentCollectionX){
                return (C) ((FluentCollectionX)a).plusAll(b);
            }
            if(a instanceof PCollection){
                return (C) ((PCollection)a).plusAll(b);
            }
            if(b instanceof FluentCollectionX){
                return (C) ((FluentCollectionX)b).plusAll(a);
            }
            if(b instanceof PCollection){
                return (C) ((PCollection)b).plusAll(a);
            }
            a.addAll(b);
            return a;
        };
    }
    
	/**
	 * @return Combination of two ReactiveSeq Streams b is appended to a
	 */
	static <T> Semigroup<ReactiveSeq<T>> combineReactiveSeq() {
		return  (a, b) -> a.appendStream(b);
	}
	/**
	 * @return Combination of two Seq's : b is appended to a
	 */
	static <T> Semigroup<Seq<T>> combineSeq() {
        return  (a, b) -> Seq.concat(a,b);
    }
	/**
	 * @return Combination of two Stream's : b is appended to a
	 */
	static <T> Semigroup<Stream<T>> combineStream() {
        return  (a, b) -> Stream.concat(a,b);
    }
	/**
	 * @return Combination of two Objects of same type, first non-null is returned
	 */
	static <T> Semigroup<T> firstNonNull() {
        return  (a, b) -> a!=null ? a : b;
    }
	/**
	 * @return Combine two Maybe's by taking the first present
	 */
	static <T> Semigroup<Maybe<T>> firstPresentMaybe() {
		return  (a, b) -> a.isPresent() ? a : b;
	}

	/**
	 * @return Combine two optionals by taking the first present
	 */
	static <T> Semigroup<Optional<T>> firstPresentOptional() {
		return  (a, b) -> a.isPresent() ? a : b;
	}

	/**
	 * @return Combine two Maybes by taking the last present
	 */
	static <T> Semigroup<Maybe<T>> lastPresentMaybe() {
		return (a, b) -> b.isPresent() ? b : a;
	}

	/**
	 * @return Combine two optionals by taking the last present
	 */
	static <T> Semigroup<Optional<T>> lastPresentOptional() {
		return  (a, b) -> b.isPresent() ? b : a;
	}
	
	/**
	 * @param joiner Separator in joined String
	 * @return Combine two strings separated by the supplied joiner
	 */
	static Semigroup<String> stringJoin(String joiner) {
		return  (a, b) -> a + joiner + b;
	}

	/**
     * @param joiner Separator in joined String
     * @return Combine two StringBuilders separated by the supplied joiner
     */
	static Semigroup<StringBuilder> stringBuilderJoin(String joiner) {
		return (a, b) -> a.append(joiner).append(b);
	}
	/**
     * @param joiner Separator in joined String
     * @return Combine two StringBuffers separated by the supplied joiner
     */
	static Semigroup<StringBuffer> stringBufferJoin(String joiner) {
		return  (a, b) -> a.append(joiner).append(b);
	}
	/**
	 * @return Combine two Comparables taking the lowest each time
	 */
	static <T, T2 extends Comparable<T>> Semigroup<T2> minComparable(){
		return (a,b) ->a.compareTo((T)b) > 0 ? b:a;
	}
	/**
	 * @return Combine two Comparables taking the highest each time
	 */
	static <T, T2 extends Comparable<T>> Semigroup<T2> maxComparable(){
		return (a,b) ->a.compareTo((T)b) > 0 ? a:b;
	}

	/**
	 * Combine two BigIntegers by adding one to a (can be used to count BigIntegers in a Collection or Stream)
	 */
	static Semigroup<BigInteger> bigIntCount = (a, b) -> a.add(BigInteger.ONE);
	/**
	 * Combine two Integers by adding one to a (can be used to count Integers in a Collection or Stream)
	 */
	static Semigroup<Integer> intCount =  (a, b) -> a + 1;
	/**
     * Combine two Longs by adding one to a (can be used to count Integers in a Collection or Stream)
     */
	static Semigroup<Long> longCount =  (a, b) -> a + 1;
	/**
     * Combine two Double by adding one to a (can be used to count Double in a Collection or Stream)
     */
	static Semigroup<Double> doubleCount =  (a, b) -> a + 1;
	/**
	 * Combine two Integers by summing them
	 */
	static Semigroup<Integer> intSum =  (a, b) -> a + b;
	/**
     * Combine two Longs by summing them
     */
	static Semigroup<Long> longSum = (a, b) -> a + b;
	/**
     * Combine two Doubles by summing them
     */
	static Semigroup<Double> doubleSum =  (a, b) -> a + b;
	/**
     * Combine two BigIngegers by summing them
     */
	static Semigroup<BigInteger> bigIntSum = (a, b) -> a.add(b);
	/**
	 * Combine two Integers by multiplying them
	 */
	static Semigroup<Integer> intMult =  (a, b) -> a * b;
	/**
     * Combine two Longs by multiplying them
     */
	static Semigroup<Long> longMult =  (a, b) -> a * b;
	/**
     * Combine two Doubles by multiplying them
     */
	static Semigroup<Double> doubleMult =  (a, b) -> a * b;
	/**
     * Combine two BigIntegers by multiplying them
     */
	static Semigroup<BigInteger> bigIntMult =  (a, b) -> a.multiply(b);
	/**
     * Combine two Integers by selecting the max
     */
	static Semigroup<Integer> intMax =  (a, b) -> b > a ? b : a;
	/**
     * Combine two Longs by selecting the max
     */
	static Semigroup<Long> longMax =  (a, b) -> b > a ? b : a;
	/**
     * Combine two Doubles by selecting the max
     */
	static Semigroup<Double> doubleMax = (a, b) -> b > a ? b : a;
	/**
     * Combine two BigIntegers by selecting the max
     */
	static Semigroup<BigInteger> bigIntMax =  (a, b) -> a.max(b);
	/**
     * Combine two Integers by selecting the min
     */
	static Semigroup<Integer> intMin =  (a, b) -> a < b ? a : b;
	/**
     * Combine two Longs by selecting the min
     */
	static Semigroup<Long> longMin =  (a, b) -> a < b ? a : b;
	/**
     * Combine two Doubles by selecting the min
     */
	static Semigroup<Double> doubleMin =  (a, b) -> a < b ? a : b;
	/**
     * Combine two BigIntegers by selecting the min
     */
	static Semigroup<BigInteger> bigIntMin =  (a, b) -> a.min(b);
	/**
	 * String concatenation
	 */
	static Semigroup<String> stringConcat =  (a, b) -> a + b;
	/**
     * StringBuffer concatenation
     */
	static Semigroup<StringBuffer> stringBufferConcat =  (a, b) -> a.append(b);
	/**
     * StringBuilder concatenation
     */
	static Semigroup<StringBuilder> stringBuilderConcat = (a, b) -> a.append(b);
	/**
     * Combine two booleans by OR'ing them (disjunction)
     */
	static Semigroup<Boolean> booleanDisjunction =  (a, b) -> a || b;
	/**
	 * Combine two booleans by XOR'ing them (exclusive disjunction)
	 */
	static Semigroup<Boolean> booleanXDisjunction =  (a, b) -> a && !b || b && !a;
	/**
	 * Combine two booleans by AND'ing them (conjunction)
	 */
	static Semigroup<Boolean> booleanConjunction = (a, b) -> a && b;

}

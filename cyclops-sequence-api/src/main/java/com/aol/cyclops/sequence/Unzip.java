package com.aol.cyclops.sequence;

import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

public class Unzip {
	/**
	 * Unzip a zipped Stream 
	 * 
	 * <pre>
	 * {@code 
	 *  unzip(SequenceM.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c")))
	 *  
	 *  // SequenceM[1,2,3], SequenceM[a,b,c]
	 * }
	 * 
	 * </pre>
	 * 
	 */
	public final static <T,U> Tuple2<SequenceM<T>,SequenceM<U>> unzip(SequenceM<Tuple2<T,U>> sequence){
		Tuple2<SequenceM<Tuple2<T,U>>,SequenceM<Tuple2<T,U>>> tuple2 = sequence.duplicateSequence();
		return new Tuple2(tuple2.v1.map(Tuple2::v1),tuple2.v2.map(Tuple2::v2));
	}
	/**
	 * Unzip a zipped Stream into 3
	 * <pre>
	 * {@code 
	 *    unzip3(SequenceM.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3,"c", 4l)))
	 * }
	 * // SequenceM[1,2,3], SequenceM[a,b,c], SequenceM[2l,3l,4l]
	 * </pre>
	 */
	public final static <T1,T2,T3> Tuple3<SequenceM<T1>,SequenceM<T2>,SequenceM<T3>> unzip3(SequenceM<Tuple3<T1,T2,T3>> sequence){
		Tuple3<SequenceM<Tuple3<T1,T2,T3>>,SequenceM<Tuple3<T1,T2,T3>>,SequenceM<Tuple3<T1,T2,T3>>> tuple3 = sequence.triplicate();
		return new Tuple3(tuple3.v1.map(Tuple3::v1),tuple3.v2.map(Tuple3::v2),tuple3.v3.map(Tuple3::v3));
	}
	/**
	 * Unzip a zipped Stream into 4
	 * 
	 * <pre>
	 * {@code 
	 * unzip4(SequenceM.of(new Tuple4(1, "a", 2l,'z'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
						"c", 4l,'x')));
		}
		// SequenceM[1,2,3], SequenceM[a,b,c], SequenceM[2l,3l,4l], SequenceM[z,y,x]
	 * </pre>
	 */
	public final static <T1,T2,T3,T4> Tuple4<SequenceM<T1>,SequenceM<T2>,SequenceM<T3>,SequenceM<T4>> unzip4(SequenceM<Tuple4<T1,T2,T3,T4>> sequence){
		Tuple4<SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();
		return new Tuple4(quad.v1.map(Tuple4::v1),quad.v2.map(Tuple4::v2),quad.v3.map(Tuple4::v3),quad.v4.map(Tuple4::v4));
	}
}

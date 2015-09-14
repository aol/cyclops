package com.aol.cyclops.functionaljava;

import java.util.Optional;
import java.util.function.Supplier;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.ComprehenderSelector;
import com.aol.cyclops.monad.AnyM;

import fj.P1;
import fj.data.Either;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.IterableW;
import fj.data.List;
import fj.data.Option;
import fj.data.Reader;
import fj.data.State;
import fj.data.Stream;
import fj.data.Validation;
import fj.data.Writer;

/**
 * FunctionalJava Cyclops integration point
 * 
 * @author johnmcclean
 *
 */
public class FJ {
	
	/**
	 * Methods for making working with FJ's Trampoline a little more Java8 friendly
	 *
	 */
	public static class Trampoline{
		/**
		 * 
		 * <pre>
		 * {@code
		 * List<String> list = FJ.anyM(FJ.Trampoline.suspend(() -> Trampoline.pure("hello world")))
								.map(String::toUpperCase)
								.asSequence()
								.toList();
		      // ["HELLO WORLD"]
		 * }
		 * </pre>
		 * 
		 * @param s Suspend using a Supplier
		 * 
		 * @return Next Trampoline stage
		 */
		public static <T> fj.control.Trampoline<T> suspend(Supplier<fj.control.Trampoline<T>> s ){
			return fj.control.Trampoline.suspend(new P1<fj.control.Trampoline<T>>(){

				@Override
				public fj.control.Trampoline<T> _1() {
					return s.get();
				}
			
			});
		}
	}
	
	
	
	/**
	 * Unwrap an AnyM to a Reader
	 * 
	 * <pre>
	 * {@code 
	 *   FJ.unwrapReader(FJ.anyM(Reader.unit( (Integer a) -> "hello "+a ))
						.map(String::toUpperCase))
						.f(10)
	 * 
	 * }
	 * </pre>
	 * 
	 * @param anyM Monad to unwrap
	 * @return unwrapped reader
	 */
	public static final <A,B> Reader<A,B> unwrapReader(AnyM<B> anyM){
		
		Reader unwrapper = Reader.unit(a->1);
		return (Reader)new ComprehenderSelector()
							.selectComprehender(unwrapper)
							.executeflatMap(unwrapper, i-> anyM.unwrap());
		
	}
	/**
	 * <pre>
	 * {@code 
	 * 		FJ.unwrapWriter(FJ.anyM(writer)
				.map(String::toUpperCase),writer)
				.value()
	 * }
	 * </pre>
	 * 
	 * @param anyM AnyM to unwrap to Writer
	 * @param unwrapper Writer of same type to do unwrapping
	 * @return Unwrapped writer
	 */
	public static final <A,B> Writer<A,B> unwrapWriter(AnyM<B> anyM,Writer<B,?> unwrapper){
		
		
		return (Writer)new ComprehenderSelector()
							.selectComprehender(unwrapper)
							.executeflatMap(unwrapper, i-> anyM.unwrap());
		
	}
	/**
	 * <pre>
	 * {@code 
	 * 		FJ.unwrapState(FJ.anyM(State.constant("hello"))
								.map(String::toUpperCase))
								.run("")
								._2()
	 * 
	 * }
	 * </pre>
	 * @param anyM AnyM to unwrap to State monad
	 * @return State monad
	 */
	public static final <A,B> State<A,B> unwrapState(AnyM<B> anyM){
		
		State unwrapper = State.constant(1);
		return (State)new ComprehenderSelector()
							.selectComprehender(unwrapper)
							.executeflatMap(unwrapper, i-> anyM.unwrap());
		
	}
	/**
	 * <pre>
	 * {@code
	 *    FJ.unwrapIO( 
				FJ.anyM(IOFunctions.lazy(a->{ System.out.println("hello world"); return a;}))
				.map(a-> {System.out.println("hello world2"); return a;})   )
				.run();
	 * 
	 * }
	 * </pre>
	 * @param anyM to unwrap to IO Monad
	 * @return IO Monad
	 */
	public static final <B> IO<B> unwrapIO(AnyM<B> anyM){
		
		IO unwrapper = IOFunctions.unit(1);
		return (IO)new ComprehenderSelector()
							.selectComprehender(unwrapper)
							.executeflatMap(unwrapper, i-> anyM.unwrap());
		
	}
	/**
	 * 
	 * <pre>
	 * {@code
	 * FJ.anyM(IOFunctions.lazy(a->{ System.out.println("hello world"); return a;}))
	 * }
	 * </pre>
	 * 
	 * @param ioM Construct an AnyM from the supplied IO Monad
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(IO<T> ioM){
		return AsAnyM.notTypeSafeAnyM(ioM);
	}
	/**
	 * <pre>
	 * {@code
	 * AnyM<String> anyM  = FJ.anyM(State.constant("hello"))
							.map(String::toUpperCase)
		}
		</pre>
	 * @param stateM Construct an AnyM from the supplied State Monad
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(State<?,T> stateM){
		return AsAnyM.notTypeSafeAnyM(stateM);
	}
	/**
	 * <pre>
	 * {@code
	 * FJ.anyM(Validation.success(success()))
			.map(String::toUpperCase)
			.toSequence()
			.toList()
	 * }
	 * 
	 * @param validationM to  construct an AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Validation<?,T> validationM){
		return AsAnyM.notTypeSafeAnyM(validationM);
	}
	
	
	/**
	 * uses the same types keeps things simpler
	 * 
	 * <pre>
	 * {@code
	 * FJ.anyM(Writer.unit("lower", "", Monoid.stringMonoid))
				.flatMap(a->FJ.anyMValue(Writer.unit("hello",Monoid.stringMonoid)))
	 * }
	 * 
	 * @param writerM to construct an AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Writer<T,T> writerM){
		return AsAnyM.notTypeSafeAnyM(writerM);
	}
	/**
	 * <pre>
	 * {@code
	 * FJ.anyMValue(Writer.unit("hello",Monoid.stringMonoid))
	 * }
	 * </pre>
	 * 
	 * @param writerM to construct an AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyMValue(Writer<T,?> writerM){
			return AsAnyM.notTypeSafeAnyM(writerM);
	}
	/**
	 * <pre>
	 * {@code 
	 * 	FJ.anyM(Reader.unit( (Integer a) -> "hello "+a )
	 * }
	 * </pre>
	 * 
	 * Create an AnyM, input type will be ignored, while Reader is wrapped in AnyM
	 * Extract to access and provide input value
	 * 
	 * @param readerM to create AnyM from 
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Reader<?,T> readerM){
		return AsAnyM.notTypeSafeAnyM(readerM);
	}
	/**
	 * <pre>
	 * {@code
	 * FJ.anyM(FJ.Trampoline.suspend(()-> Trampoline.pure(finalStage()))
	 * }
	 * </pre>
	 * 
	 * @param trampolineM to create AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(fj.control.Trampoline<T> trampolineM){
		return AsAnyM.notTypeSafeAnyM(trampolineM);
	}
	/**
	 * <pre>
	 * {@code
	 * FJ.anyM(IterableW.wrap(Arrays.asList("hello world")))
				.map(String::toUpperCase)
				.toSequence()
				.toList()
	 * 
	 * }
	 * 
	 * @param iterableWM to create AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(IterableW<T> iterableWM){
		return AsAnyM.notTypeSafeAnyM(iterableWM);
	}
	/**
	 * (Right biased)
	 * <pre>
	 * {@code 
	 * FJ.anyM(Either.right("hello world"))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList()
	 * }
	 * </pre>
	 * 
	 * @param eitherM to construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Either<?,T> eitherM){
		return AsAnyM.notTypeSafeAnyM(eitherM);
	}
	/**
	 * <pre>
	 * {@code
	 * FJ.anyM(Either.right("hello world").right())
			.map(String::toUpperCase)
			.toSequence()
			.toList()
			
			//[HELLO WORLD]
	 * }</pre>
	 * 
	 * 
	 * @param rM Right projection to construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Either<?,T>.RightProjection<?,T> rM){
		if(rM.toOption().isSome())
			return AsAnyM.notTypeSafeAnyM(Either.right(rM.value()).right());
		else
			return AsAnyM.notTypeSafeAnyM(Optional.empty());
	}
	/**
	 * <pre>
	 * {@code
	 *  FJ.anyM(Either.<String,String>left("hello world").left())
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList() 
	 * }
	 * //[HELLO WORLD]
	 * </pre>
	 * 
	 * @param lM Left Projection to construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Either<T,?>.LeftProjection<T,?> lM){
		if(lM.toOption().isSome()) //works in the opposite way to javaslang
			return AsAnyM.notTypeSafeAnyM(Either.right(lM.value()).right());
		else
			return AsAnyM.notTypeSafeAnyM(Optional.empty());
	}
	/**
	 * <pre>
	 * {@code
	 * FJ.anyM(Option.some("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList()
	 * }
	 * //[HELLO WORLD]
	 * </pre>
	 * 
	 * @param optionM to construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Option<T> optionM){
		return AsAnyM.notTypeSafeAnyM(optionM);
	}
	/**
	 * <pre>
	 * {@code
	 * FJ.anyM(Stream.stream("hello world"))
				.map(String::toUpperCase)
				.flatMap(i->AnyMonads.anyM(java.util.stream.Stream.of(i)))
				.toSequence()
				.toList()
	 * }
	 *  //[HELLO WORLD]
	 * </pre>
	 * 
	 * @param streamM to construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Stream<T> streamM){
		return AsAnyM.notTypeSafeAnyM(streamM);
	}
	/**
	 * <pre>
	 * {@code 
	 * FJ.anyM(List.list("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList()
	 * }
	 * </pre>
	 * @param listM to Construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(List<T> listM){
		return AsAnyM.notTypeSafeAnyM(listM);
	}
}

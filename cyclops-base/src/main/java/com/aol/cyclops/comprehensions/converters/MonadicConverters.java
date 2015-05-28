package com.aol.cyclops.comprehensions.converters;

import java.util.ServiceLoader;

import lombok.Getter;
import lombok.val;

import org.jooq.lambda.Seq;
import org.pcollections.PStack;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.api.Reducers;
import com.aol.cyclops.streams.StreamUtils;

public class MonadicConverters {
	

	private final  StreamUpscaler upscaler = getConverter();
	private static StreamUpscaler getConverter() {
		try{
			Class.forName("org.jooq.lambda.Seq");
			return stream -> Seq.seq(stream);
		}catch(ClassNotFoundException e){
			return  stream -> stream;
		}
		
	}
	private final static PStack<MonadicConverter> converters;
	//public static final PStack<MonadicConverter> defaultList;
	/** = ConsPStack.<MonadicConverter>singleton(new CollectionToStreamConverter())
						.plus(new DecomposableToStreamConverter())
						.plus(new OptionalDoubleToOptionalConverter())
						.plus(new OptionalIntToOptionalConverter())
						.plus(new OptionalLongToOptionalConverter())
						.plus(new NullToOptionalConverter())
						.plus(new CallableToCompletableFutureConverter())
						.plus(new StringToStreamConverter())
						.plus(new IntegerToRangeConverter())
						.plus(new FileToStreamConverter())
						.plus(new URLToStreamConverter())
						.plus(new InputStreamToStreamConverter())
						.plus(new BufferedReaderToStreamConverter())
						.plus(new ResultsetToStreamConverter())
						.plus(new SupplierToCompletableFutureConverter ())
						.plus(new ArrayToStreamConverter())
						.plus(new EnumToStreamConverter())
						.plus(new IteratorToStreamConverter())
						.plus(new StreamableToStreamConverter());**/
	
	static {
		val loader  = ServiceLoader.load(MonadicConverter.class);
		converters = Reducers.<MonadicConverter>toPStack().mapReduce(StreamUtils.stream(loader.iterator()).sorted((a,b) -> a.priority() - b.priority()));
		System.out.println(converters);
	}
	
	
	//Supplier[] Callable[]  to LazyFutureStream
	//CompletableFuture[] to EagerFutureStream
	//CheckedSupplier to Try
	//CheckedSupplier[]  to Seq of Try
	//async.Queue, async.Topic, async.Signal to Seq / Stream
	
	
	
	
	
	public Object convertToMonadicForm(Object o){
		return upscaler.upscaleIfStream(converters.stream().filter(t-> t.accept(o)).map(m -> m.convertToMonadicForm(o)).findFirst().orElse(o));
	}
	
}

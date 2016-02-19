package com.aol.cyclops.internal.comprehensions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PStack;

import com.aol.cyclops.internal.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.internal.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.internal.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.types.extensability.Comprehender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public interface ComprehensionsModule {

	static final class BaseComprehensionData {

		private ContextualExecutor delegate;
		private ContextualExecutor currentContext;

		public BaseComprehensionData(ExecutionState state) {

			this.delegate = state.contextualExecutor;

		}

		public <R extends BaseComprehensionData> R guardInternal(Supplier<Boolean> s) {
			((Foreach) delegate.getContext())
					.addExpansion(new Filter("guard", new ContextualExecutor(delegate.getContext()) {
						public Object execute() {
							currentContext = this;
							return s.get();
						}
					}));
			return (R) this;
		}

		public void run(Runnable r) {
			yieldInternal(() -> {
				r.run();
				return null;
			});
		}

		public <R> R yieldInternal(Supplier s) {
			return (R) ((Foreach) delegate.getContext())
					.yield(new ExecutionState(new ContextualExecutor(delegate.getContext()) {
						public Object execute() {
							currentContext = this;
							return s.get();
						}
					}));

		}

		public <T> T $Internal(String property) {
			Object delegate = currentContext.getContext();

			return (T) ((Map) delegate).get(property);

		}

		public <R extends BaseComprehensionData> R $Internal(String name, Object f) {
			Expansion g = new Expansion(name, new ContextualExecutor(this) {
				public Object execute() {
					currentContext = this;
					return unwrapSupplier(f);
				}

				private Object unwrapSupplier(Object f) {
					if (f instanceof Supplier)
						return ((Supplier) f).get();
					return f;
				}

			});

			((Foreach) delegate.getContext()).addExpansion(g);

			return (R) this;
		}

	}

	/**
	 * Class that collects data for free form for comprehensions
	 * 
	 * @author johnmcclean
	 *
	 * @param <T>
	 *            Variable type
	 * @param <R>
	 *            Return type
	 * @param <V>
	 *            Aggregate Variable type holder
	 */
	public static class ComprehensionData<T, R, V extends Initialisable> {
		private final BaseComprehensionData data;
		@Getter
		private final V vars;

		private final Proxier proxier = new Proxier();

		ComprehensionData(ExecutionState state, Optional<Class<V>> varsClass) {
			super();
			data = new BaseComprehensionData(state);

			this.vars = (V) new Varsonly();
			this.vars.init(data);
		}

		ComprehensionData(V vars, ExecutionState state) {
			super();
			data = new BaseComprehensionData(state);

			this.vars = (V) ((Initialisable) vars);
			this.vars.init(data);

		}

		/**
		 * Add a guard to the for comprehension
		 * 
		 * <pre>
		 * {@code
		 *  	foreachX(c -> c.$("hello",list)
							   .filter(()->c.<Integer>$("hello")<10)
								.yield(()-> c.<Integer>$("hello")+2));
			  }
		 * </pre>
		 * 
		 * @param s
		 *            Supplier that returns true for elements that should stay
		 *            in the comprehension
		 * @return this
		 */
		public ComprehensionData<T, R, V> filter(Supplier<Boolean> s) {
			data.guardInternal(s);
			return this;

		}

		ComprehensionData<T, R, V> filterFunction(Function<V, Boolean> s) {
			data.guardInternal(() -> s.apply(vars));
			return this;

		}

		/**
		 * Define the yeild section of a for comprehension and kick of
		 * processing for a comprehension
		 * 
		 * <pre>
		 * {@code
		 *  	foreachX(c -> c.$("hello",list)
							   .filter(()->c.<Integer>$("hello")<10)
								.yield(()-> c.<Integer>$("hello")+2));
			  }
		 * </pre>
		 * 
		 * @param s
		 *            Yield section
		 * @return result of for comprehension
		 */
		public <R> R yield(Supplier s) {
			return data.yieldInternal(s);

		}

		<R> R yieldFunction(Function<V, ?> s) {
			return data.yieldInternal(() -> s.apply(vars));

		}

		/**
		 * Extract a bound variable
		 * 
		 * <pre>
		 * {@code
		 *  	foreachX(c -> c.$("hello",list)
							   .filter(()->c.<Integer>$("hello")<10)
								.yield(()-> c.<Integer>$("hello")+2));
			  }
		 * </pre>
		 * 
		 * 
		 * @param name
		 *            Variable name
		 * @return variable value
		 */
		public <T> T $(String name) {
			return data.$Internal(name);

		}

		/**
		 * Bind a variable in this for comprehension
		 * 
		 * <pre>
		 * {@code
		 *  	foreachX(c -> c.$("hello",list)
							   .filter(()->c.<Integer>$("hello")<10)
								.yield(()-> c.<Integer>$("hello")+2));
			  }
		 * </pre>
		 * 
		 * @param name
		 *            of variable to bind
		 * @param f
		 *            value
		 * @return this
		 */
		public <T> ComprehensionData<T, R, V> $(String name, Object f) {
			data.$Internal(name, f);

			return (ComprehensionData) this;
		}

		/**
		 * Lazily bind a variable in this for comprehension
		 * 
		 * <pre>
		 * {@code
		 *  	foreachX(c -> c.$("hello",list)
							   .filter(()->c.<Integer>$("hello")<10)
								.yield(()-> c.<Integer>$("hello")+2));
			  }
		 * </pre>
		 * 
		 * @param name
		 *            name of variable to bind
		 * @param f
		 *            value
		 * @return this
		 */
		public <T> ComprehensionData<T, R, V> $(String name, Supplier f) {
			data.$Internal(name, f);

			return (ComprehensionData) this;
		}

	}

	@AllArgsConstructor
	@Getter
	abstract static class ContextualExecutor<T, C> {

		private volatile C context;

		public T executeAndSetContext(C context) {
			this.context = context;
			return execute();

		}

		public abstract T execute();
	}

	@Getter
	@AllArgsConstructor
	static class Expansion {

		private final String name;
		private final ContextualExecutor function;

	}

	/**
	 * Extend this interface to implement custom For Comprehensions
	 * 
	 * Format is
	 * 
	 * to bind a Monad or colleciton use methods in the following format
	 * 
	 * varName(Monad m);
	 * 
	 * to access the current value in a filter or yield expression
	 * 
	 * varName();
	 *
	 * e.g.
	 * 
	 * <pre>
	 * static interface Custom extends CustomForComprehension&lt;Stream&lt;Integer&gt;, Custom&gt; {
	 * 	Integer myVar();
	 * 
	 * 	Custom myVar(List&lt;Integer&gt; value);
	 * }
	 * </pre>
	 *
	 * <pre>
	 * Stream&lt;Integer&gt; stream = ForComprehensions.foreachX(Custom.class,
	 * 		c -&gt; c.myVar(list).yield(() -&gt; c.myVar() + 3));
	 * 
	 * </pre>
	 * 
	 * 
	 * 
	 * @author johnmcclean
	 *
	 * @param <R>
	 *            Return type of the end result
	 * @param <T>
	 *            The type of the Custom For Comprehension (the interface that
	 *            extends this one).
	 */
	public static interface CustomForComprehension<R, T> {

		public T filter(Supplier<Boolean> s);

		public R yield(Supplier s);

		public void run(Runnable r);
	}

	@AllArgsConstructor
	final static class ExecutionState<T1, T2, V> {

		public final ContextualExecutor<T1, T2> contextualExecutor;

	}

	static class Filter extends Expansion {

		public Filter(String name, ContextualExecutor func) {
			super(name, func);
		}

	}

	@AllArgsConstructor
	static class Yield<T> {

		private final List<Expansion> expansions;
		private final MonadicConverters converters = new MonadicConverters();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		T process(ContextualExecutor<?, Map> yieldExecutor, PMap<String, Object> context,
				Object currentExpansionUnwrapped, String lastExpansionName, int index) {

			Tuple2<Comprehender, Object> comprehender = selectComprehender(
					currentExpansionUnwrapped)
							.orElseGet(
									() -> selectComprehender(
											converters
													.convertToMonadicForm(
															currentExpansionUnwrapped))
																	.orElse(new Tuple2(
																			new InvokeDynamicComprehender(Optional
																					.ofNullable(
																							currentExpansionUnwrapped)
																					.map(Object::getClass)),
																			currentExpansionUnwrapped)));

			if (expansions.size() == index) {

				return (T) comprehender._1.map(comprehender._2,
						it -> yieldExecutor.executeAndSetContext(context.plus(lastExpansionName, it)));

			} else {
				Expansion head = expansions.get(index);

				if (head instanceof Filter) {

					Object s = comprehender._1.filter(comprehender._2, it -> (boolean) head.getFunction()
							.executeAndSetContext(context.plus(lastExpansionName, it)));
					return process(yieldExecutor, context, s, lastExpansionName, index + 1);
				} else {

					T result = (T) comprehender._1.executeflatMap(comprehender._2, it -> {
						PMap newMap = context.plus(lastExpansionName, it);
						return process((ContextualExecutor) yieldExecutor, newMap,
								head.getFunction().executeAndSetContext(newMap), head.getName(), index + 1);
					});
					try {
						return (T) comprehender._1.map(result, this::takeFirst);

					} catch (Goto g) {
						return (T) comprehender._1.empty();
					}

				}

			}
		}

		private static class Goto extends RuntimeException {

			@Override
			public synchronized Throwable fillInStackTrace() {
				return null;
			}

		}

		private <T> T takeFirst(Object o) {
			if (o instanceof MaterializedList) {
				if (((List) o).size() == 0)
					throw new Goto();

				return (T) ((List) o).get(0);
			}
			return (T) o;
		}

		@AllArgsConstructor
		static class Tuple2<T1, T2> {
			final T1 _1;
			final T2 _2;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Optional<Tuple2<Comprehender, Object>> selectComprehender(Object structure) {
			if (structure == null)
				return Optional.empty();
			return new Comprehenders().getRegisteredComprehenders().stream()
					.filter(e -> e.getKey().isAssignableFrom(structure.getClass())).map(e -> e.getValue())
					.map(v -> new Tuple2<Comprehender, Object>(v, structure)).findFirst();
		}

	}

	public static class Varsonly<T1, T2, T3, T4, T5> implements Initialisable {
		@Setter
		private BaseComprehensionData data;

		public void init(BaseComprehensionData data) {
			this.data = data;

		}

		public <T> T $(String name) {
			return data.$Internal(name);

		}

		public T1 $1() {
			return data.$Internal("$1");

		}

		public T2 $2() {
			return data.$Internal("$2");

		}

		public T3 $3() {
			return data.$Internal("$3");

		}

		public T4 $4() {
			return data.$Internal("$4");

		}

		public T5 $5() {
			return data.$Internal("$5");

		}
	}

@AllArgsConstructor
public static class MyComprehension<X,V>{
	private final Class<X> c;
	private final Class<V> vars;
	public <R> R foreach(Function<X,R> fn){
		return (R)new FreeFormForComprehension(c,vars).foreach(fn);
	}
}
public static interface Initialisable {
	public void init(BaseComprehensionData data);
}

static class Foreach<T> {

	private PStack<Expansion> generators = ConsPStack.empty();

	
	
	public T yield(ExecutionState state) {
		Expansion head = generators.get(0);
		return new Yield<T>(generators)
				.process(state.contextualExecutor, HashTreePMap.empty(), head
				.getFunction().executeAndSetContext(HashTreePMap.empty()), head
				.getName(), 1);
	}

	void addExpansion(Expansion g) {
		generators = generators.plus(generators.size(),g);
	}

	public static<T> T foreach(ContextualExecutor<T,Foreach<T>> comprehension) {

		return comprehension.executeAndSetContext(new Foreach<>());
	}

}

}

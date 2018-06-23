package com.oath.cyclops.jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.companion.Streamable;
import cyclops.data.*;
import cyclops.reactive.ReactiveSeq;

import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class IterableXDeserializer extends StdDeserializer<IterableX<?>> {
  private final Class<?> elementType;
  private final Class<?> itX;


  public IterableXDeserializer(Class<?> vc, Class<?> elementType) {
    super(vc);
    this.itX = vc;
    this.elementType = elementType;
  }

  @Override
  public IterableX<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {


    JsonDeserializer deser = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructArrayType(Object.class));

    Object o = deser.deserialize(p, ctxt);

    if(Vector.class.isAssignableFrom(elementType))
      return Vector.of((Object[])o);

    if(Seq.class.isAssignableFrom(elementType))
      return Seq.of((Object[])o);
    if(LazySeq.class.isAssignableFrom(elementType))
      return LazySeq.of((Object[])o);
    if(LazyString.class.isAssignableFrom(elementType))
      return LazyString.fromLazySeq((LazySeq)LazySeq.of((Object[])o));
    if(IntMap.class.isAssignableFrom(elementType))
      return IntMap.of((Object[])o);
    if(ReactiveSeq.class.isAssignableFrom(elementType))
      return ReactiveSeq.of((Object[])o);
    if(Streamable.class.isAssignableFrom(elementType))
      return Streamable.of((Object[])o);
    if(BankersQueue.class.isAssignableFrom(elementType))
      return BankersQueue.of((Object[])o);
    if(Bag.class.isAssignableFrom(elementType))
      return Bag.of((Object[])o);
    if(cyclops.data.HashSet.class.isAssignableFrom(elementType))
      return HashSet.of((Object[])o);
    if(cyclops.data.TrieSet.class.isAssignableFrom(elementType))
      return TrieSet.of((Object[])o);
    if(cyclops.data.TreeSet.class.isAssignableFrom(elementType))
      return TreeSet.of((Comparator)Comparator.naturalOrder(),(Object[])o);

    Optional<Method> m = streamMethod.computeIfAbsent(itX, c->Stream.of(c.getMethods())
      .filter(method -> "of".equals(method.getName()))
      .filter(method -> method.getParameterCount()==1)
      .filter(method -> method.getParameterTypes()[0].isArray()).findFirst()
      .map(m2->{ m2.setAccessible(true); return m2;}));
    IterableX x = m.map(mt -> (IterableX) new Invoker().executeMethod(o, mt, itX)).orElse(null);

    return x;

  }
  private static final Map<Class,Optional<Method>> streamMethod = new ConcurrentHashMap<>();
  private static final Map<Method,CallSite> callSites = new ConcurrentHashMap<>();
  static class Invoker{
    private Object executeMethod(Object t, Method m, Class z) {
      try {
        return callSites.computeIfAbsent(m, (m2) ->  {
          try {
            return new ConstantCallSite(MethodHandles.publicLookup().unreflect(m2));
          } catch (Exception e) {
            throw ExceptionSoftener.throwSoftenedException(e);
          }
        }).dynamicInvoker().invoke((Object[])t);

      } catch (Throwable e) {
        throw ExceptionSoftener.throwSoftenedException(e);
      }
    }
  }
}

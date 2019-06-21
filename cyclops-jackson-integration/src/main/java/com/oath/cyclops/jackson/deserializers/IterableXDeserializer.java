package com.oath.cyclops.jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class IterableXDeserializer extends StdDeserializer<IterableX<?>> implements ContextualDeserializer {
  private final Class<?> elementType;
  private final Class<?> itX;
  private final JsonDeserializer<?> valueDeserializer;
  private final TypeDeserializer typeDeserializerForValue;
  private final CollectionLikeType type;
  public IterableXDeserializer(Class<?> vc, Class<?> elementType,TypeDeserializer typeDeserializerForValue, JsonDeserializer<?> valueDeserializer,CollectionLikeType type) {
    super(vc);
    this.itX = vc;
    this.elementType = elementType;
    this.valueDeserializer = valueDeserializer;
    this.typeDeserializerForValue = typeDeserializerForValue;
    this.type = type;
  }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                BeanProperty property) throws JsonMappingException
    {
        JsonDeserializer<?> deser = this.valueDeserializer;
        TypeDeserializer typeDeser = this.typeDeserializerForValue;
        if (deser == null) {
            deser = ctxt.findContextualValueDeserializer(type.getContentType(), property);
        }
        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }

        return new IterableXDeserializer(elementType,itX,typeDeser, deser,type);
    }
  @Override
  public IterableX<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {


      if (!p.isExpectedStartArrayToken()) {
          return (IterableX)ctxt.handleUnexpectedToken(handledType(),p);
      }

    List multi = new ArrayList();

      JsonToken t;
      while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
          Object value;

          if (t == JsonToken.VALUE_NULL) {
              value = null;
          } else if (typeDeserializerForValue == null) {
              value = valueDeserializer.deserialize(p, ctxt);
          } else {
              value = valueDeserializer.deserializeWithType(p, ctxt, typeDeserializerForValue);
          }
          multi.add(value);
      }

    if(Vector.class.isAssignableFrom(elementType))
      return Vector.fromIterable(multi);

    if(Seq.class.isAssignableFrom(elementType))
      return Seq.fromIterable(multi);
    if(LazySeq.class.isAssignableFrom(elementType))
      return LazySeq.fromIterable(multi);
    if(LazyString.class.isAssignableFrom(elementType))
      return LazyString.fromLazySeq((LazySeq)LazySeq.fromIterable(multi));
    if(IntMap.class.isAssignableFrom(elementType))
      return IntMap.fromIterable(multi);
    if(ReactiveSeq.class.isAssignableFrom(elementType))
      return ReactiveSeq.fromIterable(multi);
    if(Streamable.class.isAssignableFrom(elementType))
      return Streamable.fromIterable(multi);
    if(BankersQueue.class.isAssignableFrom(elementType))
      return BankersQueue.fromIterable(multi);
    if(Bag.class.isAssignableFrom(elementType))
      return Bag.fromIterable(multi);
    if(cyclops.data.HashSet.class.isAssignableFrom(elementType))
      return HashSet.fromIterable(multi);
    if(cyclops.data.TrieSet.class.isAssignableFrom(elementType))
      return TrieSet.fromIterable(multi);
    if(cyclops.data.TreeSet.class.isAssignableFrom(elementType))
      return TreeSet.fromIterable(multi,(Comparator)Comparator.naturalOrder());

    Optional<Method> m = streamMethod.computeIfAbsent(elementType, c->Stream.of(c.getMethods())
                                                                      .filter(method -> "fromIterable".equals(method.getName()))
                                                                      .filter(method -> method.getParameterCount()==1)
                                                                        .findFirst()
                                                                      .map(m2->{ m2.setAccessible(true); return m2;}));
    IterableX x = m.map(mt -> (IterableX) new Invoker().executeMethod(multi, mt, itX)).orElse(null);

    return x;

  }
  private static final Map<Class,Optional<Method>> streamMethod = new ConcurrentHashMap<>();
  private static final Map<Method,CallSite> callSites = new ConcurrentHashMap<>();
  static class Invoker{
    private Object executeMethod(Iterable t, Method m, Class z) {
      try {
        return callSites.computeIfAbsent(m, (m2) ->  {
          try {
            return new ConstantCallSite(MethodHandles.publicLookup().unreflect(m2));
          } catch (Exception e) {
            throw ExceptionSoftener.throwSoftenedException(e);
          }
        }).dynamicInvoker().invoke(t);

      } catch (Throwable e) {
        throw ExceptionSoftener.throwSoftenedException(e);
      }
    }
  }
}

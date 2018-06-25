package com.oath.cyclops.jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.oath.cyclops.types.persistent.PersistentMap;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.data.HashMap;
import cyclops.data.TreeMap;

import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class PersistentMapDeserializer extends StdDeserializer<PersistentMap<?,?>> {

  private final Class<?> mapType;

  public PersistentMapDeserializer(Class<?> vc) {
    super(vc);
    this.mapType = vc;
  }

  @Override
  public PersistentMap<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonDeserializer deser = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructRawMapType(Map.class));

    Object o = deser.deserialize(p, ctxt);
    if(HashMap.class.isAssignableFrom(mapType))
      return HashMap.fromMap((Map)o);
    if(TreeMap.class.isAssignableFrom(mapType))
      return TreeMap.fromMap((Comparator)Comparator.naturalOrder(),(Map)o);

   /** if(TrieMap.class.isAssignableFrom(mapType))
      return TrieMap.fromMap((Map)o);
    if(LinkedMap.class.isAssignableFrom(mapType))
      return LinkedMap.fromMap((Map)o);
    **/
    Optional<Method> m = streamMethod.computeIfAbsent(mapType, c-> Stream.of(c.getMethods())
      .filter(method -> "fromMap".equals(method.getName()))
      .filter(method -> method.getParameterCount()==1)
      .filter(method -> method.getParameterTypes()[0].isAssignableFrom(Map.class)).findFirst()
      .map(m2->{ m2.setAccessible(true); return m2;}));
    PersistentMap<?,?> x = m.map(mt -> (PersistentMap)new  Invoker().executeMethod(o, mt, mapType)).orElse(null);

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

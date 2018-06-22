package com.oath.cyclops.jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.data.*;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;

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

public class TupleDeserializer extends StdDeserializer<Object> {



  public TupleDeserializer(Class<?> vc) {
    super(vc);

  }

  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {


    JsonDeserializer deser = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructArrayType(Object.class));

    Object[] o = (Object[])deser.deserialize(p, ctxt);

    if(o.length==0)
      return Tuple.empty();
    if(o.length==1)
      return Tuple.tuple(o[0]);
    if(o.length==2)
      return Tuple.tuple(o[0],o[1]);
    if(o.length==3)
      return Tuple.tuple(o[0],o[1],o[2]);
    if(o.length==4)
      return Tuple.tuple(o[0],o[1],o[2],o[3]);
    if(o.length==5)
      return Tuple.tuple(o[0],o[1],o[2],o[3],o[4]);
    if(o.length==6)
      return Tuple.tuple(o[0],o[1],o[2],o[3],o[4],o[5]);
    if(o.length==7)
      return Tuple.tuple(o[0],o[1],o[2],o[3],o[4],o[5],o[6]);
    if(o.length==8)
      return Tuple.tuple(o[0],o[1],o[2],o[3],o[4],o[5],o[6],o[7]);
    throw new ArrayIndexOutOfBoundsException("Max tuple length exceeded");

  }

}

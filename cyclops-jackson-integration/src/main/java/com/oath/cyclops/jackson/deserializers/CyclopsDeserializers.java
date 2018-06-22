package com.oath.cyclops.jackson.deserializers;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.oath.cyclops.jackson.deserializers.OptionDeserializer;
import com.oath.cyclops.types.persistent.PersistentMap;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.*;
import cyclops.data.tuple.*;

import java.util.HashSet;
import java.util.Set;

public class CyclopsDeserializers  extends Deserializers.Base {

  private Set<Class> tuples = new HashSet<>();
  {
    tuples.add(Tuple0.class);
    tuples.add(Tuple1.class);
    tuples.add(Tuple2.class);
    tuples.add(Tuple3.class);
    tuples.add(Tuple4.class);
    tuples.add(Tuple5.class);
    tuples.add(Tuple6.class);
    tuples.add(Tuple7.class);
    tuples.add(Tuple8.class);
  }
  @Override
  public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    Class<?> raw = type.getRawClass();

    if (raw == Maybe.class) {
      return new MaybeDeserializer(type);
    }
    if (raw == Option.class) {
      return new OptionDeserializer(type);
    }
    if (raw == Eval.class) {
      return new EvalDeserializer(type);
    }
    if (raw == Future.class) {
      return new EvalDeserializer(type);
    }
    if (raw == Ior.class) {
      return new IorDeserializer(type);
    }
    if (raw == LazyEither.class) {
      return new LazyEitherDeserializer(type);
    }
    if (raw == LazyEither3.class) {
      return new LazyEither3Deserializer(type);
    }
    if (raw == LazyEither4.class) {
      return new LazyEither4Deserializer(type);
    }

    if (raw == Either.class) {
      return new EitherDeserializer(type);
    }
    if (raw == Trampoline.class) {
      return new TrampolineDeserializer(type);
    }
    if (raw == Unrestricted.class) {
      return new TrampolineDeserializer(type);
    }
    if(tuples.contains(raw)) {
      return new TupleDeserializer(raw);
    }
    if (IterableX.class.isAssignableFrom(type.getRawClass())) {
      return new IterableXDeserializer(raw,type.containedTypeOrUnknown(0).getRawClass());
    }
    if (PersistentMap.class.isAssignableFrom(type.getRawClass())) {
      return new PersistentMapDeserializer(raw);
    }
    return super.findBeanDeserializer(type, config, beanDesc);
  }

  @Override
  public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
    Class<?> raw = type.getRawClass();
    if (IterableX.class.isAssignableFrom(type.getRawClass())) {
      return new IterableXDeserializer(raw,type.containedTypeOrUnknown(0).getRawClass());
    }
    return super.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
  }

  @Override
  public JsonDeserializer<?> findReferenceDeserializer(ReferenceType type,
                                                       DeserializationConfig config, BeanDescription bean,
                                                       TypeDeserializer typeDeserializer, JsonDeserializer<?> jsonDeserializer) throws JsonMappingException {

    return super.findReferenceDeserializer(type, config, bean, typeDeserializer, jsonDeserializer);
  }

}

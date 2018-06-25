package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.std.SerializableSerializer;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.matching.Sealed3;
import com.oath.cyclops.matching.Sealed4;
import com.oath.cyclops.matching.Sealed5;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.persistent.PersistentMap;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.*;
import cyclops.data.tuple.*;

public class CyclopsSerializers extends Serializers.Base {


  @Override
  public JsonSerializer<?> findReferenceSerializer(SerializationConfig config, ReferenceType type, BeanDescription beanDesc, TypeSerializer contentTypeSerializer, JsonSerializer<Object> contentValueSerializer) {
    if (Option.class.isAssignableFrom(type.getRawClass())) {
      return new OptionSerializer(type,true,contentTypeSerializer,contentValueSerializer);
    }
    if (Eval.class.isAssignableFrom(type.getRawClass())) {
      return new EvalSerializer(type,true,contentTypeSerializer,contentValueSerializer);
    }
    if (Trampoline.class.isAssignableFrom(type.getRawClass())) {
      return new TrampolineSerializer(type,true,contentTypeSerializer,contentValueSerializer);
    }
    if (Ior.class.isAssignableFrom(type.getRawClass())) {
      return new IorSerializer();
    }
    if (Sealed2.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed2Serializer();
    }
    if (Sealed3.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed3Serializer();
    }
    if (Sealed4.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed4Serializer();
    }
    if (Sealed5.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed5Serializer();
    }
    if (Value.class.isAssignableFrom(type.getRawClass())) {
      return new ValueSerializer(type,true,contentTypeSerializer,contentValueSerializer);
    }

    return super.findReferenceSerializer(config, type, beanDesc, contentTypeSerializer, contentValueSerializer);
  }

  @Override
  public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {

    return super.findCollectionLikeSerializer(config, type, beanDesc, elementTypeSerializer, elementValueSerializer);
  }


  @Override
  public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {

    if (Tuple1.class==type.getRawClass()) {
      return new Tuple1Serializer();
    }
    if (Tuple2.class==type.getRawClass()) {
      return new Tuple2Serializer();
    }
    if (Tuple3.class==type.getRawClass()) {
      return new Tuple3Serializer();
    }
    if (Tuple4.class==type.getRawClass()) {
      return new Tuple4Serializer();
    }
    if (Tuple5.class==type.getRawClass()) {
      return new Tuple5Serializer();
    }
    if (Tuple6.class==type.getRawClass()) {
      return new Tuple6Serializer();
    }
    if (Tuple7.class==type.getRawClass()) {
      return new Tuple7Serializer();
    }
    if (Tuple8.class==type.getRawClass()) {
      return new Tuple8Serializer();
    }
    if(PersistentMap.class.isAssignableFrom(type.getRawClass())) {
      return new PersistentMapSerializer();
    }
    if (IterableX.class.isAssignableFrom(type.getRawClass())) {
      return new IterableXSerializer();
    }
    if (Either.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed2Serializer();
    }

    if (Sealed2.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed2Serializer();
    }
    if (Sealed3.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed3Serializer();
    }
    if (Sealed4.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed4Serializer();
    }
    if (Sealed5.class.isAssignableFrom(type.getRawClass())) {
      return new Sealed5Serializer();
    }
    return super.findSerializer(config, type, beanDesc);
  }
}

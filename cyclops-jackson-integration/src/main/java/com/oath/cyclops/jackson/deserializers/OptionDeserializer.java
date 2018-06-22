package com.oath.cyclops.jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import cyclops.control.Option;

import java.io.IOException;

final class OptionDeserializer extends StdDeserializer<Option<?>> implements ResolvableDeserializer {
  JavaType valueType;
  private JsonDeserializer<Object> deser;

  protected OptionDeserializer(JavaType valueType) {
    super(valueType);
    this.valueType = valueType;
  }

  @Override
  public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return super.deserializeWithType(p, ctxt, typeDeserializer);
  }


  @Override
  public Option<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    return Option.some(deser.deserialize(p,ctxt));
  }
  @Override
  public void resolve(DeserializationContext ctxt) throws JsonMappingException {
    if (valueType.isContainerType()) {
       deser = ctxt.findRootValueDeserializer(valueType.getContentType());
    }else{
      deser = ctxt.findRootValueDeserializer(valueType.containedTypeOrUnknown(0));
    }
  }
  @Override
  public Option<?> getNullValue(DeserializationContext ctxt) {
    return Option.none();
  }




}

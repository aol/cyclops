package com.oath.cyclops.jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import cyclops.control.Either;
import cyclops.control.Ior;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;

final class IorDeserializer extends StdDeserializer<Ior<?,?>>  {

  protected IorDeserializer(JavaType valueType) {
    super(valueType);

  }

  @Override
  public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return super.deserializeWithType(p, ctxt, typeDeserializer);
  }

  @AllArgsConstructor @NoArgsConstructor
  public static class IorBean{

    @Getter @Setter
    private  Object left;
    @Getter @Setter
    private  Object right;
  }

  @Override
  public Ior<?,?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonDeserializer ser = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructSimpleType(IorBean.class, new JavaType[0]));
    IorBean x = (IorBean)ser.deserialize(p, ctxt);
    if(x.left!=null && x.right==null){
      return Ior.left(x.left);
    }
    if(x.left==null && x.right!=null){
      return Ior.right(x.right);
    }
    return Ior.both(x.left,x.right);
  }





}

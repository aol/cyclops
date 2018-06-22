package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import cyclops.control.Option;

import java.io.IOException;
import java.util.Optional;

public class OptionSerializer extends ReferenceTypeSerializer<Option<?>> {

  private static final long serialVersionUID = 1L;


  protected OptionSerializer(ReferenceType fullType, boolean staticTyping,
                               TypeSerializer vts, JsonSerializer<Object> ser)
  {
    super(fullType, staticTyping, vts, ser);
  }

  protected OptionSerializer(OptionSerializer base, BeanProperty property,
                               TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper,
                               Object suppressableValue, boolean suppressNulls)
  {
    super(base, property, vts, valueSer, unwrapper,
      suppressableValue, suppressNulls);
  }

  @Override
  protected ReferenceTypeSerializer<Option<?>> withResolved(BeanProperty prop,
                                                            TypeSerializer vts, JsonSerializer<?> valueSer,
                                                            NameTransformer unwrapper)
  {
    return new OptionSerializer(this, prop, vts, valueSer, unwrapper,
      _suppressableValue, _suppressNulls);
  }

  @Override
  public ReferenceTypeSerializer<Option<?>> withContentInclusion(Object suppressableValue,
                                                                   boolean suppressNulls)
  {
    return new OptionSerializer(this, _property, _valueTypeSerializer,
      _valueSerializer, _unwrapper,
      suppressableValue, suppressNulls);
  }

    /*
    /**********************************************************
    /* Abstract method impls
    /**********************************************************
     */

  @Override
  protected boolean _isValuePresent(Option<?> value) {
    return value.isPresent();
  }

  @Override
  protected Object _getReferenced(Option<?> value) {
    return value.orElse(null);
  }

  @Override
  protected Object _getReferencedIfPresent(Option<?> value) {
    return  value.orElse(null);
  }

}

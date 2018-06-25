package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.oath.cyclops.types.Value;
import cyclops.control.Eval;

public class ValueSerializer extends ReferenceTypeSerializer<Value<?>> {

  private static final long serialVersionUID = 1L;


  protected ValueSerializer(ReferenceType fullType, boolean staticTyping,
                            TypeSerializer vts, JsonSerializer<Object> ser)
  {
    super(fullType, staticTyping, vts, ser);
  }

  protected ValueSerializer(ValueSerializer base, BeanProperty property,
                            TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper,
                            Object suppressableValue, boolean suppressNulls)
  {
    super(base, property, vts, valueSer, unwrapper,
      suppressableValue, suppressNulls);
  }

  @Override
  protected ReferenceTypeSerializer<Value<?>> withResolved(BeanProperty prop,
                                                            TypeSerializer vts, JsonSerializer<?> valueSer,
                                                            NameTransformer unwrapper)
  {
    return new ValueSerializer(this, prop, vts, valueSer, unwrapper,
      _suppressableValue, _suppressNulls);
  }

  @Override
  public ReferenceTypeSerializer<Value<?>> withContentInclusion(Object suppressableValue,
                                                                   boolean suppressNulls)
  {
    return new ValueSerializer(this, _property, _valueTypeSerializer,
      _valueSerializer, _unwrapper,
      suppressableValue, suppressNulls);
  }

    /*
    /**********************************************************
    /* Abstract method impls
    /**********************************************************
     */

  @Override
  protected boolean _isValuePresent(Value<?> value) {
    return value.isPresent();
  }

  @Override
  protected Object _getReferenced(Value<?> value) {
    return value.orElse(null);
  }

  @Override
  protected Object _getReferencedIfPresent(Value<?> value) {
    return  value.orElse(null);
  }

}

package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import cyclops.control.Eval;

public class EvalSerializer extends ReferenceTypeSerializer<Eval<?>> {

  private static final long serialVersionUID = 1L;


  protected EvalSerializer(ReferenceType fullType, boolean staticTyping,
                           TypeSerializer vts, JsonSerializer<Object> ser)
  {
    super(fullType, staticTyping, vts, ser);
  }

  protected EvalSerializer(EvalSerializer base, BeanProperty property,
                           TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper,
                           Object suppressableValue, boolean suppressNulls)
  {
    super(base, property, vts, valueSer, unwrapper,
      suppressableValue, suppressNulls);
  }

  @Override
  protected ReferenceTypeSerializer<Eval<?>> withResolved(BeanProperty prop,
                                                            TypeSerializer vts, JsonSerializer<?> valueSer,
                                                            NameTransformer unwrapper)
  {
    return new EvalSerializer(this, prop, vts, valueSer, unwrapper,
      _suppressableValue, _suppressNulls);
  }

  @Override
  public ReferenceTypeSerializer<Eval<?>> withContentInclusion(Object suppressableValue,
                                                                   boolean suppressNulls)
  {
    return new EvalSerializer(this, _property, _valueTypeSerializer,
      _valueSerializer, _unwrapper,
      suppressableValue, suppressNulls);
  }

    /*
    /**********************************************************
    /* Abstract method impls
    /**********************************************************
     */

  @Override
  protected boolean _isValuePresent(Eval<?> value) {
    return value.isPresent();
  }

  @Override
  protected Object _getReferenced(Eval<?> value) {
    return value.orElse(null);
  }

  @Override
  protected Object _getReferencedIfPresent(Eval<?> value) {
    return  value.orElse(null);
  }

}

package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import cyclops.control.Trampoline;

public class TrampolineSerializer extends ReferenceTypeSerializer<Trampoline<?>> {

  private static final long serialVersionUID = 1L;


  protected TrampolineSerializer(ReferenceType fullType, boolean staticTyping,
                                 TypeSerializer vts, JsonSerializer<Object> ser)
  {
    super(fullType, staticTyping, vts, ser);
  }

  protected TrampolineSerializer(TrampolineSerializer base, BeanProperty property,
                                 TypeSerializer vts, JsonSerializer<?> valueSer, NameTransformer unwrapper,
                                 Object suppressablTrampolineue, boolean suppressNulls)
  {
    super(base, property, vts, valueSer, unwrapper,
      suppressablTrampolineue, suppressNulls);
  }

  @Override
  protected ReferenceTypeSerializer<Trampoline<?>> withResolved(BeanProperty prop,
                                                            TypeSerializer vts, JsonSerializer<?> valueSer,
                                                            NameTransformer unwrapper)
  {
    return new TrampolineSerializer(this, prop, vts, valueSer, unwrapper,
      _suppressableValue, _suppressNulls);
  }

  @Override
  public ReferenceTypeSerializer<Trampoline<?>> withContentInclusion(Object suppressablTrampolineue,
                                                                   boolean suppressNulls)
  {
    return new TrampolineSerializer(this, _property, _valueTypeSerializer,
      _valueSerializer, _unwrapper,
      suppressablTrampolineue, suppressNulls);
  }

    /*
    /**********************************************************
    /* Abstract method impls
    /**********************************************************
     */

  @Override
  protected boolean _isValuePresent(Trampoline<?> value) {
    return value.isPresent();
  }

  @Override
  protected Object _getReferenced(Trampoline<?> value) {
    return value.orElse(null);
  }

  @Override
  protected Object _getReferencedIfPresent(Trampoline<?> value) {
    return  value.orElse(null);
  }

}

package com.oath.cyclops.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.oath.cyclops.jackson.deserializers.CyclopsDeserializers;
import com.oath.cyclops.jackson.serializers.CyclopsSerializers;

public class CyclopsModule extends SimpleModule {




  @Override
  public void setupModule(SetupContext context) {
    context.addDeserializers(new CyclopsDeserializers());
    context.addSerializers(new CyclopsSerializers());
    context.addTypeModifier(new CyclopsTypeModifier());

  }
}

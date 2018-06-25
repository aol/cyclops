package com.oath.cyclops.jackson;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.oath.cyclops.util.ExceptionSoftener;

public final class JacksonUtil {



  private static ObjectMapper mapper = null;





  private synchronized static ObjectMapper createMapper() {
    if (mapper == null) {
      mapper = new ObjectMapper();

      mapper = mapper.registerModule(new CyclopsModule());
      mapper = mapper.registerModule(new Jdk8Module());


    }
    return mapper;
  }
  public  static ObjectMapper getMapper() {
    if(mapper==null)
      return createMapper();
    return mapper;
  }

  public static String serializeToJsonFailSilently(final Object data) {
    try {
      return serializeToJson(data);
    } catch (Exception e) {
    }
    return "";
  }

  public static String serializeToJson(final Object data) {
    String jsonString = "";
    if (data == null)
      return jsonString;
    try {
      jsonString = getMapper().writeValueAsString(data);
    } catch (final Exception ex) {
      ExceptionSoftener.throwSoftenedException(ex);
    }
    return jsonString;
  }

  public static <T> T convertFromJson(final String jsonString, final Class<T> type) {
    try {


      return getMapper().readValue(jsonString, type);

    } catch (final Exception ex) {
      ExceptionSoftener.throwSoftenedException(ex);
    }
    return null;
  }
  public static <T> T convertFromJson(final String jsonString, final JavaType type) {
    try {


      return getMapper().readValue(jsonString, type);

    } catch (final Exception ex) {
      ExceptionSoftener.throwSoftenedException(ex);
    }
    return null;

  }

  public static <T> T convertFromJson(String json, final TypeReference<T> type) {
    try {
      return JacksonUtil.getMapper().readValue(json, type);
    } catch (final Exception ex) {
      ExceptionSoftener.throwSoftenedException(ex);
    }
    return null;
  }



}

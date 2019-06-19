package com.oath.cyclops.jackson.fix;

import com.oath.cyclops.jackson.JacksonUtil;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SerTest {

    String testQuery = "{\n" +
        "  \"queries\": [{\n" +
        "    \"name\": \"BidLandscape/ui/query\",\n" +
        "    \"queries\": [{\n" +
        "      \"name\": \"BidLandscape/ui/query/facets/query\"\n" +
        "    }]\n" +
        "  }]\n" +
        "}";

    @Test
    public void issue(){
        Query q = JacksonUtil.convertFromJson(testQuery,Query.class);
        System.out.println(q);

        String json = JacksonUtil.serializeToJson(q);

        assertThat(JacksonUtil.convertFromJson(json,Query.class),equalTo(q));
    }
}

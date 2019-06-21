package com.oath.cyclops.jackson.fix;

import com.oath.cyclops.jackson.JacksonUtil;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

public class SerTest {

    String testQuery = "{\n" +
        "  \"queries\": [{\n" +
        "    \"name\": \"name1\",\n" +
        "    \"queries\": [{\n" +
        "      \"name\": \"name2\"\n" +
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

    @Test
    public void deserIssue(){
        Query q = JacksonUtil.convertFromJson(testQuery,Query.class);
        assertTrue(q.queries.getOrElse(0,null) instanceof  Query);
    }
}

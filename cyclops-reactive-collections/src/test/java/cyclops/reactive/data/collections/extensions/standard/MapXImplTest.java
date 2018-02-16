package cyclops.reactive.data.collections.extensions.standard;

import com.oath.cyclops.data.collections.extensions.standard.MapXImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MapXImplTest {

  @Test
  public void testPlusAll() {
    Map map = new HashMap();
    map.put(1, "a");
    map.put(2, "b");
    MapXImpl mapX = new MapXImpl(map);

    Map map2 = new HashMap();
    map2.put(2, "B");
    map2.put(3, "c");

    mapX.plusAll(map2);
    assertThat(mapX.size(), equalTo(3));
    assertThat(mapX.get(1), equalTo("a"));
    assertThat(mapX.get(2), equalTo("B"));
    assertThat(mapX.get(3), equalTo("c"));
  }

  @Test
  public void testPlusAllWithNull() {
    Map map = new HashMap();
    map.put(1, "a");
    map.put(2, "b");
    MapXImpl mapX = new MapXImpl(map);

    mapX.plusAll((Map) null);
    assertThat(mapX.size(), equalTo(2));
    assertThat(mapX.get(1), equalTo("a"));
    assertThat(mapX.get(2), equalTo("b"));
  }

}

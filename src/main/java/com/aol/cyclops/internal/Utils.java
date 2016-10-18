package com.aol.cyclops.internal;

import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static <T> T firstOrNull(final List<T> list) {
        if (list == null || list.size() == 0)
            return null;
        return list.get(0);
    }
}

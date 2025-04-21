package com.buildbetter.plan.util;

import java.util.List;

public class TableauUtils {
    public static String safeGet(List<Integer> list, int idx) {
        if (list == null || list.size() <= idx || list.get(idx) == null) {
            return "Null";
        }
        return list.get(idx).toString();
    }
}

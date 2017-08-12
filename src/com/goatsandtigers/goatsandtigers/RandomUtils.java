package com.goatsandtigers.goatsandtigers;

import java.util.List;

public class RandomUtils {

    public static <T> List<T> randomizeList(List<T> list) {
        for (int i = 0; i < list.size(); i++) {
            int randomIndex = (int) (Math.random() * list.size());
            T randomElement = list.get(randomIndex);
            list.remove(randomIndex);
            list.add(randomElement);
        }
        return list;
    }
}

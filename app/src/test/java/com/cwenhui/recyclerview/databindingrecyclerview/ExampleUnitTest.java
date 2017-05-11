package com.cwenhui.recyclerview.databindingrecyclerview;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        List<Integer> integers = new ArrayList<>();
        integers.addAll(Arrays.asList(new Integer[]{1, 2}));
        integers.add(2, 3);
        integers.get(3);
        for (int i : integers) {
            System.out.print(i);
        }
        assertEquals(4, 2 + 2);
    }
}
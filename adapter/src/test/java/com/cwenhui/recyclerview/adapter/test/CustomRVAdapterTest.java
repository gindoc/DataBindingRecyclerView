package com.cwenhui.recyclerview.adapter.test;

import com.cwenhui.recyclerview.adapter.CustomRVAdapter;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Author: GIndoc on 2017/5/11 15:44
 * email : 735506583@qq.com
 * FOR   :
 */

public class CustomRVAdapterTest {

    @Test
    public void testAddAll() {
        CustomRVAdapter adapter = Mockito.spy(CustomRVAdapter.class);
        adapter.addAll(Mockito.anyList(), Mockito.anyInt());
        Mockito.verify(adapter).addAll(Mockito.anyList(),Mockito.anyInt());
    }
}

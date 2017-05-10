package com.cwenhui.recyclerview.databindingrecyclerview;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.cwenhui.recyclerview.adapter.LoadMoreAdapter;
import com.cwenhui.recyclerview.databindingrecyclerview.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoadMoreAdapter.RequestLoadMoreListener {
    private ActivityMainBinding mBinding;
    private LoadMoreAdapter adapter;
    private static final int ITEM = 0x00011111;
    private int isFirst = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                String[] ss = {"NIVEA MEN", "CHOCOLATE", "COLA", "TEA", "RICE", "RIO", "BEEF", "MILK",
                        "ORANGE", "APPLE"};
                adapter.addAll(Arrays.asList(ss), ITEM);
                adapter.loadMoreComplete();
            } else if (msg.what == 2) {
                String[] ss = {"NIVEA MEN2", "CHOCOLATE2", "COLA2", "TEA2", "RICE2", "RIO2", "BEEF2",
                        "MILK2", "ORANGE2", "APPLE2"};
                adapter.addAll(Arrays.asList(ss), ITEM);
                adapter.loadMoreEnd();
                handler.sendEmptyMessageDelayed(3, 3000);
            } else if (msg.what == 3) {
                String[] ss = {"NIVEA MEN..", "CHOCOLATE..", "COLA..", "TEA..", "RICE..", "RIO..", "BEEF..",
                        "MILK..", "ORANGE..", "APPLE.."};
                adapter.setNewData(Arrays.asList(ss), ITEM);
                adapter.loadMoreComplete();
                isFirst = 0;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        adapter = new LoadMoreAdapter.Builder(this)
                .setLoadMoreViewRes(R.layout.layout_simple_load_more)
                .setAutoLoadMoreSize(10)
                .setAnimationType(LoadMoreAdapter.SLIDEIN_LEFT)
                .setLayoutManager(new LinearLayoutManager(this))
                .setRecyclerView(mBinding.recyclerView)
                .setRequestLoadMoreListener(this)
                .build();

        List<String> strings = new ArrayList<>();
        String[] ss = {"小明", "细明", "粗哥", "野蛮哥", "靓仔", "美女", "嘿嘿嘿", "咯咯咯", "啦啦啦", "香蕉人"};
        strings.addAll(Arrays.asList(ss));
        adapter.addViewTypeToLayoutMap(ITEM, R.layout.item_recyclerview);
        adapter.addAll(strings, ITEM);
        mBinding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoadMoreRequested() {
        if (isFirst == 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0);
                    ++isFirst;
                }
            }, 2000);
        } else if (isFirst == 1) {
            adapter.loadMoreFail();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(1);
                }
            }, 2000);
            ++isFirst;
        } else if (isFirst == 2) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(2);
                }
            }, 2000);
            ++isFirst;
        }/* else if (isFirst == 3) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(3);
                }
            }, 3000);
        }*/
    }
}

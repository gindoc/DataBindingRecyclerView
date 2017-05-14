package com.cwenhui.recyclerview.databindingrecyclerview;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.cwenhui.recyclerview.adapter.DecorateAdapter;
import com.cwenhui.recyclerview.databindingrecyclerview.databinding.ActivityEmptyBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: GIndoc on 2017/5/13 20:31
 * email : 735506583@qq.com
 * FOR   :
 */

public class EmptyActivity extends AppCompatActivity {
    private static final int ITEM = 0x00011111;
    private static final int ITEM2 = 0x00011112;
    private ActivityEmptyBinding mBinding;
    private DecorateAdapter adapter;

    private boolean mError = true;
    private boolean mNoData = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_empty);
        adapter = new DecorateAdapter.Builder(this)
//                .setLoadMoreViewLayout(R.layout.layout_simple_load_more)
//                .setRequestLoadMoreListener(this)
                .setRecyclerView(mBinding.recyclerView)
                .setLayoutManager(new LinearLayoutManager(this))
//                .setLayoutManager(new GridLayoutManager(this, 2))
                .setAutoLoadMoreSize(5)
                .setDisableLoadMoreIfNotFullPage(true)
//                .setAnimationType(DecorateAdapter.SLIDEIN_LEFT)
                .build();
        adapter.addHeader(R.layout.layout_header);
        adapter.addFooter(R.layout.layout_footer);
        adapter.setHeaderFooterEmpty(true, true);
//        adapter.setEmptyViewLayout(R.layout.layout_empty);
        adapter.setDecorator(new MyDecorator());
//        ((SimpleItemAnimator)mBinding.recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        onRefresh();
    }

    public void onclick(View view){
        mError = true;
        mNoData = true;
        adapter.clear();
        onRefresh();
    }

    public static Intent getStartIntent(MainActivity mainActivity) {
        return new Intent(mainActivity, EmptyActivity.class);
    }

    class MyDecorator implements DecorateAdapter.Decorator{

        @Override
        public void decorator(RecyclerView.ViewHolder holder, int position, int viewType) {
            if (viewType == DecorateAdapter.EMPTY_VIEW_TYPE) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onRefresh();
                    }
                });
            }
        }
    }

    public void onRefresh() {
//        int res = R.layout.loading_view;
        adapter.setEmptyViewLayout(LayoutInflater.from(this).inflate(R.layout.loading_view,
                mBinding.recyclerView, false));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mError) {
//                    adapter.setEmptyViewLayout(R.layout.error_view);
                    adapter.setEmptyViewLayout(LayoutInflater.from(EmptyActivity.this)
                            .inflate(R.layout.error_view, mBinding.recyclerView, false));
                    mError = false;
                } else {
                    if (mNoData) {
//                        adapter.setEmptyViewLayout(R.layout.empty_view);
                        adapter.setEmptyViewLayout(LayoutInflater.from(EmptyActivity.this)
                                .inflate(R.layout.empty_view, mBinding.recyclerView, false));
                        mNoData = false;
                    } else {
                        List<String> strings = new ArrayList<>();
                        String[] ss = {"小明", "细明", "粗哥", "野蛮哥", "靓仔", "美女", "嘿嘿嘿", "咯咯咯", "啦啦啦", "香蕉人"};
                        strings.addAll(Arrays.asList(ss));
                        adapter.addViewTypeToLayoutMap(ITEM, R.layout.item_recyclerview);
                        adapter.set(strings, ITEM);
                    }
                }
            }
        }, 1000);
    }
}

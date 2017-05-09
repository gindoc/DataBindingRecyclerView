package com.cwenhui.recyclerview.loadmore;

import android.view.View;

import com.cwenhui.recyclerview.adapter.BindingViewHolder;

/**
 * Author: GIndoc on 2017/5/8 22:33
 * email : 735506583@qq.com
 * FOR   : 此类包含“加载更多”视图的各种状态，根据状态来设置“加载更多”视图的visible
 */
public abstract class LoadMoreView {
    public static final int STATUS_DEFAULT = 1;
    public static final int STATUS_LOADING = 2;
    public static final int STATUS_FAIL = 3;
    public static final int STATUS_END = 4;

    private int mLoadMoreStatus = STATUS_DEFAULT;
    private boolean mLoadMoreEndGone = false;

    public void setLoadMoreStatus(int loadMoreStatus) {
        this.mLoadMoreStatus = loadMoreStatus;
    }

    public int getLoadMoreStatus() {
        return mLoadMoreStatus;
    }

    public void convert(BindingViewHolder holder) {
        switch (mLoadMoreStatus) {
            case STATUS_LOADING:
                visibleLoading(holder, View.VISIBLE);
                visibleLoadFail(holder, View.GONE);
                visibleLoadEnd(holder, View.GONE);
                break;
            case STATUS_FAIL:
                visibleLoading(holder, View.GONE);
                visibleLoadFail(holder, View.VISIBLE);
                visibleLoadEnd(holder, View.GONE);
                break;
            case STATUS_END:
                visibleLoading(holder, View.GONE);
                visibleLoadFail(holder, View.GONE);
                visibleLoadEnd(holder, View.VISIBLE);
                break;
            case STATUS_DEFAULT:
                visibleLoading(holder, View.GONE);
                visibleLoadFail(holder, View.GONE);
                visibleLoadEnd(holder, View.GONE);
                break;
        }
        holder.getBinding().executePendingBindings();
    }

    /**
     * 设置“没有更多”视图的可见性
     * @param loadMoreEndGone
     */
    public final void setLoadMoreEndGone(boolean loadMoreEndGone) {
        this.mLoadMoreEndGone = loadMoreEndGone;
    }

    public final boolean isLoadEndMoreGone() {
//        if (getLoadEndViewId() == 0) {
//            return true;
//        }
        return mLoadMoreEndGone;

    }

    protected abstract void visibleLoading(BindingViewHolder holder, int visible);

    protected abstract void visibleLoadFail(BindingViewHolder holder, int visible);

    protected abstract void visibleLoadEnd(BindingViewHolder holder, int visible);
}

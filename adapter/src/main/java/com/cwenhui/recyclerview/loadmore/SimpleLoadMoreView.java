package com.cwenhui.recyclerview.loadmore;

import com.cwenhui.recyclerview.adapter.BindingViewHolder;
import com.cwenhui.recyclerview.adapter.databinding.LayoutSimpleLoadMoreBinding;

/**
 * Author: GIndoc on 2017/5/8 23:03
 * email : 735506583@qq.com
 * FOR   :
 */

public final class SimpleLoadMoreView extends LoadMoreView {

    @Override
    protected void visibleLoading(BindingViewHolder holder, int visible) {
        // 可以加点动画
//        holder.getBinding().setVariable(BR.isLoading, visible);
        ((LayoutSimpleLoadMoreBinding)holder.getBinding()).loading.setVisibility(visible);
    }

    @Override
    protected void visibleLoadFail(BindingViewHolder holder, int visible) {
//        holder.getBinding().setVariable(BR.isFailed, visible);
        ((LayoutSimpleLoadMoreBinding)holder.getBinding()).loadFailed.setVisibility(visible);
    }

    @Override
    protected void visibleLoadEnd(BindingViewHolder holder, int visible) {
//        holder.getBinding().setVariable(BR.isEnded, visible);
        ((LayoutSimpleLoadMoreBinding)holder.getBinding()).loadEnded.setVisibility(visible);
    }
}

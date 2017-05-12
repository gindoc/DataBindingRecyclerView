package com.cwenhui.recyclerview.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cwenhui.recyclerview.loadmore.LoadMoreView;
import com.cwenhui.recyclerview.loadmore.SimpleLoadMoreView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: GIndoc on 2017/5/11 19:19
 * email : 735506583@qq.com
 * FOR   :
 */

public class DecorateAdapter<T> extends RecyclerView.Adapter<RecyclerView
        .ViewHolder> {
//    private final T mBase;
    private static final int HEADER_VIEW_TYPE = -1000;
    private static final int FOOTER_VIEW_TYPE = -2000;
    private static final int LOAD_MORE_VIEW_TYPE = -3000;
    private final List<View> mHeaders = new ArrayList<>();
    private final List<View> mFooters = new ArrayList<>();
    protected ArrayList<Integer> mCollectionViewType;
    private ArrayMap<Integer, Integer> mItemTypeToLayoutMap = new ArrayMap<>();
    private List<T> mData;

    /**
     * load more
     */
    private LoadMoreView mLoadMoreView = new SimpleLoadMoreView();      // 保存lode more view的状态
    private View mLoadView;
    private boolean mNextLoadEnable = false;
    private boolean mLoadMoreEnable = false;
    private boolean mLoading = false;
    private RequestLoadMoreListener mRequestLoadMoreListener;
    private RecyclerView mRecyclerView;                                 // 用于检查列表是否满屏
    protected final LayoutInflater mLayoutInflater;
    protected Presenter mPresenter;
    protected Decorator mDecorator;

    public DecorateAdapter(Context context) {
        super();
        mData = new ArrayList<>();
        mCollectionViewType = new ArrayList<>();
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setRequestLoadMoreListener(RequestLoadMoreListener listener, View view,
                                           RecyclerView recyclerView) {
        bindToRecyclerView(recyclerView);
        openLoadMore(listener);
        mLoadView = view;
    }

    private RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    /**
     * 检测是否有设置recyclerView
     */
    private void checkNotNull() {
        if (getRecyclerView() == null) {
            throw new RuntimeException("please bind recyclerView first!");
        }
    }

    /**
     * same as recyclerView.setAdapter(), and save the instance of recyclerView
     */
    private void bindToRecyclerView(RecyclerView recyclerView) {
        if (getRecyclerView() != null) {
            throw new RuntimeException("Don't bind twice");
        }
        setRecyclerView(recyclerView);
        getRecyclerView().setAdapter(this);
    }

    private void openLoadMore(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
        mNextLoadEnable = true;
        mLoadMoreEnable = true;
        mLoading = false;
    }

    public void setLoadMoreView(LoadMoreView loadMoreView) {
        mLoadMoreView = loadMoreView;
    }

    /**
     * bind recyclerView {@link #bindToRecyclerView(RecyclerView)} before use!
     *
     * @see #disableLoadMoreIfNotFullPage(RecyclerView)
     */
    private void disableLoadMoreIfNotFullPage() {
        checkNotNull();
        disableLoadMoreIfNotFullPage(getRecyclerView());
    }

    /**
     * check if full page after {@link #set(List, int)}, if full, it will enable load more again.
     *
     * @param recyclerView your recyclerView
     * @see #set(List, int)
     */
    private void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
        setEnableLoadMore(false);
        if (recyclerView == null) return;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager == null) return;
        if (manager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1) != getItemCount()) {
                        setEnableLoadMore(true);
                    }
                }
            }, 50);
        } else if (manager instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager)
                    manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int[] positions = new int[staggeredGridLayoutManager.getSpanCount()];
                    staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(positions);
                    int pos = getTheBiggestNumber(positions) + 1;
                    if (pos != getItemCount()) {
                        setEnableLoadMore(true);
                    }
                }
            }, 50);
        }
    }

    /**
     * Set the enabled state of load more.
     *
     * @param enable True if load more is enabled, false otherwise.
     */
    private void setEnableLoadMore(boolean enable) {
        int oldLoadMoreCount = getLoadMoreViewCount();
        mLoadMoreEnable = enable;
        int newLoadMoreCount = getLoadMoreViewCount();

        if (oldLoadMoreCount == 1) {
            if (newLoadMoreCount == 0) {
                notifyItemRemoved(getItemCount());
            }
        } else {
            if (newLoadMoreCount == 1) {
                mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                notifyItemInserted(getItemCount());
            }
        }
    }

    /**
     * Returns the enabled status for load more.
     *
     * @return True if load more is enabled, false otherwise.
     */
    public boolean isLoadMoreEnable() {
        return mLoadMoreEnable;
    }

    private int getTheBiggestNumber(int[] numbers) {
        int tmp = -1;
        if (numbers == null || numbers.length == 0) {
            return tmp;
        }
        for (int num : numbers) {
            if (num > tmp) {
                tmp = num;
            }
        }
        return tmp;
    }

    private int getLoadMoreViewCount() {
        if (mRequestLoadMoreListener == null || !mLoadMoreEnable) {
            return 0;
        }
        if (!mNextLoadEnable && mLoadMoreView.isLoadMoreEndGone()) {
            return 0;
        }
        if (mData.size() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * Refresh end, no more data
     */
    public void loadMoreEnd() {
        loadMoreEnd(false);
    }

    /**
     * Refresh end, no more data
     *
     * @param gone if true gone the load more view
     */
    public void loadMoreEnd(boolean gone) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mNextLoadEnable = false;
        mLoadMoreView.setLoadMoreEndGone(gone);
        if (gone) {
            notifyItemRemoved(getItemCount() - 1);
        } else {
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_END);
            notifyItemChanged(getItemCount() - 1);
        }
    }

    /**
     * Refresh complete
     */
    public void loadMoreComplete() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getItemCount() - 1);
    }

    /**
     * Refresh failed
     */
    public void loadMoreFail() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_FAIL);
        notifyItemChanged(getItemCount() - 1);
    }

    /**
     * Adds a header view.
     */
    public void addHeader(@NonNull View view) {
        if (view == null) {
            throw new IllegalArgumentException("You can't have a null header!");
        }
        mHeaders.add(view);
    }

    /**
     * Adds a footer view.
     */
    public void addFooter(@NonNull View view) {
        if (view == null) {
            throw new IllegalArgumentException("You can't have a null footer!");
        }
        mFooters.add(view);
    }

    public void removeFooter(@NonNull View view) {
        if (view == null) {
            throw new IllegalArgumentException("You can't remove a null footer!");
        }
        mFooters.remove(view);
    }

    /**
     * Toggles the visibility of the header views.
     */
    public void setHeaderVisibility(boolean shouldShow) {
        for (View header : mHeaders) {
            header.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    public void setHeaderVisibility(View view, boolean shouldShow) {
        for (View header : mHeaders) {
            if (header == view) {
                header.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * Toggles the visibility of the footer views.
     */
    public void setFooterVisibility(boolean shouldShow) {
        for (View footer : mFooters) {
            footer.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    public void setFooterVisibility(View view, boolean shouldShow) {
        for (View footer : mFooters) {
            if (footer == view) {
                footer.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * @return the number of headers.
     */
    public int getHeaderCount() {
        return mHeaders.size();
    }

    /**
     * @return the number of footers.
     */
    public int getFooterCount() {
        return mFooters.size();
    }

    /**
     * Gets the indicated header, or null if it doesn't exist.
     */
    public View getHeader(int i) {
        return i < mHeaders.size() ? mHeaders.get(i) : null;
    }

    /**
     * Gets the indicated footer, or null if it doesn't exist.
     */
    public View getFooter(int i) {
        return i < mFooters.size() ? mFooters.get(i) : null;
    }

    @LayoutRes
    protected int getLayoutRes(int viewType) {
        int res = mItemTypeToLayoutMap.get(viewType);
        return res;
    }

    private boolean isHeader(int viewType) {
        return viewType >= HEADER_VIEW_TYPE && viewType < (HEADER_VIEW_TYPE + mHeaders.size());
    }

    private boolean isFooter(int viewType) {
        return viewType >= FOOTER_VIEW_TYPE && viewType < (FOOTER_VIEW_TYPE + mFooters.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (isHeader(viewType)) {
            int whichHeader = Math.abs(viewType - HEADER_VIEW_TYPE);
            View headerView = mHeaders.get(whichHeader);
            headerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerView.ViewHolder(headerView) {
            };
        } else if (isFooter(viewType)) {
            int whichFooter = Math.abs(viewType - FOOTER_VIEW_TYPE);
            View footerView = mFooters.get(whichFooter);
            footerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerView.ViewHolder(footerView) {
            };

        } else if (viewType == LOAD_MORE_VIEW_TYPE) {
            mLoadView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new BindingViewHolder<>(DataBindingUtil.bind(mLoadView));
        } else {
            int res = getLayoutRes(viewType);
            ViewDataBinding binding = DataBindingUtil.inflate(mLayoutInflater, res, viewGroup, false);
            return new BindingViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        autoLoadMore(position);
        if (getItemViewType(position) == LOAD_MORE_VIEW_TYPE) {
            mLoadMoreView.convert((BindingViewHolder) holder);
            ((BindingViewHolder) holder).getBinding().getRoot().setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                        notifyItemChanged(getItemCount() - 1);
                    }
                }
            });
        }else if (position < mHeaders.size()) {
            // Headers don't need anything special

        } else if (position < mHeaders.size() + mData.size()) {
            // This is a real position, not a header or footer. Bind it.
            final Object item = mData.get(position - mHeaders.size());
            ViewDataBinding binding = ((BindingViewHolder) holder).getBinding();
            binding.setVariable(com.cwenhui.recyclerview.adapter.BR.item, item);
            binding.setVariable(com.cwenhui.recyclerview.adapter.BR.presenter, getPresenter());
            binding.executePendingBindings();
            if (mDecorator != null) {
                mDecorator.decorator(((BindingViewHolder) holder), position, getItemViewType(position));
            }
        } else {
            // Footers don't need anything special
        }
    }
    private int mAutoLoadMoreSize = 1;

    private void setAutoLoadMoreSize(int autoLoadMoreSize) {
        if (autoLoadMoreSize > 1) {
            mAutoLoadMoreSize = autoLoadMoreSize;
        }
    }

    private void autoLoadMore(int position) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        if (position < getItemCount() - mAutoLoadMoreSize) {
            return;
        }
        if (mLoadMoreView.getLoadMoreStatus() != LoadMoreView.STATUS_DEFAULT) {
            return;
        }
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_LOADING);
        if (!mLoading) {
            mLoading = true;
            if (getRecyclerView() != null) {
                getRecyclerView().post(new Runnable() {
                    @Override
                    public void run() {
                        mRequestLoadMoreListener.onLoadMoreRequested();
                    }
                });
            } else {
                mRequestLoadMoreListener.onLoadMoreRequested();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mHeaders.size() + mData.size() + mFooters.size() + getLoadMoreViewCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaders.size()) {
            return HEADER_VIEW_TYPE + position;
        } else if (position < (mHeaders.size() + mData.size())) {
            return mCollectionViewType.get(position - mHeaders.size());
        } else if (position < mHeaders.size() + mData.size() + mFooters.size()) {
            return FOOTER_VIEW_TYPE + position - mHeaders.size() - mData.size();
        } else {
            return LOAD_MORE_VIEW_TYPE;
        }
    }

    public void addViewTypeToLayoutMap(Integer viewType, Integer layoutRes) {
        mItemTypeToLayoutMap.put(viewType, layoutRes);
    }

    public void addAll(List viewModels, int viewType) {
        mData.addAll(viewModels);
        for (int i = 0; i < viewModels.size(); ++i) {
            mCollectionViewType.add(viewType);
        }
        notifyItemRangeInserted(mHeaders.size() + mCollectionViewType.size() - viewModels.size(),
                viewModels.size());
    }

    public void addAll(int position, List viewModels, int viewType) {
        mData.addAll(position, viewModels);
        for (int i = 0; i < viewModels.size(); i++) {
            mCollectionViewType.add(position + i, viewType);
        }
        notifyItemRangeInserted(mHeaders.size() + position, viewModels.size());
    }

    public void addAll(List viewModels, MultiTypeAdapter.MultiViewType multiViewType) {
        mData.addAll(viewModels);
        for (int i = 0; i < viewModels.size(); ++i) {
            mCollectionViewType.add(multiViewType.getViewType(viewModels.get(i)));
        }
        notifyItemRangeInserted(mHeaders.size() + mCollectionViewType.size() - viewModels.size(),
                viewModels.size());
    }

    public void set(List viewModels, int viewType) {
        mData.clear();
        mCollectionViewType.clear();

        if (viewModels == null) {
            mData.add(null);
            mCollectionViewType.add(viewType);
        } else {
            mData.addAll(viewModels);
            for (int i = 0; i < viewModels.size(); ++i) {
                mCollectionViewType.add(viewType);
            }
        }
        notifyDataSetChanged();
    }

    public void set(List viewModels, MultiTypeAdapter.MultiViewType viewType) {
        mData.clear();
        mCollectionViewType.clear();
        mData.addAll(viewModels);
        for (int i = 0; i < viewModels.size(); ++i) {
            mCollectionViewType.add(viewType.getViewType(viewModels.get(i)));
        }
        notifyDataSetChanged();
    }

    public void add(T viewModel, int viewType) {
        mData.add(viewModel);
        mCollectionViewType.add(viewType);
        notifyItemInserted(mHeaders.size() + mData.size());
    }

    public void add(int position, T viewModel, int viewType) {
        mData.add(position, viewModel);
        mCollectionViewType.add(position, viewType);
        notifyItemInserted(mHeaders.size() + position);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (isFixedViewType(type)) {
            setFullSpan(holder);
        } else {
//            addAnimation(holder);
        }
    }

    private boolean isFixedViewType(int type) {
        return isHeader(type) || isFooter(type);
    }

    public void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams)
                    holder.itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }


    public interface RequestLoadMoreListener {
        void onLoadMoreRequested();
    }

    /**
     * 用户可以自定义Decorator继承DefaultDecorator，重写decorator方法，
     * 也可以实现接口BaseViewAdapter.Decorator
     */
//    class DefaultDecorator implements BaseViewAdapter.Decorator {
//        @Override
//        public void decorator(BindingViewHolder holder, int position, int viewType) {
//            if (viewType == LOAD_MORE_VIEW_TYPE) {
//                mLoadMoreView.convert(holder);
//                holder.getBinding().getRoot().setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
//                            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
//                            notifyItemChanged(getItemCount() - 1);
//                        }
//                    }
//                });
//            }
//        }
//    }

//    public void setDecorator(BaseViewAdapter.Decorator decorator) {
//        mBase.setDecorator(decorator);
//    }

    public interface Presenter {
    }

    public interface Decorator {
        void decorator(BindingViewHolder holder, int position, int viewType);
    }

    public void setDecorator(Decorator decorator) {
        mDecorator = decorator;
    }

    public void setPresenter(Presenter presenter) {
        mPresenter = presenter;
    }

    protected Presenter getPresenter() {
        return mPresenter;
    }
}

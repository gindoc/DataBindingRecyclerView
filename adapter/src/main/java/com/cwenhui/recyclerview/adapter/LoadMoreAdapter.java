package com.cwenhui.recyclerview.adapter;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.cwenhui.recyclerview.animation.AlphaInAnimation;
import com.cwenhui.recyclerview.animation.BaseAnimation;
import com.cwenhui.recyclerview.animation.ScaleInAnimation;
import com.cwenhui.recyclerview.animation.SlideInBottomAnimation;
import com.cwenhui.recyclerview.animation.SlideInLeftAnimation;
import com.cwenhui.recyclerview.animation.SlideInRightAnimation;
import com.cwenhui.recyclerview.loadmore.LoadMoreView;
import com.cwenhui.recyclerview.loadmore.SimpleLoadMoreView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

/**
 * Author: GIndoc on 2017/5/9 0:10
 * email : 735506583@qq.com
 * FOR   :
 */

public class LoadMoreAdapter extends MultiTypeAdapter {
    private static final int VIEW_TYPE_LOAD_MORE = 0x00000222;
    private LoadMoreView mLoadMoreView = new SimpleLoadMoreView();
    //load more
    private boolean mNextLoadEnable = false;
    private boolean mLoadMoreEnable = false;
    private boolean mLoading = false;
    private RequestLoadMoreListener mRequestLoadMoreListener;

    //Animation
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHAIN = 0x00000001;

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SCALEIN = 0x00000002;

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_BOTTOM = 0x00000003;

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_LEFT = 0x00000004;

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_RIGHT = 0x00000005;

    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    private boolean mFirstOnlyEnable = true;
    private boolean mOpenAnimationEnable = false;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mDuration = 300;
    private int mLastPosition = -1;

    private BaseAnimation mCustomAnimation;
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();

    private RecyclerView mRecyclerView;     // 用于检查列表是否满屏

    private LoadMoreAdapter(Context context) {
        super(context);
    }

    private LoadMoreAdapter(Context context, int layoutRes) {
        super(context);
        setDecorator(new DefaultDecorator());      // 最好让用户自己去定义并赋值
        setLoadMoreViewRes(layoutRes);
    }

    private LoadMoreAdapter(Context context, Map<Integer, Integer> viewTypeToLayoutMap) {
        super(context, viewTypeToLayoutMap);
        setDecorator(new DefaultDecorator());
    }

    private RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

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

    /**
     * 将“加载更多”视图的布局添加到mItemTypeToLayoutMap中，对应的ViewType是VIEW_TYPE_LOAD_MORE
     *
     * @param layoutRes
     */
    private void setLoadMoreViewRes(int layoutRes) {
        if (layoutRes != 0) {
            addViewTypeToLayoutMap(VIEW_TYPE_LOAD_MORE, layoutRes);
            add(null, VIEW_TYPE_LOAD_MORE);
        } else {
            throw new RuntimeException("请确定是否提供了Load More View的布局文件");
        }
    }

    /**
     * 设置LoadMoreView(包含“加载更多”视图的各种状态)
     *
     * @param loadMoreView
     */
    private void setLoadMoreView(LoadMoreView loadMoreView) {
        this.mLoadMoreView = loadMoreView;
    }


    private void openLoadMore(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
        mNextLoadEnable = true;
        mLoadMoreEnable = true;
        mLoading = false;
    }

    private void setOnLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener, RecyclerView
            recyclerView) {
        openLoadMore(requestLoadMoreListener);
        if (getRecyclerView() == null) {
            setRecyclerView(recyclerView);
        }
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
//                notifyItemRemoved(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
                notifyItemRemoved(mCollection.size());
            }
        } else {
            if (newLoadMoreCount == 1) {
                mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
//                notifyItemInserted(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
                notifyItemInserted(mCollection.size());
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

    /**
     * Sets the duration of the animation.
     *
     * @param duration The length of the animation, in milliseconds.
     */
    private void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Load more view count
     *
     * @return 0 or 1
     */
    private int getLoadMoreViewCount() {
        if (mRequestLoadMoreListener == null || !mLoadMoreEnable) {
            return 0;
        }
        if (!mNextLoadEnable && mLoadMoreView.isLoadEndMoreGone()) {
            return 0;
        }
        if (mCollection.size() == 0) {
            return 0;
        }
        return 1;
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
//            notifyItemRemoved(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
            notifyItemRemoved(mCollection.size());
        } else {
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_END);
//            notifyItemChanged(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
            notifyItemChanged(mCollection.size());
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
//        notifyItemChanged(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
        notifyItemChanged(mCollection.size());
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
//        notifyItemChanged(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
        notifyItemChanged(mCollection.size());
    }

    @Override
    public void onBindViewHolder(BindingViewHolder holder, int position) {
        autoLoadMore(position);
        super.onBindViewHolder(holder, position);
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
    public void onViewAttachedToWindow(BindingViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == VIEW_TYPE_LOAD_MORE) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    /**
     * When set to true, the item will layout using all span area. That means, if orientation
     * is vertical, the view will have full width; if orientation is horizontal, the view will
     * have full height.
     * if the hold view use StaggeredGridLayoutManager they should using all span area
     *
     * @param holder True if this item should traverse all spans.
     */
    private void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    /**
     * add animation when you want to show time
     *
     * @param holder
     */
    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation = null;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    startAnim(anim, holder.getLayoutPosition());
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * set anim to start when loading
     *
     * @param anim
     * @param index
     */
    private void startAnim(Animator anim, int index) {
        anim.setDuration(mDuration).start();
        anim.setInterpolator(mInterpolator);
    }

    /**
     * Set the view animation type.
     *
     * @param animationType One of {@link #ALPHAIN}, {@link #SCALEIN}, {@link #SLIDEIN_BOTTOM},
     *                      {@link #SLIDEIN_LEFT}, {@link #SLIDEIN_RIGHT}.
     */
    private void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case ALPHAIN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALEIN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDEIN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * Set Custom ObjectAnimator
     *
     * @param animation ObjectAnimator
     */
    private void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    /**
     * To open the animation when loading
     */
    private void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }

    /**
     * {@link #addAnimation(RecyclerView.ViewHolder)}
     *
     * @param firstOnly true just show anim when first loading false show anim when load the data every time
     */
    private void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    /**
     * setting up a new instance to data;
     *
     * @param data
     */
    public void setNewData(@Nullable List data, int viewType) {
        Object lastObject = mCollection.get(mCollection.size() - 1);
        Integer lastViewType = mCollectionViewType.get(mCollectionViewType.size() - 1);
        mCollection.clear();
        mCollection.addAll(data);
        mCollectionViewType.clear();
        for (int i=0;i<mCollection.size();i++) {
            mCollectionViewType.add(viewType);
        }
        mCollection.add(lastObject);
        mCollectionViewType.add(lastViewType);
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true;
            mLoadMoreEnable = true;
            mLoading = false;
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        }
        mLastPosition = -1;
        notifyDataSetChanged();
    }

    /********************
     * override MultiTypeAdapter start
     ***********************/
    @Override
    public void set(List viewModels, int viewType) {
        mCollection.clear();
        mCollectionViewType.clear();
        if (viewModels == null) {
            add(null, viewType);
        } else {
            addAll(viewModels, viewType);
        }
    }

    @Override
    public void set(List viewModels, MultiViewType viewType) {
        mCollection.clear();
        mCollectionViewType.clear();
        addAll(viewModels, viewType);
    }

    @Override
    public void add(Object viewModel, int viewType) {
        if (mCollection.size() == 0) {
            add(0, viewModel, viewType);
        } else {
            add(mCollection.size() - 1, viewModel, viewType);       // 加载load view前面
        }
    }

    @Override
    public void add(int position, Object viewModel, int viewType) {
        mCollection.add(position, viewModel);
        mCollectionViewType.add(position, viewType);
        notifyItemInserted(position);
    }

    @Override
    public void addAll(List viewModels, int viewType) {
        int pos = mCollection.size() == 0 ? 0 : mCollection.size() - 1;
        addAll(pos, viewModels, viewType);
    }

    @Override
    public void addAll(int position, List viewModels, int viewType) {
        mCollection.addAll(position, viewModels);
        for (int i = 0; i < viewModels.size(); i++) {
            mCollectionViewType.add(position + i, viewType);
        }
        notifyItemRangeInserted(position, viewModels.size());
    }

    @Override
    public void addAll(List viewModels, MultiViewType multiViewType) {
        int pos = mCollection.size() == 0 ? 0 : mCollection.size() - 1;
        mCollection.addAll(pos, viewModels);
        for (int i = 0; i < viewModels.size(); ++i) {
            mCollectionViewType.add(pos + i, multiViewType.getViewType(viewModels.get(i)));
        }
        notifyItemRangeInserted(pos, viewModels.size());
    }

    /********************
     * override MultiTypeAdapter end
     ***********************/

    class DefaultDecorator implements BaseViewAdapter.Decorator {
        @Override
        public void decorator(BindingViewHolder holder, int position, int viewType) {
            if (viewType == VIEW_TYPE_LOAD_MORE) {
                mLoadMoreView.convert(holder);
            }
            holder.getBinding().getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                        notifyItemChanged(mCollection.size() - 1);
                    }
                }
            });
        }
    }

    public interface RequestLoadMoreListener {
        void onLoadMoreRequested();
    }

    public static class Builder {
        Context mContext;
        RecyclerView mRecyclerView;
        int mLoadMoreViewRes;
        RecyclerView.LayoutManager mLayoutManager;
        RequestLoadMoreListener mRequestLoadMoreListener;
        int mAutoLoadMoreSize;
        boolean isDisableLoadMoreIfNotFullPage;
        boolean isFirstOnly = true;
        boolean isOpenAnimation;
        int mAnimationType;
        BaseAnimation mBaseAnimation;
        LoadMoreView mLoadMoreView;
        int mDuration = 300;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setLoadMoreViewRes(int loadMoreViewRes) {
            mLoadMoreViewRes = loadMoreViewRes;
            return this;
        }

        public Builder setRequestLoadMoreListener(RequestLoadMoreListener listener) {
            mRequestLoadMoreListener = listener;
            return this;
        }

        public Builder setRecyclerView(RecyclerView recyclerView) {
            this.mRecyclerView = recyclerView;
            return this;
        }

        public Builder setLayoutManager(RecyclerView.LayoutManager layoutManager) {
            this.mLayoutManager = layoutManager;
            return this;
        }

        public Builder setAutoLoadMoreSize(int autoLoadMoreSize) {
            this.mAutoLoadMoreSize = autoLoadMoreSize;
            return this;
        }

        public Builder setDisableLoadMoreIfNotFullPage(boolean disableLoadMoreIfNotFullPage) {
            isDisableLoadMoreIfNotFullPage = disableLoadMoreIfNotFullPage;
            return this;
        }

        public Builder setFirstOnly(boolean firstOnly) {
            isFirstOnly = firstOnly;
            return this;
        }

        public Builder setAnimationType(int animationType) {
            isOpenAnimation = true;
            this.mAnimationType = animationType;
            return this;
        }

        public Builder setBaseAnimation(BaseAnimation baseAnimation) {
            isOpenAnimation = true;
            this.mBaseAnimation = baseAnimation;
            return this;
        }

        public Builder setLoadMoreView(LoadMoreView loadMoreView) {
            this.mLoadMoreView = loadMoreView;
            return this;
        }

        public Builder setDuration(int duration) {
            mDuration = duration;
            return this;
        }

        public LoadMoreAdapter build() {
            LoadMoreAdapter adapter = new LoadMoreAdapter(mContext, mLoadMoreViewRes);
            if (isDisableLoadMoreIfNotFullPage) {
                adapter.disableLoadMoreIfNotFullPage();
            }
            if (mLoadMoreView != null) {
                adapter.setLoadMoreView(mLoadMoreView);
            }
            if (mRecyclerView == null) {
                throw new RuntimeException("请确定提供了RecyclerView");
            }
            mRecyclerView.setLayoutManager(mLayoutManager);
            adapter.setOnLoadMoreListener(mRequestLoadMoreListener, mRecyclerView);
            adapter.setAutoLoadMoreSize(mAutoLoadMoreSize);
            if (isOpenAnimation) {
                if (mAnimationType != 0) {
                    adapter.openLoadAnimation(mAnimationType);
                } else if (mBaseAnimation != null) {
                    adapter.openLoadAnimation(mBaseAnimation);
                } else {
                    adapter.openLoadAnimation();
                }
                adapter.setDuration(mDuration);
                adapter.isFirstOnly(isFirstOnly);
            }
            return adapter;
        }
    }
}

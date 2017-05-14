package com.cwenhui.recyclerview.adapter;

import android.animation.Animator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Author: GIndoc on 2017/5/11 19:19
 * email : 735506583@qq.com
 * FOR   :
 */

public class DecorateAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HEADER_VIEW_TYPE = -1000;
    public static final int FOOTER_VIEW_TYPE = -2000;
    public static final int LOAD_MORE_VIEW_TYPE = -3000;
    public static final int EMPTY_VIEW_TYPE = -4000;
    private final List<Integer> mHeaders = new ArrayList<>();
    private final List<Integer> mFooters = new ArrayList<>();
    protected ArrayList<Integer> mCollectionViewType;
    private ArrayMap<Integer, Integer> mItemTypeToLayoutMap = new ArrayMap<>();
    private List<T> mData;

    // empty view
    private FrameLayout mEmptyViewLayout;
    private boolean mIsUseEmpty = true;
    private boolean mHeadAndEmptyEnable;
    private boolean mFootAndEmptyEnable;

    /**
     * load more
     */
    private LoadMoreView mLoadMoreView = new SimpleLoadMoreView();      // 保存lode more view的状态
    private int mLoadMoreViewLayout;
    private boolean mNextLoadEnable = false;
    private boolean mLoadMoreEnable = false;
    private boolean mLoading = false;
    private RequestLoadMoreListener mRequestLoadMoreListener;
    private RecyclerView mRecyclerView;                                 // 用于检查列表是否满屏
    protected final LayoutInflater mLayoutInflater;
    protected Presenter mPresenter;
    protected Decorator mDecorator;

    /**
     * Animation
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHAIN = 0x00000001;
    public static final int SCALEIN = 0x00000002;
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    public static final int SLIDEIN_LEFT = 0x00000004;
    public static final int SLIDEIN_RIGHT = 0x00000005;

    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    private boolean mFirstOnlyEnable = true;                            // 动画是否只执行一次
    private boolean mOpenAnimationEnable = false;                       // 是否开启动画
    private Interpolator mInterpolator = new LinearInterpolator();      // 动画插值器
    private int mDuration = 300;                                        // 动画执行时间
    private int mLastPosition = -1;                                     // 上个动画的下标

    private BaseAnimation mCustomAnimation;                             // 自定义动画
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();    // 库提供的动画（和自定义动画2选1执行）

    private DecorateAdapter(Context context) {
        super();
        mData = new ArrayList<>();
        mCollectionViewType = new ArrayList<>();
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    private void setLoadMoreViewLayout(int loadMoreViewLayout) {
        mLoadMoreViewLayout = loadMoreViewLayout;
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

    /**
     * Adds a header view.
     */
    public void addHeader(@LayoutRes int layout) {
        if (layout == 0) {
            throw new IllegalArgumentException("You can't have a null header!");
        }
        mHeaders.add(layout);
    }

    /**
     * Adds a footer view.
     */
    public void addFooter(@LayoutRes int layout) {
        if (layout == 0) {
            throw new IllegalArgumentException("You can't have a null footer!");
        }
        mFooters.add(layout);
    }

    public void removeFooter(@LayoutRes int layout) {
        if (layout == 0) {
            throw new IllegalArgumentException("You can't remove a null footer!");
        }
        mFooters.remove(layout);
    }

    /**
     * Toggles the visibility of the header views.
     */
//    public void setHeaderVisibility(boolean shouldShow) {
//        for (View header : mHeaders) {
//            header.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
//        }
//    }

//    public void setHeaderVisibility(View view, boolean shouldShow) {
//        for (View header : mHeaders) {
//            if (header == view) {
//                header.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
//            }
//        }
//    }

    /**
     * Toggles the visibility of the footer views.
     */
//    public void setFooterVisibility(boolean shouldShow) {
//        for (View footer : mFooters) {
//            footer.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
//        }
//    }

//    public void setFooterVisibility(View view, boolean shouldShow) {
//        for (View footer : mFooters) {
//            if (footer == view) {
//                footer.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
//            }
//        }
//    }

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
        return getRecyclerView().getChildAt(i);
    }

    /**
     * Gets the indicated footer, or null if it doesn't exist.
     */
    public View getFooter(int i) {
        return getRecyclerView().getChildAt(mHeaders.size() + mData.size() + i);
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
    public int getItemCount() {
        if (getEmptyViewCount() == 1) {
            int count = 1;
            if (mHeadAndEmptyEnable && mHeaders.size() != 0) {
                count++;
            }
            if (mFootAndEmptyEnable && mFooters.size() != 0) {
                count++;
            }
            return count;
        }
        return mHeaders.size() + mData.size() + mFooters.size() + getLoadMoreViewCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == 1) {
            boolean header = mHeadAndEmptyEnable && mHeaders.size() != 0;
            switch (position) {
                case 0:
                    if (header) {
                        return HEADER_VIEW_TYPE;
                    } else {
                        return EMPTY_VIEW_TYPE;
                    }
                case 1:
                    if (header) {
                        return EMPTY_VIEW_TYPE;
                    } else {
                        return FOOTER_VIEW_TYPE;
                    }
                case 2:
                    return FOOTER_VIEW_TYPE;
                default:
                    return EMPTY_VIEW_TYPE;
            }
        }
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

    public void clear() {
        mData.clear();
        mCollectionViewType.clear();
        mItemTypeToLayoutMap.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (isHeader(viewType)) {
            int whichHeader = Math.abs(viewType - HEADER_VIEW_TYPE);
            int headerView = mHeaders.get(whichHeader);
            return new BindingViewHolder<>(DataBindingUtil.inflate(mLayoutInflater, headerView,
                    viewGroup, false));
        } else if (isFooter(viewType)) {
            int whichFooter = Math.abs(viewType - FOOTER_VIEW_TYPE);
            int footerView = mFooters.get(whichFooter);
            return new BindingViewHolder<>(DataBindingUtil.inflate(mLayoutInflater, footerView,
                    viewGroup, false));
        } else if (viewType == LOAD_MORE_VIEW_TYPE) {
            return new BindingViewHolder<>(DataBindingUtil.inflate(mLayoutInflater,
                    mLoadMoreViewLayout, viewGroup, false));
        } else if (viewType == EMPTY_VIEW_TYPE) {
            /*ViewDataBinding binding = DataBindingUtil.inflate(mLayoutInflater, mEmptyViewLayout,
                    viewGroup, false);*/
//            ViewDataBinding binding = DataBindingUtil.bind(mEmptyViewLayout);
//            return new BindingViewHolder<>(binding);
            return new RecyclerView.ViewHolder(mEmptyViewLayout) {
            };
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
            ((BindingViewHolder) holder).getBinding().getRoot()
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                                mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                                notifyItemChanged(getItemCount() - 1);
                            }
                        }
                    });
        } else if (getItemViewType(position) == EMPTY_VIEW_TYPE) {

        } else if (position < mHeaders.size()) {
            // Headers don't need anything special

        } else if (position < mHeaders.size() + mData.size()) {
            // This is a real position, not a header or footer. Bind it.
            final Object item = mData.get(position - mHeaders.size());
            ViewDataBinding binding = ((BindingViewHolder) holder).getBinding();
            binding.setVariable(com.cwenhui.recyclerview.adapter.BR.item, item);
            binding.setVariable(com.cwenhui.recyclerview.adapter.BR.presenter, getPresenter());
            binding.executePendingBindings();
        } else {
            // Footers don't need anything special
        }
        if (mDecorator != null) {
            mDecorator.decorator(holder, position, getItemViewType(position));
        }
    }

    private int mAutoLoadMoreSize = 1;

    // load more start
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

    private void openLoadMore(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
        mNextLoadEnable = true;
        mLoadMoreEnable = true;
        mLoading = false;
    }

    private void setLoadMoreView(LoadMoreView loadMoreView) {
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

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (isFixedViewType(type)) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    private boolean isFixedViewType(int type) {
        return isHeader(type) || isFooter(type) || type == LOAD_MORE_VIEW_TYPE;
    }

    private void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams)
                    holder.itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    /**
     * if asFlow is true, footer/header will arrange like normal item view.
     * only works when use {@link GridLayoutManager},and it will ignore span size.
     */
    private boolean headerViewAsFlow, footerViewAsFlow;

    public void setHeaderViewAsFlow(boolean headerViewAsFlow) {
        this.headerViewAsFlow = headerViewAsFlow;
    }

    public boolean isHeaderViewAsFlow() {
        return headerViewAsFlow;
    }

    public void setFooterViewAsFlow(boolean footerViewAsFlow) {
        this.footerViewAsFlow = footerViewAsFlow;
    }

    public boolean isFooterViewAsFlow() {
        return footerViewAsFlow;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (isHeader(type) && isHeaderViewAsFlow()) {
                        return 1;
                    }
                    if (isFooter(type) && isFooterViewAsFlow()) {
                        return 1;
                    }
                    if (mSpanSizeLookup == null) {
                        return isFixedViewType(type) ? gridManager.getSpanCount() : 1;
                    } else {
                        return (isFixedViewType(type)) ? gridManager.getSpanCount() : mSpanSizeLookup
                                .getSpanSize(gridManager,
                                        position - mHeaders.size());
                    }
                }
            });
        }
    }

    private SpanSizeLookup mSpanSizeLookup;

    public interface SpanSizeLookup {
        int getSpanSize(GridLayoutManager gridLayoutManager, int position);
    }

    /**
     * @param spanSizeLookup instance to be used to query number of spans occupied by each item
     */
    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        this.mSpanSizeLookup = spanSizeLookup;
    }

    public void setOnLoadMoreListener(RequestLoadMoreListener listener) {
        openLoadMore(listener);
    }

    public interface RequestLoadMoreListener {
        void onLoadMoreRequested();
    }
// load more end

// animation start

    public void setDuration(int duration) {
        mDuration = duration;
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
    private void openLoadAnimation(@CustomRVAdapter.AnimationType int animationType) {
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

// animation end

// empty view start

    /**
     * if show empty view will be return 1 or not will be return 0
     *
     * @return
     */
    public int getEmptyViewCount() {
        if (mEmptyViewLayout == null || mEmptyViewLayout.getChildCount() == 0) {
            return 0;
        }
        if (!mIsUseEmpty) {
            return 0;
        }
        if (mData.size() != 0) {
            return 0;
        }
        return 1;
    }

    /**
     * set emptyView show if adapter is empty and want to show headview and footview
     * Call before {@link RecyclerView#setAdapter(RecyclerView.Adapter)}
     *
     * @param isHeadAndEmpty
     * @param isFootAndEmpty
     */
    public void setHeaderFooterEmpty(boolean isHeadAndEmpty, boolean isFootAndEmpty) {
        mHeadAndEmptyEnable = isHeadAndEmpty;
        mFootAndEmptyEnable = isFootAndEmpty;
    }

    public void setEmptyViewLayout(View emptyView) {
        SimpleItemAnimator animator = (SimpleItemAnimator) getRecyclerView().getItemAnimator();
        if (animator.getSupportsChangeAnimations()) {
            animator.setSupportsChangeAnimations(false);
        }
        boolean neededInsert = false;
        if (mEmptyViewLayout == null) {
            mEmptyViewLayout = new FrameLayout(emptyView.getContext());
            final RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView
                    .LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
            final ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if (lp != null) {
                layoutParams.width = lp.width;
                layoutParams.height = lp.height;
            }
            mEmptyViewLayout.setLayoutParams(layoutParams);
            neededInsert = true;
        }
        mEmptyViewLayout.removeAllViews();
        mEmptyViewLayout.addView(emptyView);
        mIsUseEmpty = true;
        if (getEmptyViewCount() == 1) {
            int position = 0;
            if (mHeadAndEmptyEnable && mHeaders.size() != 0) {
//                position++;
                position += mHeaders.size();
            }
            if (neededInsert) {
                notifyItemInserted(position);
            } else {
                notifyItemChanged(position);
//                notifyDataSetChanged();
            }
        }
    }

// empty view end

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
        void decorator(RecyclerView.ViewHolder holder, int position, int viewType);
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

    public static final class Builder {
        Context mContext;
        RecyclerView mRecyclerView;
        int mLoadMoreViewLayout;

        RecyclerView.LayoutManager mLayoutManager;
        RequestLoadMoreListener mRequestLoadMoreListener;
        int mAutoLoadMoreSize = 1;
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

        public Builder setLoadMoreViewLayout(int loadMoreViewLayout) {
            mLoadMoreViewLayout = loadMoreViewLayout;
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

        public DecorateAdapter build() {
            DecorateAdapter adapter = new DecorateAdapter(mContext);
            if (mLoadMoreView != null) {
                adapter.setLoadMoreView(mLoadMoreView);
            }
            if (mRecyclerView == null) {
                throw new RuntimeException("请确定提供了RecyclerView");
            }
            if (mLayoutManager == null)
                throw new RuntimeException("请确定提供了LayoutManager");
            mRecyclerView.setLayoutManager(mLayoutManager);
            adapter.bindToRecyclerView(mRecyclerView);
            if (mRequestLoadMoreListener != null && mLoadMoreViewLayout != 0) {
                adapter.setOnLoadMoreListener(mRequestLoadMoreListener);
                adapter.setLoadMoreViewLayout(mLoadMoreViewLayout);
            }
            if (isDisableLoadMoreIfNotFullPage) {
                adapter.disableLoadMoreIfNotFullPage();
            }

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

package com.cwenhui.recyclerview.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Super simple multi-type adapter using data-binding.
 *
 * @author markzhai on 16/8/23
 */
public class MultiTypeAdapter extends BaseViewAdapter<Object> {

    public interface MultiViewType {
        int getViewType(Object item);
    }

    protected ArrayList<Integer> mCollectionViewType;

    private ArrayMap<Integer, Integer> mItemTypeToLayoutMap = new ArrayMap<>();

    public MultiTypeAdapter(Context context) {
        this(context, null);
    }

    public MultiTypeAdapter(Context context, Map<Integer, Integer> viewTypeToLayoutMap) {
        super(context);
        mCollection = new ArrayList<>();
        mCollectionViewType = new ArrayList<>();
        if (viewTypeToLayoutMap != null && !viewTypeToLayoutMap.isEmpty()) {
            mItemTypeToLayoutMap.putAll(viewTypeToLayoutMap);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int res = getLayoutRes(viewType);
        ViewDataBinding binding = DataBindingUtil.inflate(mLayoutInflater, res, parent, false);
        return new BindingViewHolder(binding);
    }

    public void addViewTypeToLayoutMap(Integer viewType, Integer layoutRes) {
        mItemTypeToLayoutMap.put(viewType, layoutRes);
    }

    @Override
    public int getItemViewType(int position) {
        Log.e("POS", mCollectionViewType.size() + "--------" + position);
        return mCollectionViewType.get(position);
    }

    protected void set(List viewModels, int viewType) {
        mCollection.clear();
        mCollectionViewType.clear();

        if (viewModels == null) {
            add(null, viewType);
        } else {
            addAll(viewModels, viewType);
        }
    }

    protected void set(List viewModels, MultiViewType viewType) {
        mCollection.clear();
        mCollectionViewType.clear();

        addAll(viewModels, viewType);
    }

    public void add(Object viewModel, int viewType) {
        mCollection.add(viewModel);
        mCollectionViewType.add(viewType);
//      notifyItemInserted(0);
        notifyItemInserted(mCollection.size());
    }

    public void add(int position, Object viewModel, int viewType) {
        mCollection.add(position, viewModel);
        mCollectionViewType.add(position, viewType);
        notifyItemInserted(position);
    }

    public void addAll(List viewModels, int viewType) {
        mCollection.addAll(viewModels);
        for (int i = 0; i < viewModels.size(); ++i) {
            mCollectionViewType.add(viewType);
        }
//        notifyDataSetChanged();
        notifyItemRangeInserted(mCollectionViewType.size()-viewModels.size(), viewModels.size());
    }

    public void addAll(int position, List viewModels, int viewType) {
        mCollection.addAll(position, viewModels);
        for (int i = 0; i < viewModels.size(); i++) {
            mCollectionViewType.add(position + i, viewType);
        }
//        notifyItemRangeChanged(position, viewModels.size() - position);
        notifyItemRangeInserted(position, viewModels.size());
    }

    public void addAll(List viewModels, MultiViewType multiViewType) {
        mCollection.addAll(viewModels);
        for (int i = 0; i < viewModels.size(); ++i) {
            mCollectionViewType.add(multiViewType.getViewType(viewModels.get(i)));
        }
//        notifyDataSetChanged();
        notifyItemRangeInserted(mCollectionViewType.size()-viewModels.size(), viewModels.size());
    }

    public void remove(int position) {
        mCollectionViewType.remove(position);
        super.remove(position);
    }

    public void clear() {
        mCollectionViewType.clear();
        super.clear();
    }

    @LayoutRes
    protected int getLayoutRes(int viewType) {
        int res = mItemTypeToLayoutMap.get(viewType);
        return res;
    }
}
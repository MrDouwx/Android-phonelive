package com.sevenshuyun.phonelive.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sevenshuyun.phonelive.R;
import com.sevenshuyun.phonelive.bean.CoinBean;
import com.sevenshuyun.phonelive.interfaces.OnItemClickListener;
import com.sevenshuyun.phonelive.utils.WordUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/23.
 */

public class CoinAdapter extends HeaderAdapter {

    private List<CoinBean> mList;
    private View.OnClickListener mOnClickListener;
    private String mGiveString;
    private String mCoinName;
    private OnItemClickListener<CoinBean> mOnItemClickListener;

    public CoinAdapter(Context context, int headHeight, String coinName) {
        super(context, headHeight);
        mList = new ArrayList<>();
        mGiveString = WordUtil.getString(R.string.coin_give);
        mCoinName = coinName;
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((CoinBean) tag, 0);
                }
            }
        };
    }

    public void setList(List<CoinBean> list) {
        if (list != null && list.size() > 0) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }


    public void setOnItemClickListener(OnItemClickListener<CoinBean> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateNormalViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_coin, parent, false));
    }

    @Override
    protected void onBindNormalViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        ((Vh) vh).setData(mList.get(position));
    }

    @Override
    protected int getNormalItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mCoin;
        TextView mMoney;
        TextView mGive;

        public Vh(View itemView) {
            super(itemView);
            mCoin = itemView.findViewById(R.id.coin);
            mMoney = itemView.findViewById(R.id.money);
            mGive = itemView.findViewById(R.id.give);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(CoinBean bean) {
            itemView.setTag(bean);
            mCoin.setText(bean.getCoin());
            mMoney.setText("￥" + bean.getMoney());
            if (!"0".equals(bean.getGive())) {
                if (mGive.getVisibility() != View.VISIBLE) {
                    mGive.setVisibility(View.VISIBLE);
                }
                mGive.setText(mGiveString + bean.getGive() + mCoinName);
            } else {
                if (mGive.getVisibility() == View.VISIBLE) {
                    mGive.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}

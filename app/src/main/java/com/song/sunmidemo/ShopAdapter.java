package com.song.sunmidemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<String[]> listItem;
    private Demo context;

    public ShopAdapter(Context context, ArrayList<String[]> listItem) {
        this.mInflater = LayoutInflater.from(context);
        this.listItem = listItem;
        this.context = (Demo)context;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public Object getItem(int pos) {
        return listItem.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        View item = mInflater.inflate(R.layout.shop_item, null);
        TextView name = item.findViewById(R.id.name),
                price = item.findViewById(R.id.price),
                goodNum = item.findViewById(R.id.goodNum);
        String[] arr = listItem.get(pos);
        name.setText(arr[Demo.NAME_POS]);
        price.setText("￥" + arr[Demo.PRICE_POS]);
        goodNum.setText(arr[Demo.NUM_POS]);
        ImageView minus = item.findViewById(R.id.minusGoodNum),
                add = item.findViewById(R.id.addGoodNum);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] itemParams = listItem.get(pos);
                int goodNum = Integer.valueOf(itemParams[Demo.NUM_POS]);
                if (goodNum > 1)
                    itemParams[Demo.NUM_POS] = goodNum - 1 + "";
                else
                    listItem.remove(pos);
                ShopAdapter.this.notifyDataSetChanged();
                context.getAllGoodPrice();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] itemParams = listItem.get(pos);
                itemParams[Demo.NUM_POS] = Integer.valueOf(itemParams[Demo.NUM_POS]) + 1 + "";
                ShopAdapter.this.notifyDataSetChanged();
                context.getAllGoodPrice();
            }
        });
        return item;
    }
}

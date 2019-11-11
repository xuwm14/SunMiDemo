package com.song.sunmidemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ProductList extends Dialog {
    public View view;

    public ProductList(Context context) {
        super(context);
    }

    public ProductList(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        public Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public ProductList create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final ProductList dialog = new ProductList(context);
            View layout = inflater.inflate(R.layout.shop_cart, null);
            dialog.view = layout;
            dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            return dialog;
        }
    }
}

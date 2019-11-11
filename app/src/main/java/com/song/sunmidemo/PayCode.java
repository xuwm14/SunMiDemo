package com.song.sunmidemo;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PayCode extends Dialog {
    public View view;

    public PayCode(Context context) {
        super(context);
    }

    public PayCode(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        public Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public PayCode create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final PayCode dialog = new PayCode(context);
            View layout = inflater.inflate(R.layout.pay_code, null);
            dialog.view = layout;
            dialog.addContentView(layout, new ViewGroup.LayoutParams(400, 400));

            return dialog;
        }
    }
}

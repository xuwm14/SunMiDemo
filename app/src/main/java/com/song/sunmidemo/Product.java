package com.song.sunmidemo;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class Product extends LinearLayout implements View.OnTouchListener, View.OnClickListener {
    private Context mContext;
    private View mView;
    public String productId;

    public Product(final Context context, String productId, String productName, String productPrice, final String picUrl) {
        this(context, null, 0);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.product, this, true);
        mView.setOnClickListener(this);
        mView.setOnTouchListener(this);
        TextView nameView = mView.findViewById(R.id.productName),
                priceView = mView.findViewById(R.id.productPrice);
        nameView.setText(productName);
        priceView.setText("￥" + productPrice);
        this.productId = productId;

        final ImageView imageView = mView.findViewById(R.id.productImg);
        final Bitmap bitmap = Demo.getImgFromSQL(mContext, picUrl);
        final Demo demo = (Demo)mContext;
        if (bitmap != null) {
            demo.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        } else {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder().url(picUrl).build();
            try {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        return;
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        byte[] result = response.body().bytes();
                        Demo.storeImgToSQL(mContext, picUrl, result);
                        final Bitmap img = BitmapFactory.decodeByteArray(result, 0, result.length);
                        demo.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(img);
                            }
                        });
                    }
                });//发送请求
            } catch (Exception e) {

            }
        }
    }

    public Product(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mView.setBackgroundResource(R.drawable.copy_shape);
        } else {
            mView.setBackgroundResource(R.drawable.shape_corner);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        ((Demo)mContext).addProductToCart(productId);
    }
}

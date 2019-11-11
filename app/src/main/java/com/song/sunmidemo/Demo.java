package com.song.sunmidemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.QMUIFloatLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Demo extends Activity implements View.OnClickListener {

    private int screenWidth, screenHeight;
    public final static int ID_POS = 0, NAME_POS = 1, PRICE_POS = 2, NUM_POS = 3, IMG_POS = 3, TOTAL_NUM = 4;

    ProductList shopCart;
    PayCode payCode;

    NumImageView numImageView, numImageView2;
    TextView shopPrice, shopPrice2, shopPrice3;
    ArrayList<String[]> goodParamsList;
    ShopAdapter goodListAdapter;
    HashMap<String, String[]> productList;

    Bitmap codePic;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.demo);

        WindowManager wm = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();

        ProductList.Builder listBuilder = new ProductList.Builder(this);
        shopCart = listBuilder.create();
        Window window = shopCart.getWindow();
        window.setGravity(Gravity.BOTTOM);

        numImageView = findViewById(R.id.shopCnt);
        numImageView2 = shopCart.view.findViewById(R.id.shopCnt);
        shopPrice = findViewById(R.id.shopPrice);
        shopPrice2 = shopCart.view.findViewById(R.id.shopPrice);
        shopPrice3 = shopCart.view.findViewById(R.id.total_price);

        window.getDecorView().setPadding(0, 0, 0, 0);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.height = (int)(screenHeight * 0.5);
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        window.getDecorView().setBackgroundColor(Color.parseColor("#FFFFFF"));

        setShopCnt(10);
        setShopPrice(99.67);

        goodParamsList = new ArrayList<>();
        goodListAdapter = new ShopAdapter(this, goodParamsList);
        ListView lv = (ListView)shopCart.view.findViewById(R.id.list1);
        lv.setAdapter(goodListAdapter);
        getAllGoodPrice();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String code = intent.getStringExtra("data");
                if (code != null && !code.isEmpty()) {
                    Log.e("扫描结果", code);
                    shopCart.show();
                    addProductToCart(code);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED");
        registerReceiver(receiver, filter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url("http://39.97.232.16:10000/products/page-num/1/page-size/10000").build();
                try {
                    Response response = client.newCall(request).execute();//发送请求
                    String result = response.body().string();
                    JSONArray jsonArray = new JSONObject(result).getJSONArray("products");
                    productList = new HashMap<>();
                    final QMUIFloatLayout productListView = findViewById(R.id.product_list);
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String[] productParams = new String[TOTAL_NUM];
                        productParams[ID_POS] = jsonObject.getString("barcode");
                        productParams[NAME_POS] = jsonObject.getString("name");
                        productParams[IMG_POS] = jsonObject.getString("pic");
                        productParams[PRICE_POS] = (double)jsonObject.getInt("price") / 100 + "";
                        productList.put(productParams[ID_POS], productParams);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addItemToFloatLayout(productListView, productList);
                        }
                    });
                    Log.e("result", jsonArray.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void addItemToFloatLayout(QMUIFloatLayout floatLayout, HashMap<String, String[]> productList) {
        for (String[] productParams : productList.values()) {
            Product p = new Product(this, productParams[ID_POS], productParams[NAME_POS], productParams[PRICE_POS], productParams[IMG_POS]);
            int width = (screenWidth - 160) / 4,
                    height = (int) (width * 1.5);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(width, height);
            floatLayout.addView(p, lp);
        }
    }

    public void getAllGoodPrice() {
        double totalPrice = 0.0;
        int totalNum = 0;
        for (String[] goodParams:goodParamsList) {
            double price = Double.valueOf(goodParams[PRICE_POS]);
            int num = Integer.valueOf(goodParams[NUM_POS]);
            totalPrice += price * num;
            totalNum += num;
        }
        DecimalFormat df = new DecimalFormat("0.00");
        totalPrice = Double.valueOf(df.format(totalPrice));
        setShopPrice(totalPrice);
        setShopCnt(totalNum);
        goodListAdapter.notifyDataSetChanged();
    }

    public void setShopCnt(int num) {
        numImageView.setNum(num);
        numImageView2.setNum(num);
    }

    public void setShopPrice(double price) {
        String str = "￥" + price;
        shopPrice.setText(str);
        shopPrice2.setText(str);
        shopPrice3.setText(str);
    }

    public void addProductToCart(String goodId) {
        goodId = goodId.trim();
        boolean goodExist = false;
        for (int i = 0; i < goodParamsList.size(); ++i) {
            String[] params = goodParamsList.get(i);
            if (params[ID_POS].equals(goodId)) {
                goodExist = true;
                int num = Integer.valueOf(params[NUM_POS]);
                params[NUM_POS] = num + 1 + "";
                break;
            }
        }
        if (!goodExist) {
            if (productList.containsKey(goodId)) {
                String[] productParams = productList.get(goodId);
                String[] goodParams = new String[TOTAL_NUM];
                goodParams[0] = productParams[0];
                goodParams[1] = productParams[1];
                goodParams[2] = productParams[2];
                goodParams[3] = 1 + "";
                goodParamsList.add(goodParams);
            } else {
                Toast toast = Toast.makeText(this, "该商品不存在！", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
        getAllGoodPrice();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openShop:
                if (shopCart.isShowing())
                    shopCart.dismiss();
                else
                    shopCart.show();
                break;
            case R.id.cleanShopCart:
                goodParamsList.clear();
                getAllGoodPrice();
                goodListAdapter.notifyDataSetChanged();
                break;
            case R.id.pay:
                if (goodParamsList.size() == 0) {
                    Toast toast = Toast.makeText(this, "购物车为空！", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject();
                                JSONArray array = new JSONArray();
                                for (String[] params:goodParamsList) {
                                    JSONObject param = new JSONObject();
                                    param.put("barcode", params[ID_POS]);
                                    param.put("num", params[NUM_POS]);
                                    array.put(param);
                                }
                                object.put("order_detail_list", array);
                                MediaType jsonType = MediaType.parse("application/json; charset=utf-8");
                                RequestBody body = RequestBody.create(jsonType, object.toString());
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url("http://39.97.232.16:10000/order").put(body).build();
                                Response response = client.newCall(request).execute();//发送请求
                                String result = response.body().string();
                                codePic = getImgFromSQL(Demo.this, result);
                                if (codePic == null) {
                                    final Request request2 = new Request.Builder().url(result).build();
                                    Response response2 = client.newCall(request2).execute();//发送请求
                                    byte[] picBytes = response2.body().bytes();
                                    storeImgToSQL(Demo.this, result, picBytes);
                                    codePic = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
                                }
                                Demo.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        PayCode.Builder payCodeBuilder = new PayCode.Builder(Demo.this);
                                        payCode = payCodeBuilder.create();
                                        ImageView codeImg = payCode.findViewById(R.id.code_img);
                                        codeImg.setImageBitmap(codePic);
                                        payCode.show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }

        }
    }

    public static Bitmap getImgFromSQL(Context context, String picUrl) {
        final SQLiteHelper pics = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = pics.getWritableDatabase();
        String query = "select * from Pics where name = '" + picUrl + "'";
        Cursor rlt = db.rawQuery(query, null);
        if (rlt.moveToNext()) {
            byte[] pic = rlt.getBlob(rlt.getColumnIndex("avatar"));
            final Bitmap img = BitmapFactory.decodeByteArray(pic, 0, pic.length);
            return img;
        } else
            return null;
    }

    public static void storeImgToSQL(Context context, String picUrl, byte[] pic) {
        final SQLiteHelper pics = SQLiteHelper.getInstance(context);
        SQLiteDatabase db = pics.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("name", picUrl);
        cv.put("avatar", pic);
        db.insert("Pics", null, cv);
        db.close();
    }
}

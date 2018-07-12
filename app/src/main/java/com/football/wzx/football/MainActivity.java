package com.football.wzx.football;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private MyDatabaseHelper dbHelper;
    private static final String TABLE_NAME="football";
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper=new MyDatabaseHelper(MainActivity.this);
        db =  dbHelper.getReadableDatabase();

        webView = (WebView) findViewById(R.id.wv_main);
//        webView.loadUrl("http://205.201.1.200");
        webView.loadUrl("file:///android_asset/output_form.html");
        //开启js
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new JSBridge(),"jsbridge");
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url) {
                //getFootTableList();
                //Toast.makeText(MainActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity. this, url, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                // 当开启新的页面的时候用webview来进行处理而不是用系统自带的浏览器处理
                //view.loadUrl(url);
                return true;
            }

            public void onLoadResource(WebView view, String url) {
                //String str = "document.getElementsByTagName(\"body\")[0].innerHTML";
                //view.loadUrl("javascript:alert("+str+");");
                //Toast.makeText(MainActivity.this, url, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getFootTableList(){
        //获取数据
        Cursor cursor= db.query(TABLE_NAME,new String[]{"pl","je","ms","jesum","createtime"},"",null,null,null,"createtime desc");

        String sql = "select count(*) as c from football where je = '100000'" ;
        Cursor newCursor = db.rawQuery(sql, null);
        boolean result = false;
        if(newCursor.moveToNext()){
            int count = newCursor.getInt(0);
            if(count>0){
                result = true;
            }
        }
        Log.i("aaa",String.valueOf(result));
        JSONArray jsonArray = new JSONArray();
        JSONObject tmpObj = null;
        while(cursor.moveToNext()) {
            String pl = cursor.getString(cursor.getColumnIndex("pl"));
            String je = cursor.getString(cursor.getColumnIndex("je"));
            String ms = cursor.getString(cursor.getColumnIndex("ms"));
            String jesum = cursor.getString(cursor.getColumnIndex("jesum"));
            String createtime = cursor.getString(cursor.getColumnIndex("createtime"));
            tmpObj = new JSONObject();
            try {
                tmpObj.put("pl",pl);
                tmpObj.put("je",je);
                tmpObj.put("ms",ms);
                tmpObj.put("jesum",jesum);
                tmpObj.put("createtime",createtime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(tmpObj);
        }
        webView.loadUrl("javascript:showList("+jsonArray+")");
    }

    private class JSBridge {
        //消息弹出层
        @JavascriptInterface
        public void alertMsg(String alertMsg){
            Toast.makeText(MainActivity.this, alertMsg, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void getDataList(){
            getFootTableList();
        }

        @JavascriptInterface
        public int setFootData(String json){
            JSONObject jsonObject;
            long time = System.currentTimeMillis() / 1000;//获取系统时间的10位的时间戳
            String createtime = String.valueOf(time);

            try {
                jsonObject = new JSONObject(json);
                //新增数据
                ContentValues values = new ContentValues();
                values.put("je",jsonObject.optString("je"));
                values.put("pl",jsonObject.optString("pl"));
                values.put("ms",jsonObject.optString("ms"));
                values.put("jesum",jsonObject.optString("jesum"));
                values.put("createtime",createtime);
                db.insert(TABLE_NAME,null,values);
                values.clear();
                Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                return 1;
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return 0;
            }
        }
    }
}

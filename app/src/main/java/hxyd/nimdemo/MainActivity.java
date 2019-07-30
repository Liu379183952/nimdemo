package hxyd.nimdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Map;

import hxyd.mynimdemo.NimDemo;

public class MainActivity extends Activity {
    private Button btn_zxzx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_zxzx = (Button) findViewById(R.id.btn_zxzx);

        btn_zxzx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> params = BaseApp.getInstance().getZdyRequestParams("5751");
                params.put("wkfFlag","1");
                params.put("userid","");
                NimDemo.getNimDemo().getTokenLogin(MainActivity.this,params,"http://zt.wx.pangjiachen.com/miapp/TEST.A0117/gateway");
            }
        });
    }
}

package com.netease.nim.uikit;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by 刘野 on 2018/5/3.
 */

public class MyDialog_zxzx extends Dialog {
    private Button yes;//确定按钮
    private Button no;//取消按钮
    private TextView titleTv;//消息标题文本
    private String titleStr;//从外界设置的title文本
    protected ListView lv;
    //确定文本和取消文本的显示内容
    private String yesStr, noStr;
    private List<ScoreBean> mList;

    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    /**
     29.     * 设置取消按钮的显示内容和监听
     30.     *
     31.     * @param str
     32.     * @param onNoOnclickListener
     33.     */
    public void setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
               if (str != null) {
                      noStr = str;
                    }
             this.noOnclickListener = onNoOnclickListener;
           }
    /**
     42.     * 设置确定按钮的显示内容和监听
     43.     *
     44.     * @param str
     45.     * @param onYesOnclickListener
     46.     */
    public void setYesOnclickListener(String str, onYesOnclickListener onYesOnclickListener) {
                if (str != null) {
                        yesStr = str;
                   }
                this.yesOnclickListener = onYesOnclickListener;
            }

    public MyDialog_zxzx(Context context) {
        super(context, R.style.MyDialog);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.item_dialog_zxzx);
               //按空白处不能取消动画
                setCanceledOnTouchOutside(false);

               //初始化界面控件
                initView();
                //初始化界面数据
               initData();
                //初始化界面控件的事件
                initEvent();

            }
    /**
    * 初始化界面的确定和取消监听器
    */
  private void initEvent() {
              //设置确定按钮被点击后，向外界提供监听
              yes.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View v) {
                               if (yesOnclickListener != null) {
                                     yesOnclickListener.onYesClick();
                                    }
                           }
        });
             //设置取消按钮被点击后，向外界提供监听
              no.setOnClickListener(new View.OnClickListener() {
           @Override
          public void onClick(View v) {
                                if (noOnclickListener != null) {
                                      noOnclickListener.onNoClick();
                                    }
                           }
      });
           }
/**
     * 初始化界面控件的显示数据
      */
  private void initData() {
              //如果用户自定了title和
              if (mList.size() != 0){
                  ScoreAdapter mAdapter = new ScoreAdapter(getContext(), mList);
                  lv.setAdapter(mAdapter);
                  mAdapter.notifyDataSetChanged();
              }
               if (titleStr != null) {
                      titleTv.setText(titleStr);
                  }
               //如果设置按钮的文字
              if (yesStr != null) {
                      yes.setText(yesStr);
                   }
                if (noStr != null) {
                      no.setText(noStr);
                   }
            }
        /**
        * 初始化界面控件
        */
        private void initView() {
            yes = (Button) findViewById(R.id.yes);
            no = (Button) findViewById(R.id.no);
            titleTv = (TextView) findViewById(R.id.title);
            lv = (ListView) findViewById(R.id.scoreListview);
          }
/**
    * 从外界Activity为Dialog设置标题
     *
    * @param title
    */
    public void setTitle(String title) {
               titleStr = title;
          }
    public void setmList(List<ScoreBean> list) {
            mList = list;
        }

/**
    * 从外界Activity为Dialog设置View
     *
    */
    public ListView getListView() {
        return lv;
    }
       /*
      * 设置确定按钮和取消被点击的接口
      */
           public interface onYesOnclickListener {
        public void onYesClick();
   }

            public interface onNoOnclickListener {
        public void onNoClick();
  }

}

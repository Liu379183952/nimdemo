package com.netease.nim.uikit;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by 刘野 on 2018/5/3.
 */

public class MyDialog_AvChat extends Dialog {
    private ImageView yes;//确定按钮
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器

    /**
     * 42.     * 设置确定按钮的显示内容和监听
     * 43.     *
     * 44.     * @param str
     * 45.     * @param onYesOnclickListener
     * 46.
     */
    public void setYesOnclickListener(onYesOnclickListener onYesOnclickListener) {

        this.yesOnclickListener = onYesOnclickListener;
    }

    public MyDialog_AvChat(Context context) {
        super(context, R.style.MyDialog1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_dialog_avchat);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        //初始化界面控件
        initView();
        //初始化界面数据
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
    }

    /**
     * 初始化界面控件的显示数据
     */

    /**
     * 初始化界面控件
     */
    private void initView() {
        yes = (ImageView) findViewById(R.id.yes);
    }
    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onYesOnclickListener {
        public void onYesClick();
    }
}

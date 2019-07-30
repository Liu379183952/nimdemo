package com.netease.nim.uikit;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by admin on 2019/5/21.
 */

public class MyToolBar extends Toolbar {
    //布局
    private LayoutInflater mInflater;
    //右边按钮
    private ImageView mRightButton;
    //左边按钮
    private ImageView mLeftButton;
    //标题
    private TextView mTextTitle;

    private View view;

    public MyToolBar(Context context) {
        this(context, null);
    }

    public MyToolBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化函数
        initView();
        setContentInsetsRelative(10, 10);
        if (attrs != null) {
            setLeftButtonIcon(R.drawable.nim_actionbar_dark_back_icon);//设置左图标
            //设置点击事件
            setLeftButtonOnClickLinster(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getContext(), "left", Toast.LENGTH_SHORT).show();
                }
            });
            setRightButtonIcon(R.drawable.nim_actionbar_dark_back_icon);//设置右图标
            //设置点击事件
            setRightButtonOnClickLinster(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getContext(), "right", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initView() {
        if (view == null) {
            //初始化
            mInflater = LayoutInflater.from(getContext());
            //添加布局文件
            view = mInflater.inflate(R.layout.actionbar_header, null);
            //绑定控件
            mTextTitle = (TextView) view.findViewById(R.id.title);
            mLeftButton = (ImageView) view.findViewById(R.id.back);
            mRightButton = (ImageView) view.findViewById(R.id.home);

            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
            addView(view, layoutParams);
        }
    }

    public void setRightButtonIcon(int icon) {

        if (mRightButton != null) {

            mRightButton.setImageResource(icon);
            // mRightButton.setVisibility(VISIBLE);
        }

    }
    public void setTitle(String title) {
        if (mTextTitle != null) {
           mTextTitle.setText(title);
        }

    }

    public void setLeftButtonIcon(int icon) {

        if (mLeftButton != null) {

            mLeftButton.setImageResource(icon);
            //mLeftButton.setVisibility(VISIBLE);
        }

    }

    //设置右侧按钮监听事件
    public void setRightButtonOnClickLinster(OnClickListener linster) {
        mRightButton.setOnClickListener(linster);
    }

    //设置左侧按钮监听事件
    public void setLeftButtonOnClickLinster(OnClickListener linster) {
        mLeftButton.setOnClickListener(linster);
    }
}
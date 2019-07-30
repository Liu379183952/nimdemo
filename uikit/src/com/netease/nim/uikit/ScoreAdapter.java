package com.netease.nim.uikit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ScoreAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ScoreBean> mList;
	
	public ScoreAdapter(Context context, List<ScoreBean> list) {
		mContext = context;
		mList = list;
		mInflater = LayoutInflater.from(mContext);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_score_list, parent, false);
			holder = new ViewHolder();
			holder.textContext = (TextView) view.findViewById(R.id.score);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.textContext.setText(mList.get(position).getOption());
		return view;
	}

	private class ViewHolder {
		TextView textContext;
	}
}

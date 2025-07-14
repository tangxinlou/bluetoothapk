package com.example.bluetooth;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.view.Menu;
import android.widget.TextView;
import android.graphics.Color;
public class TableAdapter extends BaseAdapter {
	private static final String TAG = "TableAdapter";
	private List<BtResultData> list;
	private List<Integer> list_filted = new ArrayList<>();
	private LayoutInflater inflater;
	private Context context;
	private int logLevel = Log.VERBOSE;
    public static final int loglevel_menu=0x7f0c0000;
	public TableAdapter(Context context, List<BtResultData> list){
		this.list = list;
		inflater = LayoutInflater.from(context);
		this.context = context;
	}
	public void updateData( List<BtResultData> data) {
		this.list = data;
		genListFilted();
	}
	public int getCount() {
		int ret = 0;
		if (logLevel > Log.VERBOSE) {
			ret = list_filted.size();
		} else {
			if(list != null){
				ret = list.size();
			}
		}
		return ret;
	}
	public Object getItem(int position) {
		if (logLevel > Log.VERBOSE) {
			return list.get(list_filted.get(position));
		} else {
			return list.get(position);
		}
	}
	public long getItemId(int position) {
		return position;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		BtResultData BtResultData = (BtResultData) this.getItem(position);
		ViewHolder viewHolder;
		if(convertView == null || !(convertView.getTag() instanceof ViewHolder)){
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.btlist_item, null);
			viewHolder.BtResultID = (TextView) convertView.findViewById(R.id.index);
			viewHolder.BtResultStamp = (TextView) convertView.findViewById(R.id.time_stamp);
			viewHolder.BtResult = (TextView) convertView.findViewById(R.id.result);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.BtResultID.setText(BtResultData.getId());
		viewHolder.BtResultID.setTextSize(13);
		viewHolder.BtResultID.setTextColor(Color.BLACK);
		viewHolder.BtResultStamp.setText(BtResultData.getTimeStamp());
		viewHolder.BtResultStamp.setTextSize(13);
		viewHolder.BtResultStamp.setTextColor(Color.BLACK);
		viewHolder.BtResult.setText(BtResultData.getResult());
		viewHolder.BtResult.setTextSize(13);
		viewHolder.BtResult.setTextColor(Color.BLACK);
		if (0 == position) {
			Drawable drawable = context.getResources().getDrawable(android.R.drawable.ic_menu_sort_by_size);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			viewHolder.BtResult.setCompoundDrawables(null, null, drawable, null);
			viewHolder.BtResult.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					showLogLevelMenu(viewHolder.BtResult);
				}
			});
		} else {
			viewHolder.BtResult.setCompoundDrawables(null, null, null, null);
		}
		if (position % 2 == 0) {
			convertView.setBackgroundColor(Color.parseColor("#B2DFEE"));
		} else {
			convertView.setBackgroundColor(Color.parseColor("#87CEEB"));
		}
		String index = "INDEX";
		if (index.equals(viewHolder.BtResultID.getText().toString())) {
			convertView.setBackgroundColor(Color.parseColor("#ff0099cc"));
		}
		String result = viewHolder.BtResult.getText().toString();
		if (BtResultData.status >= Log.ASSERT) {
			convertView.setBackgroundColor(Color.parseColor("#FF0000"));  //red
		} else if ((result != null && result.startsWith("FAIL")) || BtResultData.status >= Log.ERROR) {
			convertView.setBackgroundColor(Color.parseColor("#FF8000"));  //orange
		} else if (BtResultData.status == Log.WARN) {
			convertView.setBackgroundColor(Color.parseColor("#FFFF00"));  //yellow
		}
		return convertView;
	}
	public static class ViewHolder{
		public TextView BtResultID;
		public TextView BtResultStamp;
		public TextView BtResult;
	}
	private void showLogLevelMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(context, view);
        Menu menu = popupMenu.getMenu();
        menu.add(0, 0, 0, "ERROR");
        menu.add(0, 1, 1, "WARN");
        menu.add(0, 2, 2, "INFO");
        menu.add(0, 3, 3, "DEBUG");
        menu.add(0, 4, 4, "VERBOSE");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				int prev = logLevel;
				switch (item.getItemId()) {
					case 0:
						logLevel = Log.ERROR;
						break;
					case 1:
						logLevel = Log.WARN;
						break;
					case 2:
						logLevel = Log.INFO;
						break;
					case 3:
						logLevel = Log.DEBUG;
						break;
					case 4:
						logLevel = Log.VERBOSE;
						break;
				}
				if (prev != logLevel) {
					genListFilted();
					TableAdapter.this.notifyDataSetChanged();
				}
				return true;
			}
		});
		popupMenu.show();
	}
	private void genListFilted() {
		if (logLevel <= Log.VERBOSE) return;
		list_filted.clear();
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				list_filted.add(i);
				continue;
			}
			if (list.get(i).status >= logLevel) {
				list_filted.add(i);
			}
		}
	}
}

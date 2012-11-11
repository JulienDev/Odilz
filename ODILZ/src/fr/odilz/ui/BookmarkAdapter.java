package fr.odilz.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import fr.odilz.application.OdilzApplication;
import fr.odilz.model.Place;

public class BookmarkAdapter extends BaseAdapter {

	ArrayList<Place> mBookmarks;
	
	Context mContext;

	public BookmarkAdapter(Context context) {
		mContext = context;
		mBookmarks = OdilzApplication.mBookmarks;
	}

	@Override
	public int getCount() {
		return mBookmarks.size();
	}

	@Override
	public Object getItem(int position) {
		return mBookmarks.toArray()[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new BookmarkView(mContext);
		}
		((BookmarkView) convertView).setData(mBookmarks.get(position));
		return convertView;
	}
}
package fr.odilz.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.odilz.R;
import fr.odilz.model.Place;

public class BookmarkView extends LinearLayout {

	private Context mContext;
	private final TextView mPlaceName;

	public BookmarkView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.list_item_bookmark, this);
		mContext = context;
		mPlaceName = (TextView) findViewById(R.id.place_name);
	}

	public void setData(Place place) {
		mPlaceName.setText(place.name);
	}
}
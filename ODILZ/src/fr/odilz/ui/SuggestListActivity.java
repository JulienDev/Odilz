package fr.odilz.ui;

import java.util.ArrayList;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import fr.odilz.R;
import fr.odilz.application.OdilzApplication;
import fr.odilz.model.Hobbies;
import fr.odilz.model.Place;
import fr.odilz.model.SuggestType;
import fr.odilz.util.BookmarkUtils;
import fr.odilz.util.ImageManager;

public class SuggestListActivity extends SherlockFragmentActivity{

	private Context mContext;
	private ImageManager mImageManager;
	private ListView mListView;
	private SuggestAdapter mAdapter;
	private MenuDrawerManager mMenuDrawer;
	private ArrayList<Place> mBookmarks;
	private SuggestType[] suggestTypes; 
	private ListView mBookmarksList;
	private BookmarkAdapter mBookmarkAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		
		mImageManager = OdilzApplication.getImageManager(this);
		mBookmarks = OdilzApplication.mBookmarks;
		suggestTypes = OdilzApplication.mPlaces;

		mMenuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_WINDOW);
		mMenuDrawer.setContentView(R.layout.activity_list_suggest);
		
		View bookmarksView = LayoutInflater.from(this).inflate(R.layout.view_bookmark, null);
		ImageButton bookmarksShare = (ImageButton) bookmarksView.findViewById(R.id.bookmarksShare);
		bookmarksShare.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				BookmarkUtils.shareBookmarks(mContext);
			}
		});
		mBookmarksList = (ListView) bookmarksView.findViewById(R.id.bookmarksList);
		mBookmarkAdapter = new BookmarkAdapter(this);
		mBookmarksList.setAdapter(mBookmarkAdapter);
		mBookmarksList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				OdilzApplication.mPlace = mBookmarks.get(position);
				Intent intent = PlaceActivity.getIntent(mContext);
				startActivity(intent);	
			}
		});
		mMenuDrawer.setMenuView(bookmarksView);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mListView = (ListView) findViewById(R.id.suggests_list);
		mAdapter = new SuggestAdapter(this);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_list_suggest, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mBookmarkAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenuDrawer.toggleMenu();
			break;
		case R.id.map:
			Intent intent = new Intent(this, SuggestMapActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		final int drawerState = mMenuDrawer.getDrawerState();
		if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
			mMenuDrawer.closeMenu();
			return;
		}

		super.onBackPressed();
	}
	
	private class SuggestAdapter extends BaseAdapter {

		Context mContext;

		public SuggestAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return suggestTypes.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new SuggestView(mContext);
			}
			((SuggestView) convertView).setData(suggestTypes[position]);
			return convertView;
		}
	}

	private class SuggestView extends LinearLayout implements ImageManager.OnImageDownloaderListener {

		private Context mContext;
		private final LinearLayout mSuggestScrollContent;
		private final TextView mSuggestType;

		public SuggestView(Context context) {
			super(context);

			LayoutInflater.from(context).inflate(R.layout.list_item_suggest, this);

			mContext = context;

			mSuggestScrollContent = (LinearLayout) findViewById(R.id.suggest_scroll_content);
			mSuggestType = (TextView) findViewById(R.id.suggest_type);
		}

		public void setData(SuggestType suggestType) {

			Hobbies hobby = Hobbies.getTypeById(Integer.parseInt(suggestType.type));
			mSuggestType.setText(hobby.name);
			
			if (suggestType.places.length == 0) {
				TextView textView = new TextView(mContext);
				textView.setText(R.string.place_nothing_found);
				mSuggestScrollContent.addView(textView);
				return;
			}
			
			for (int i=0; i< suggestType.places.length; i++) {
				final Place place = suggestType.places[i];
				View viewPlace = LayoutInflater.from(mContext).inflate(R.layout.scroll_item_suggest, null);
				viewPlace.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						OdilzApplication.mPlace = place;
						Intent intent = PlaceActivity.getIntent(mContext);
						startActivity(intent);
					}
				});
				((TextView) viewPlace.findViewById(R.id.place_name)).setText(place.name);
				ImageView suggestImage = (ImageView) viewPlace.findViewById(R.id.suggest_image);
				if (hobby.placeholderId != 0) {
					suggestImage.setImageResource(hobby.placeholderId);
				}
//				mImageManager.download(this, suggestImage, "http://figueetsardine.files.wordpress.com/2011/08/01-terrasse-jour.jpg", true);
//				CheckBox suggestBookmark = (CheckBox) viewPlace.findViewById(R.id.suggest_bookmark);
//				suggestBookmark.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//					@Override
//					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//						
//					}
//				});
				mSuggestScrollContent.addView(viewPlace);
			}
		}

		@Override
		public void onImageDownloadSuccess(Bitmap bitmap, ImageView imageView, String url) {
			imageView.setImageBitmap(bitmap);
		}

		@Override
		public void onImageDownloadFail() { }
	}
}
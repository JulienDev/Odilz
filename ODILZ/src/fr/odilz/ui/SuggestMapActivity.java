package fr.odilz.ui;

import java.util.ArrayList;
import java.util.HashMap;

import net.simonvt.widget.MenuDrawer;
import net.simonvt.widget.MenuDrawerManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cyrilmottier.polaris.Annotation;
import com.cyrilmottier.polaris.MapCalloutView;
import com.cyrilmottier.polaris.MapViewUtils;
import com.cyrilmottier.polaris.PolarisMapView;
import com.cyrilmottier.polaris.PolarisMapView.OnAnnotationSelectionChangedListener;

import fr.odilz.R;
import fr.odilz.application.OdilzApplication;
import fr.odilz.model.Hobbies;
import fr.odilz.model.Place;
import fr.odilz.model.SuggestType;
import fr.odilz.util.BookmarkUtils;
import fr.odilz.util.LatLonPoint;

public class SuggestMapActivity extends SherlockMapActivity {

	private Context mContext;
	private ActionBar mActionbar;
	private PolarisMapView mMapView;
	private MenuDrawerManager mMenuDrawer;
	private ArrayList<Place> mBookmarks;
	private ListView mBookmarksList;
	private BookmarkAdapter mBookmarkAdapter;
	private HashMap<Integer, Place> mPlacesHash = new HashMap<Integer, Place>();

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		mContext = this;

		mBookmarks = OdilzApplication.mBookmarks;

		mMenuDrawer = new MenuDrawerManager(this, MenuDrawer.MENU_DRAG_WINDOW);
		mMenuDrawer.setContentView(R.layout.activity_map_suggest);

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

		mActionbar = getSupportActionBar();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mMapView = (PolarisMapView) findViewById(R.id.polaris_map_view);
		mMapView.setUserTrackingButtonEnabled(true);

		final SuggestType[] mSuggestTypes = OdilzApplication.mPlaces;

		int i = 0;
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();

		for (final SuggestType suggestType : mSuggestTypes) {

			final Hobbies hobby = Hobbies.getTypeById(Integer.parseInt(suggestType.type));
			Log.d("hobby", "hobby:" + hobby.name);

			for (Place place : suggestType.places) {
				mPlacesHash.put(i, place);
				i++;

				LatLonPoint geoPoint = new LatLonPoint(place.latitude, place.longitude);
				Annotation annotation = new Annotation(geoPoint, place.name);
				final Drawable altMarker = MapViewUtils.boundMarkerCenterBottom(getResources().getDrawable(hobby.pinId));
				annotation.setMarker(altMarker);
				annotations.add(annotation);
			}
			mMapView.setOnAnnotationSelectionChangedListener(new OnAnnotationSelectionChangedListener() {

				@Override
				public void onAnnotationSelected(PolarisMapView mapView,
						MapCalloutView calloutView, int position, Annotation annotation) {
					// TODO Auto-generated method stub	
				}

				@Override
				public void onAnnotationDeselected(PolarisMapView mapView,
						MapCalloutView calloutView, int position, Annotation annotation) {
					// TODO Auto-generated method stub	
				}

				@Override
				public void onAnnotationClicked(PolarisMapView mapView,
						MapCalloutView calloutView, int position, Annotation annotation) {
					//					OdilzApplication.mPlace = suggestType.places[position];
					OdilzApplication.mPlace = mPlacesHash.get(position);
					Intent intent = PlaceActivity.getIntent(mContext);
					startActivity(intent);
				}
			});
			//			mMapView.setAnnotations(annotations, R.drawable.map_pin_blue);

		}
		mMapView.setAnnotations(annotations, R.drawable.map_pin_blue);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_map_suggest, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenuDrawer.toggleMenu();
			break;
		case R.id.list:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMapView.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mMapView.onStop();
	}


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
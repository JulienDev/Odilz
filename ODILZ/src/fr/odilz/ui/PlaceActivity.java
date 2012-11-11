package fr.odilz.ui;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cyrilmottier.polaris.Annotation;
import com.cyrilmottier.polaris.PolarisMapView;
import com.google.android.maps.MapController;

import fr.odilz.R;
import fr.odilz.application.OdilzApplication;
import fr.odilz.model.Place;
import fr.odilz.util.LatLonPoint;

public class PlaceActivity extends SherlockMapActivity implements ActionBar.TabListener {

	private static final String EXTRA_PLACE_POSITION = "position";

	private ActionBar mActionbar;
	private PolarisMapView mMapView;
	//	private int mPlacePosition;
	private Place mPlace;
	private ArrayList<Place> mBookmarks;
	private String[] mTabs = {"Résumé", "Photos"};

	public static Intent getIntent(Context context) {
		Intent intent = new Intent(context, PlaceActivity.class);
		//		intent.putExtra(EXTRA_PLACE_POSITION, position);
		return intent;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//		outState.putInt(EXTRA_PLACE_POSITION, mPlacePosition);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_place_main);

		mBookmarks = OdilzApplication.mBookmarks;
		mPlace = OdilzApplication.mPlace;
		mActionbar = getSupportActionBar();
		mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mActionbar.setDisplayShowHomeEnabled(true);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setTitle(mPlace.name);
		mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (int i = 0; i < mTabs.length; i++) {
			addTab(i);
		} 

		mMapView = (PolarisMapView) findViewById(R.id.polaris_map_view);
		mMapView.setUserTrackingButtonEnabled(true);

		LatLonPoint geoPoint = new LatLonPoint(mPlace.latitude, mPlace.longitude);
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		annotations.add(new Annotation(geoPoint, mPlace.name));
		mMapView.setAnnotations(annotations, R.drawable.map_pin_red);

		MapController mc = mMapView.getController();
		mc.setCenter(geoPoint);
		mc.setZoom(16);

		String address = buildAddress();
		((TextView) findViewById(R.id.placeAddress)).setText(address);

		TextView placePhone = (TextView) findViewById(R.id.placePhone);
		if (mPlace.tel.length() > 0) {
			placePhone.setText(mPlace.tel);
		} else {
			placePhone.setVisibility(View.GONE);
		}

		TextView placeMail = (TextView) findViewById(R.id.placeMail);
		if (mPlace.mail.length() > 0) {
			placeMail.setText(mPlace.mail);
		} else {
			placeMail.setVisibility(View.GONE);
		}

		ImageButton placeDirections = (ImageButton) findViewById(R.id.placeDirections);
		placeDirections.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="
						+ mPlace.latitude + "," +  mPlace.longitude));
				startActivity(intent);				
			}
		});

		//		if (mPlace.description.length() > 0) {
		//			((TextView) mDetails.findViewById(R.id.tvPOIDescription)).setText(mPOI.description);
		//		} else {
		//			((TextView) mDetails.findViewById(R.id.tvPOIDescription)).setText("Pas de description");
		//		}

		//		mTvPOIDistance = (TextView) mDetails.findViewById(R.id.tvPOIDistance);

	}
	
	private String buildAddress() {
		return mPlace.number + " " + mPlace.typeOfRoad + " " + mPlace.road + "\n" + mPlace.postcode + " " + mPlace.city;
	}

	private void addTab(int position) {
		ActionBar.Tab tab = mActionbar.newTab().setText(mTabs[position]);
		tab.setTabListener(this);
		mActionbar.addTab(tab);
	}

	private void toggleMenuItem(MenuItem item) {
		boolean checked = item.isChecked();
		if (checked) {
			mBookmarks.remove(mPlace);
		} else {
			mBookmarks.add(mPlace);
		}
		setMenuItemIcon(item, !checked);
		item.setChecked(!checked);
	}

	private void setMenuItemIcon(MenuItem item, boolean checked) {
		if (checked) {
			item.setIcon(R.drawable.ic_bookmark_on);
		} else {
			item.setIcon(R.drawable.ic_bookmark_off);
		}
		Log.d("mBookmarks", "mBookmarks:" + mBookmarks.size());
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_place, menu);

		MenuItem menuBookmark = menu.findItem(R.id.menu_bookmark);

		if (mBookmarks.contains(mPlace)) {
			menuBookmark.setChecked(true);
			setMenuItemIcon(menuBookmark, true);
		} else {
			menuBookmark.setChecked(false);
			setMenuItemIcon(menuBookmark, false);
		}

		return super.onCreateOptionsMenu(menu);
	}

	private void shareIt() {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		String shareBody = buildAddress();
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.place_share_subject);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(intent, getString(R.string.place_share_via)));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_bookmark:
			toggleMenuItem(item);
			break;
		case R.id.menu_share:
			shareIt();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	//	@Override
	//	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
	//		
	//		switch (itemPosition) {
	//		case 0:
	////			setView(ViewType.PRESENTATION);
	//			break;
	//		default:
	//			Toast.makeText(this, R.string.soon, Toast.LENGTH_SHORT).show();
	//			break;
	//		}
	//		
	//		return true;
	//	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}
}
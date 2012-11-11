package fr.odilz.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.gson.Gson;

import fr.odilz.R;
import fr.odilz.application.OdilzApplication;
import fr.odilz.model.Hobbies;
import fr.odilz.model.SuggestType;
import fr.odilz.model.Weather;
import fr.odilz.util.AsyncTask;
import fr.odilz.util.ImageManager;

public class SearchActivity extends SherlockActivity implements OnItemClickListener, OnSeekBarChangeListener, LocationListener {

	private Context mContext;
	private GridView mGridView;
	private HobbiesAdapter mAdapter;
	private HashSet<Hobbies> mCheckedHobbies = new HashSet<Hobbies>();
	private MenuItem mMenuItemSearch;
	private SeekBar mSearchRange;
	private TextView mSearchRangeText;

	public Location mLocation;
	public LocationManager mLocationManager;
	private TextView mWeatherTemperature;
	private TextView mWeatherSituation;
	private ImageView mWeatherImage;
	private Weather mWeather;
	private ImageManager mImageManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_search);

		mContext = this;
		
		OdilzApplication.restoreBookmarks();
		mImageManager = OdilzApplication.getImageManager(mContext);

		ActionBar mActionBar = getSupportActionBar();
		mActionBar.setTitle(getString(R.string.app_name));
		
		mGridView = (GridView) findViewById(R.id.search_hobbies);
		mAdapter = new HobbiesAdapter(this);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);

		mSearchRange = (SeekBar) findViewById(R.id.searchSeekkBarRange);
		mSearchRange.setOnSeekBarChangeListener(this);
		mSearchRangeText = (TextView) findViewById(R.id.searchRangeText);
		getRange();
		
		mWeatherTemperature = (TextView) findViewById(R.id.weatherTemperature);
		mWeatherSituation = (TextView) findViewById(R.id.weatherSituation);
		mWeatherImage = (ImageView) findViewById(R.id.weatherImage);
		
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		setSupportProgressBarIndeterminateVisibility(true);
		startTracking();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startTracking();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopTracking();
	}

	public void startTracking() {
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	}

	public void stopTracking() {
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		this.mLocation = location;
		stopTracking();
		new AsyncWeather().execute();
		Log.d("mLocation", "mLocation:" + mLocation.getLatitude() + " - " + mLocation.getLongitude());
	}

	private class AsyncWeather extends AsyncTask<Void, Void, Weather> implements ImageManager.OnImageDownloaderListener{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Weather doInBackground(Void... params) {

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				HttpGet httpGet = new HttpGet(getString(R.string.server_data) + "/weather?latitude=" + mLocation.getLatitude() + "&longitude=" + mLocation.getLongitude());
				HttpResponse response = httpClient.execute(httpGet, localContext);

				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				return new Gson().fromJson(reader, Weather.class);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Weather weather) {
			super.onPostExecute(weather);
			setSupportProgressBarIndeterminateVisibility(false);
			mMenuItemSearch.setEnabled(true);
			if (weather != null) {
				mWeather = weather;
				mWeatherTemperature.setText(weather.temperature + "°C");
				mWeatherSituation.setText(weather.name);
				mImageManager.download(this, mWeatherImage, getString(R.string.server_weather_images) + weather.image, true);
			}
		}

		@Override
		public void onImageDownloadSuccess(Bitmap bitmap, ImageView imageView, String url) {
			if (imageView == mWeatherImage) {
				mWeatherImage.setImageBitmap(bitmap);
			}
		}

		@Override
		public void onImageDownloadFail() {
			// TODO Auto-generated method stub
			
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		OdilzApplication.saveBookmarks();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
//		OdilzApplication.restoreBookmarks();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int position, long arg3) {
		checkWeather(position);
		HobbyIconView iconView = (HobbyIconView) v;
		if (iconView.isChecked()) {
			iconView.setChecked(false);
			mCheckedHobbies.remove(Hobbies.values()[position]);
		} else {
			iconView.setChecked(true);
			mCheckedHobbies.add(Hobbies.values()[position]);
		}
	}
	
	private void checkWeather(int position) {
		if (position == 2) {
			if (mWeather.name.contains("luie") || mWeather.name.contains("verse")) {
				Toast.makeText(mContext, R.string.search_bad_weather, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class AsyncSearch extends AsyncTask<Void, Void, SuggestType[]> {
		@Override
		protected SuggestType[] doInBackground(Void... params) {
			return search();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Toast.makeText(mContext, R.string.search_in_progress, Toast.LENGTH_LONG).show();
			setSupportProgressBarIndeterminateVisibility(true);
			mMenuItemSearch.setEnabled(false);
		}

		@Override
		protected void onPostExecute(SuggestType[] result) {
			super.onPostExecute(result);

			OdilzApplication.mPlaces = result;

			if (result != null) {
				Toast.makeText(mContext, "Result:" + result.length, Toast.LENGTH_SHORT).show();
//				Toast.makeText(mContext, "Result:" + result[0].places.length, Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(mContext, SuggestListActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(mContext, "Fail", Toast.LENGTH_SHORT).show();
			}
			mMenuItemSearch.setEnabled(true);
			setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	private SuggestType[] search() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(getString(R.string.server_data) + "/suggest");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(mCheckedHobbies.size());
			nameValuePairs.add(new BasicNameValuePair("longitude", "" + mLocation.getLongitude()));
			nameValuePairs.add(new BasicNameValuePair("latitude", "" + mLocation.getLatitude()));
			
			String types = "";
			for (Hobbies hobby : mCheckedHobbies) {
				nameValuePairs.add(new BasicNameValuePair("types[]", "" + hobby.id));

//				types += hobby.id + 
			}
//			TextUtils.join(",", tokens)
			
//			nameValuePairs.add(new BasicNameValuePair("types[" + hobby.id + "]", "true"));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(httpPost);
			InputStream inputStream = response.getEntity().getContent();
			Reader reader = new InputStreamReader(inputStream);

			return new Gson().fromJson(reader, SuggestType[].class);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return null;
	}

	private class HobbiesAdapter extends BaseAdapter {

		private Context mContext;

		public HobbiesAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return Hobbies.values().length;
		}

		@Override
		public Object getItem(int position) {
			return Hobbies.values()[position];
		}

		@Override
		public long getItemId(int position) {
			return Hobbies.values()[position].id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = new HobbyIconView(mContext);
			}
			Hobbies hobby = (Hobbies) getItem(position);
			((HobbyIconView) convertView).setData(hobby);

			return convertView;
		}
	}

	private class HobbyIconView extends LinearLayout implements Checkable {

		private final CheckBox mHobbyIcon;
		private final TextView mHobbyName;

		public HobbyIconView(Context context) {
			super(context);

			LayoutInflater.from(context).inflate(R.layout.list_item_hobby, this);

			mHobbyIcon = (CheckBox) findViewById(R.id.hobbyIcon);
			mHobbyName = (TextView) findViewById(R.id.hobbyName);
		}

		public void setData(final Hobbies hobby) {
			mHobbyIcon.setBackgroundResource(hobby.iconId);
			mHobbyName.setText(hobby.name);
		}

		@Override
		public boolean isChecked() {
			return mHobbyIcon.isChecked();
		}

		@Override
		public void setChecked(boolean checked) {
			mHobbyIcon.setChecked(checked);
		}

		@Override
		public void toggle() {
			if (isChecked()) {
				setChecked(false);
			} else {
				setChecked(true);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_search, menu);
		mMenuItemSearch = menu.findItem(R.id.search);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			if (mCheckedHobbies.size() == 0) {
				Toast.makeText(mContext, R.string.search_nothing_selected, Toast.LENGTH_SHORT).show();
			} else {
				new AsyncSearch().execute();
			}
			break;
		case R.id.about:
			Intent intent = AboutActivity.getIntent(this);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar == mSearchRange) {
			getRange();
		}
	}

	private void getRange() {
		String searchRange = (mSearchRange.getProgress() + 1) + "km";
		mSearchRangeText.setText(searchRange);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}
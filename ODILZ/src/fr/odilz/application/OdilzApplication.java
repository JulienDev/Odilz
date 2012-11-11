package fr.odilz.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.odilz.model.Place;
import fr.odilz.model.SuggestType;
import fr.odilz.util.ImageManager;

public class OdilzApplication extends Application{

	private static final String BOOKMARKS_FILENAME = "/bookmarks.json";
	
	private static ImageManager mImageManager;
	private static Context mContext;
	private static File mCacheDir;
	
	public static SuggestType[] mPlaces;
	public static ArrayList<Place> mBookmarks = new ArrayList<Place>();
	public static Place mPlace;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mContext = getApplicationContext();
		mCacheDir = mContext.getCacheDir();
		mImageManager = new ImageManager(getApplicationContext());
	}
	
	public static void saveBookmarks() {
		if (!mCacheDir.exists()) {
			mCacheDir.mkdir();
		}
		
		Gson gson = new Gson();
	 
		// convert java object to JSON format,
		// and returned as JSON formatted string
		String json = gson.toJson(mBookmarks);
	 
		try {
			//write converted json data to a file named "file.json"
			FileWriter writer = new FileWriter(mCacheDir + BOOKMARKS_FILENAME);
			writer.write(json);
			writer.close();
	 
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void restoreBookmarks() {
		Gson gson = new Gson();
		 
		try {
			BufferedReader br = new BufferedReader(new FileReader(mCacheDir + BOOKMARKS_FILENAME));
	 
			//convert the json string back to object
			Type fooType = new TypeToken<List<Place>>() {}.getType();
			mBookmarks = (ArrayList<Place>) gson.fromJson(br, fooType);
	 	 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ImageManager getImageManager(Context context) {
		return mImageManager;
	}
}
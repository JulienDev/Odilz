package fr.odilz.util;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import fr.odilz.R;
import fr.odilz.application.OdilzApplication;
import fr.odilz.model.Place;

public class BookmarkUtils {

	public static void shareBookmarks(Context context) {
		ArrayList<Place> mPlaces = OdilzApplication.mBookmarks;
		
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/html");
		
		String shareBody = "";
		for (Place place : mPlaces) {
			shareBody += place.name + "\n";
			shareBody += buildAddress(place) + "\n\n";
		}
		
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.place_share_subject);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.place_share_via)));
	}
	
	private static String buildAddress(Place place) {
		return place.number + " " + place.typeOfRoad + " " + place.road + "\n" + place.postcode + " " + place.city;
	}
}
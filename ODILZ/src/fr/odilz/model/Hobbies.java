package fr.odilz.model;

import fr.odilz.R;

public enum Hobbies {
	
	RESTAURANTS	(0, "Restaurants", 	R.drawable.search_icon_default, R.drawable.hobby_restaurant,R.drawable.map_pin_blue),
	MUSEUM		(1, "Musées", 		R.drawable.search_icon_default, R.drawable.hobby_museum, 	R.drawable.map_pin_red),
	PARKS		(2, "Parcs", 		R.drawable.search_icon_default, R.drawable.hobby_park,		R.drawable.map_pin_green),
	CINEMA		(3, "Cinéma", 		R.drawable.search_icon_default, 0,							R.drawable.map_pin_blue),
	CONCERT		(4, "Concert", 		R.drawable.search_icon_default, 0,							R.drawable.map_pin_blue),
	FREE		(5, "Gratuit", 		R.drawable.search_icon_default, 0,							R.drawable.map_pin_blue),
	EVENTS		(6, "Evenements", 	R.drawable.search_icon_default, 0,							R.drawable.map_pin_blue),
	MONUMENTS	(7, "Monuments", 	R.drawable.search_icon_default, R.drawable.hobby_monuments,	R.drawable.map_pin_black),
	HOTELS		(8, "Hotels", 		R.drawable.search_icon_default, R.drawable.hobby_hotel,		R.drawable.map_pin_orange);

	public int id;
	public String name;
	public int iconId;
	public int placeholderId;
	public int pinId;
	
	private Hobbies(int id, String name, int iconId, int placeholderId, int pinId) {
		this.id = id;
		this.name = name;
		this.iconId = iconId;
		this.placeholderId = placeholderId;
		this.pinId = pinId;
	}
	
	public static Hobbies getTypeById(int id) {
		for (int i=0; i<Hobbies.values().length; i++) {
			if (Hobbies.values()[i].id == id) {
				return Hobbies.values()[i];
			}
		}
		return null;
	}
}
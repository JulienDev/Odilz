package fr.odilz.model;

import com.google.gson.annotations.SerializedName;

public class Weather {

	@SerializedName("tempe_apmidi")
	public String temperature = "";
	@SerializedName("namepictos_apmidi")
	public String name = "";
	@SerializedName("pictos_apmidi")
	public String image = "";
}
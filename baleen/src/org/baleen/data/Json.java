/*
 * Copyright (C) 2010- Peer internet solutions
 *
 * This file is part of mixare.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.baleen.data;

import java.util.Random;

import org.baleen.MixView;
import org.baleen.image.ImageUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

public class Json extends DataHandler {

	String imageURL = null;

	public void processBuzzJSONObject(JSONObject jo) throws NumberFormatException, JSONException {
		if (jo.has("title") && jo.has("geocode") && jo.has("links")) {
			Log.d(MixView.TAG, "processing Google Buzz JSON data");
			createMarker( jo.getString("title"),
					Double.valueOf(jo.getString("geocode").split(" ")[0]),
					Double.valueOf(jo.getString("geocode").split(" ")[1]),0,
					jo.getJSONObject("links").getJSONArray("alternate").getJSONObject(0).getString("href"));
		}
	}

	public void processTwitterJSONObject(JSONObject jo) throws NumberFormatException, JSONException {
		Log.d(MixView.TAG, "processing Twitter JSON data");
		Log.d(MixView.TAG, "processing Twitter JSON data - text: "+jo.getString("text"));
		Random randlat = new Random(System.currentTimeMillis());
		Random randlon = new Random(System.currentTimeMillis());
		Random randop = new Random(System.currentTimeMillis());
		Double rlat = (double) randlat.nextInt(100);
		Double rlon = (double) randlon.nextInt(100);
		int op = randop.nextInt(100);
		Double lat;
		Double lon;

		if (op < 50) {
			lat = -37.7 + (rlat/1000);
		} else {
			lat = -37.7 - (rlat/1000);
		}
		
		if (op < 50) {
			lon = 144.9 + (rlon/1000);
		} else {
			lon = 144.9 + (rlon/1000);
		}

		Log.d(MixView.TAG, "processing Twitter JSON data - text: "+lat);
		Log.d(MixView.TAG, "processing Twitter JSON data - text: "+lon);
		
		if(jo.has("profile_image_url")){
			 imageURL= jo.getString("profile_image_url");
			createMarker( jo.getString("text"),
//					Double.parseDouble(coordinates.getString(0)),
//					Double.parseDouble(coordinates.getString(1)),
					lat,
					lon,
					0,
					null,
					imageURL,
					jo.getString("from_user"));
		}
		else{
			createMarker( jo.getString("text"),
//					Double.parseDouble(coordinates.getString(0)),
//					Double.parseDouble(coordinates.getString(1)),
					lat,
					lon,
					0,
					null);
		}

/*		if (jo.has("geo")&& !jo.isNull("geo")) {
			Log.d(MixView.TAG, "processing Twitter JSON data");
			JSONObject geo = jo.getJSONObject("geo");
			JSONArray coordinates = geo.getJSONArray("coordinates");

			createMarker( jo.getString("text"),
					Double.parseDouble(coordinates.getString(0)),
					Double.parseDouble(coordinates.getString(1)),
//					Double.parseDouble("-37.7592"),
//					Double.parseDouble("144.9486"),
					0,null);
		}*/
	}

	public void processMixareJSONObject(JSONObject jo) throws JSONException {

		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("elevation") && jo.has("has_detail_page")) {

			Log.d(MixView.TAG, "processing Mixare JSON data");
			String link=null;

			if(jo.getInt("has_detail_page")!=0 && jo.has("webpage"))
				link=jo.getString("webpage");

			createMarker( jo.getString("title"),
					jo.getDouble("lat"),
					jo.getDouble("lng"),
					jo.getDouble("elevation"),
					link);

		}
	}

	public void processWikipediaJSONObject(JSONObject jo) throws JSONException {

		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("elevation") && jo.has("wikipediaUrl")) {

			Log.d(MixView.TAG, "processing Wikipedia JSON data");
			createMarker( jo.getString("title"),
					jo.getDouble("lat"),
					jo.getDouble("lng"),
					jo.getDouble("elevation"),
					"http://"+jo.getString("wikipediaUrl"));
		}
	}

	public void load(JSONObject root) {
		JSONObject jo = null;
		JSONArray dataArray = null;

		try {
			// Twitter & own schema
			if(root.has("results")) {
				dataArray = root.getJSONArray("results");
				if (dataArray != null) {

					Log.i(MixView.TAG, "Twitter JSON Data Array");
					int top = Math.min(50, dataArray.length());
					Log.i(MixView.TAG, "Twitter JSON Data Array top: "+top);

					for (int i = 0; i < top; i++) {
						jo = dataArray.getJSONObject(i);

						processTwitterJSONObject(jo);
					}
					

				}
			}
			// Wikipedia
			else if (root.has("geonames")) {
				dataArray = root.getJSONArray("geonames");
				if (dataArray != null) {

					Log.i(MixView.TAG, "Wikipedia JSON Data Array");
					int top = Math.min(50, dataArray.length());

					for (int i = 0; i < top; i++) {
						jo = dataArray.getJSONObject(i);

						processWikipediaJSONObject(jo);
					}
				}
			}
			// Google Buzz
			else if (root.has("data") && root.getJSONObject("data").has("items")) {
				dataArray = root.getJSONObject("data").getJSONArray("items");
				if (dataArray != null) {

					Log.i(MixView.TAG, "Buzz JSON Data Array");
					int top = Math.min(50, dataArray.length());

					for (int i = 0; i < top; i++) {
						jo = dataArray.getJSONObject(i);

						processBuzzJSONObject(jo);
					}
				}
			}
/*			if (dataArray != null) {

				Log.i(MixView.TAG, "processing JSON Data Array");
				int top = Math.min(50, dataArray.length());

				for (int i = 0; i < top; i++) {
					jo = dataArray.getJSONObject(i);

					processMixareJSONObject(jo);
					processWikipediaJSONObject(jo);
					processTwitterJSONObject(jo);
					processBuzzJSONObject(jo);
				}
			}*/
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

}

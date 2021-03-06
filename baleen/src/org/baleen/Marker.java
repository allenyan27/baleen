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
package org.baleen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Comparator;

import org.baleen.gui.PaintScreen;
import org.baleen.gui.ScreenLine;
import org.baleen.gui.ScreenObj;
import org.baleen.gui.TextObj;
import org.baleen.image.ImageUtilities;
import org.baleen.reality.PhysicalPlace;
import org.baleen.render.Camera;
import org.baleen.render.MixVector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;



public class Marker {

	private String title;
	private String URL;
	private PhysicalPlace mGeoLoc;

	// Draw properties
	private boolean isVisible;
//	private boolean isLookingAt;
//	private boolean isNear;
//	private float deltaCenter;
	MixVector cMarker = new MixVector();
	private MixVector signMarker = new MixVector();
//	private MixVector oMarker = new MixVector();
	
	private static final int COLOR_DEFAULT = Color.rgb(255, 0, 0);

	// Temp properties
	private MixVector tmpa = new MixVector();
	private MixVector tmpb = new MixVector();
	private MixVector tmpc = new MixVector();
	
	private MixVector locationVector = new MixVector();
	private MixVector origin = new MixVector(0, 0, 0);
	private MixVector upV = new MixVector(0, 1, 0);
	private ScreenLine pPt = new ScreenLine();

	private Label txtLab = new Label();
	private TextObj textBlock;
	
	
	private Bitmap bitmap= null;
	private String userName = null;
	private float distance =-1;
	public Marker(String title, double latitude, double longitude, double altitude, String URL, Bitmap bitmap) {
		super();

		this.title = title;
		this.mGeoLoc = new PhysicalPlace();
		this.mGeoLoc.setLatitude(latitude);
		this.mGeoLoc.setLongitude(longitude);
		this.mGeoLoc.setAltitude(altitude);
		this.URL = URL;
		this.bitmap = bitmap;
	}
	
	public Marker(String title, double latitude, double longitude, double altitude, String URL, Bitmap bitmap, String userName) {
		super();

		this.title = title;
		this.mGeoLoc = new PhysicalPlace();
		this.mGeoLoc.setLatitude(latitude);
		this.mGeoLoc.setLongitude(longitude);
		this.mGeoLoc.setAltitude(altitude);
		this.URL = URL;
		this.bitmap = bitmap;
		this.userName = userName;
	}
	
	
	
	public Marker(String title, double latitude, double longitude, double altitude, String URL) {
		super();

		this.title = title;
		this.mGeoLoc = new PhysicalPlace();
		this.mGeoLoc.setLatitude(latitude);
		this.mGeoLoc.setLongitude(longitude);
		this.mGeoLoc.setAltitude(altitude);
		this.URL = URL;
	}
	

	public void setDistance(float distance){
		this.distance = distance;
	}
	
	public String getTitle(){
		return title;
	}
	
	public Bitmap getBitMap(){
		return bitmap;
	}
	
	public String getUserName(){
		return userName;
	}

	public String getURL(){
		return URL;
	}

	public double getLatitude() {
		return mGeoLoc.getLatitude();
	}
	
	public double getLongitude() {
		return mGeoLoc.getLongitude();
	}
	
	public double getAltitude() {
		return mGeoLoc.getAltitude();
	}
	
	public MixVector getLocationVector() {
		return locationVector;
	}
	
	private void cCMarker(MixVector originalPoint, Camera viewCam, float addX, float addY) {
		tmpa.set(originalPoint); //1
		tmpc.set(upV); 
		tmpa.add(locationVector); //3 
		tmpc.add(locationVector); //3
		tmpa.sub(viewCam.lco); //4
		tmpc.sub(viewCam.lco); //4
		tmpa.prod(viewCam.transform); //5
		tmpc.prod(viewCam.transform); //5

		viewCam.projectPoint(tmpa, tmpb, addX, addY); //6
		cMarker.set(tmpb); //7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); //6
		signMarker.set(tmpb); //7
	}

	private void calcV(Camera viewCam) {
		isVisible = false;
//		isLookingAt = false;
//		deltaCenter = Float.MAX_VALUE;

		if (cMarker.z < -1f) {
			isVisible = true;

			if (MixUtils.pointInside(cMarker.x, cMarker.y, 0, 0,
					viewCam.width, viewCam.height)) {

//				float xDist = cMarker.x - viewCam.width / 2;
//				float yDist = cMarker.y - viewCam.height / 2;
//				float dist = xDist * xDist + yDist * yDist;

//				deltaCenter = (float) Math.sqrt(dist);
//
//				if (dist < 50 * 50) {
//					isLookingAt = true;
//				}
			}
		}
	}

	public void update(Location curGPSFix, long time) {
		PhysicalPlace.convLocToVec(curGPSFix, mGeoLoc, locationVector);
	}

	public void calcPaint(Camera viewCam, float addX, float addY) {
		cCMarker(origin, viewCam, addX, addY);
		calcV(viewCam);
	}

//	private void calcPaint(Camera viewCam) {
//		cCMarker(origin, viewCam, 0, 0);
//	}

	private boolean isClickValid(float x, float y) {
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);

		pPt.x = x - signMarker.x;
		pPt.y = y - signMarker.y;
		pPt.rotate(Math.toRadians(-(currentAngle + 90)));
		pPt.x += txtLab.getX();
		pPt.y += txtLab.getY();

		float objX = txtLab.getX() - txtLab.getWidth() / 2;
		float objY = txtLab.getY() - txtLab.getHeight() / 2;
		float objW = txtLab.getWidth();
		float objH = txtLab.getHeight();

		if (pPt.x > objX && pPt.x < objX + objW && pPt.y > objY
				&& pPt.y < objY + objH) {
			return true;
		} else {
			return false;
		}
	}

	public void draw(PaintScreen dw, float radius, float dist, String dataSource) {

		//TODO: grandezza cerchi e trasparenza
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		if (textBlock == null) {
			if(userName != null){
				textBlock = new TextObj(userName, title, Math.round(maxHeight / 2f) + 1,
						160, dw, this.distance);			
			}
			else{
			textBlock = new TextObj(title, Math.round(maxHeight / 2f) + 1,
					160, dw, this.distance);
			}
			
		}

		if (isVisible) {
			//default color
			dw.setColor(COLOR_DEFAULT);
			//String dataSource = MixListView.getDataSource();
			if ("Wikipedia".equals(dataSource))
				dw.setColor(Color.rgb(255, 0, 0));
			else if ("Buzz".equals(dataSource))
				dw.setColor(Color.rgb(4, 228, 20));
			else if ("Twitter".equals(dataSource))
				dw.setColor(Color.rgb(50, 204, 255));
			else if ("OpenStreetMap".equals(dataSource))
				dw.setColor(Color.rgb(255, 168, 0));
			else if ("Streaming".equals(dataSource))
				dw.setColor(Color.rgb(255, 215, 0));
			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);
			
			//luo resize the bitmap according to the distance
			Bitmap resizedBitmap = null;
			int scaledRate = 1;
			if(bitmap!=null){
				scaledRate = (int) (48*(10f/dist+radius/20f));
				if(dist<=3)scaledRate=200;
				//scaledRate = (int) (24*(10f/dist+radius/10f));
				resizedBitmap = Bitmap.createScaledBitmap(bitmap, scaledRate, scaledRate, true);
			}
			
			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);
			txtLab.prepare(textBlock);
			
			/*if (("Twitter".equals(dataSource))&&(resizedBitmap!=null)){
	            dw.paintBitmap(cMarker.x-40, cMarker.y+35*(float)(0.4+radius/80), resizedBitmap);
	            dw.setStrokeWidth(1f);
	    		dw.setFill(true);
	            dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()
						/ 2, signMarker.y + maxHeight, currentAngle + 90, (float)0.4+radius/80);
			}*/
			if (("Twitter".equals(dataSource))&&(bitmap!=null)){
	            dw.paintBitmap(cMarker.x-40, cMarker.y-40, bitmap);
	            dw.setStrokeWidth(1f);
	    		dw.setFill(true);
	            dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()
						/ 2, signMarker.y + maxHeight, currentAngle + 90, 1);
			}
			else if(("Streaming".equals(dataSource))&&(resizedBitmap!=null)){
		        dw.paintBitmap(cMarker.x-40, cMarker.y+35*(float)(0.4+radius/80), resizedBitmap);
		        dw.setStrokeWidth(1f);
				dw.setFill(true);
		        dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()
						/ 2, signMarker.y + maxHeight, currentAngle + 90, (float)0.4+radius/80);

				}
				else{
					dw.paintCircle(cMarker.x, cMarker.y, maxHeight / 1.5f);
					dw.setStrokeWidth(1f);
					dw.setFill(true);
					dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()
							/ 2, signMarker.y + maxHeight, currentAngle + 90, 1);
				}
			
		}
	}

	public boolean fClick(float x, float y, MixContext ctx, MixState state) {
		boolean evtHandled = false;

		if (isClickValid(x, y)) {
			evtHandled = state.handleEvent(ctx, URL);
		}
		return evtHandled;
	}
}


class Label implements ScreenObj {
	private float x, y;
	private float width, height;
	private ScreenObj obj;

	public void prepare(ScreenObj drawObj) {
		obj = drawObj;
		float w = obj.getWidth();
		float h = obj.getHeight();

		x = w / 2;
		y = 0;

		width = w * 2;
		height = h * 2;
	}

	public void paint(PaintScreen dw) {
		dw.paintObj(obj, x, y, 0, 1);
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
}


/**
 * Compares the markers. The closer they are the higher in the stack.
 * @author daniele
 */
class MarkersOrder implements Comparator<Object> {

	private static MarkersOrder instance;	// singleton
	
	public static MarkersOrder getInstance() {
		if (instance == null)
			instance = new MarkersOrder();
		return instance;
	}
	
	/** Private, use getInstance() */
	private MarkersOrder() {
	}

	public int compare(Object left, Object right) {
		Marker leftPm = (Marker) left;
		Marker rightPm = (Marker) right;

		return Float.compare(leftPm.cMarker.z, rightPm.cMarker.z);
	}
}

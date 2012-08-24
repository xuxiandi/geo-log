package com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions;

import android.graphics.BitmapFactory;

public class TBitmapDecodingOptions {

	public static BitmapFactory.Options GetBitmapFactoryOptions() {
		BitmapFactory.Options BFO = new BitmapFactory.Options();
		BFO.inDither=false;                     //Disable Dithering mode
		BFO.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
		BFO.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
		BFO.inTempStorage=new byte[32*1024]; 		
		return BFO;
	}
}

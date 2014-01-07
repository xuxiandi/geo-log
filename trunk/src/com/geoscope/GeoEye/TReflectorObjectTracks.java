package com.geoscope.GeoEye;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TObjectTrack;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoLog.Utils.OleDate;

public class TReflectorObjectTracks {
	
	private TReflector Reflector;
	public ArrayList<TObjectTrack> Tracks = new ArrayList<TObjectTrack>();
	private Paint DrawPaint = new Paint();
	private float NodeRadius;
	
	public TReflectorObjectTracks(TReflector pReflector) {
		Reflector = pReflector;
		DrawPaint.setDither(true);
		DrawPaint.setStyle(Paint.Style.STROKE);
		DrawPaint.setStrokeJoin(Paint.Join.ROUND);
		DrawPaint.setStrokeCap(Paint.Cap.ROUND);
		DrawPaint.setStrokeWidth(1.5F*Reflector.metrics.density);
		NodeRadius = 1.5F*Reflector.metrics.density;
	}
	
	public byte[] GetTrackData(int idGeoMonitorObject, double Day, int Color) throws IOException, Exception {
		return GetCoGeoMonitorObjectTrackData(idGeoMonitorObject, Reflector.Configuration.GeoSpaceID, Day,Day, 0/*simple track data type*/);
	}
	
	public void AddNewTrack(int idGeoMonitorObject, double Day, int Color) throws IOException, Exception {
		byte[] TrackData = GetTrackData(idGeoMonitorObject, Day, Color);
		TObjectTrack ObjectTrack = new TObjectTrack(idGeoMonitorObject, Day, Color, TrackData);
		Track_RecalculateScreenNodes(ObjectTrack);
		Tracks.add(ObjectTrack);
	}
	
	public void AddNewTrack(byte[] TrackData, int idGeoMonitorObject, double Day, int Color) throws IOException, Exception {
		TObjectTrack ObjectTrack = new TObjectTrack(idGeoMonitorObject, Day, Color, TrackData);
		Track_RecalculateScreenNodes(ObjectTrack);
		Tracks.add(ObjectTrack);
	}
	
	public void RemoveTrack(int TrackIndex) {
		Tracks.remove(TrackIndex);
	}
	
	public void DrawOnCanvas(Canvas canvas) {
		for (int I = 0; I < Tracks.size(); I++) {
			TObjectTrack OT = Tracks.get(I);
			if (OT.flEnabled && (OT.ScreenNodes != null) && (OT.ScreenNodes.length > 1)) {
				DrawPaint.setColor(OT.TrackColor);
				float X0 = OT.ScreenNodes[0];
				float Y0 = OT.ScreenNodes[1];
				int Idx = 2;
				float X1,Y1;
				for (int J = 1; J < OT.NodesCount; J++) {
					X1 = OT.ScreenNodes[Idx]; Y1 = OT.ScreenNodes[Idx+1];
		    		canvas.drawLine(X0,Y0,X1,Y1, DrawPaint);
					Idx += 2;
					//.
					X0 = X1; Y0 = Y1;
				}
				Idx = 0;
				for (int J = 0; J < OT.NodesCount; J++) {
					X0 = OT.ScreenNodes[Idx]; Y0 = OT.ScreenNodes[Idx+1];
					canvas.drawCircle(X0,Y0,NodeRadius,DrawPaint);
					Idx += 2;
				}
			}
		}
	}
	
	public void Track_RecalculateScreenNodes(TObjectTrack Track) {
		if ((Track.ScreenNodes == null) && (Track.NodesCount > 0))
			Track.ScreenNodes = new float[Track.NodesCount << 1];
		if (Track.ScreenNodes != null) {
			int Idx = 0;
			int ScrIdx = 0;
			for (int I = 0; I < Track.NodesCount; I++) {
				TXYCoord C = Reflector.ReflectionWindow.ConvertToScreen(Track.Nodes[Idx+1]/*X*/,Track.Nodes[Idx+2]/*Y*/);
				Track.ScreenNodes[ScrIdx] = (float)C.X;
				Track.ScreenNodes[ScrIdx+1] = (float)C.Y;
				//.
				ScrIdx += 2;
				Idx += 3;				
			}
		}
	}
	
	public void RecalculateScreenNodes() {
		for (int I = 0; I < Tracks.size(); I++) 
			Track_RecalculateScreenNodes(Tracks.get(I));
	}
	
    private byte[] GetCoGeoMonitorObjectTrackData(int idCoComponent, int GeoSpaceID, double BegTime, double EndTime, int DataType) throws Exception,IOException {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"TypedCo"+"/"+Integer.toString(SpaceDefines.idTCoGeoMonitorObject)+"/"+Integer.toString(idCoComponent)+"/"+"CoGeoMonitorObjectTrackData.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(GeoSpaceID)+","+Double.toString(BegTime)+","+Double.toString(EndTime)+","+Integer.toString(DataType);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		//.
		HttpURLConnection Connection = Reflector.Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return null; //. ->
				byte[] Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
	            //.
	            return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
    }	
    
	public synchronized AlertDialog CreateTracksSelectorPanel(Activity ParentActivity) {
    	final CharSequence[] _items = new CharSequence[Tracks.size()];
    	final boolean[] Mask = new boolean[Tracks.size()];
    	for (int I = 0; I < Tracks.size(); I++) {
    		TObjectTrack OT = Tracks.get(I);
    		OleDate Day = new OleDate(OT.Day);
    		_items[I] = Integer.toString(OT.idGeoMonitorObject)+".  "+Integer.toString(Day.year)+"/"+Integer.toString(Day.month)+"/"+Integer.toString(Day.date);
    		Mask[I] = OT.flEnabled;
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
    	builder.setTitle(R.string.SObjectTracks);
    	builder.setNegativeButton(Reflector.getString(R.string.SClose),null);
    	builder.setMultiChoiceItems(_items, Mask, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				Tracks.get(arg1).flEnabled = arg2;
				Reflector.WorkSpace.Update();
			}
			
    	});
    	AlertDialog alert = builder.create();
    	return alert;
	}
}

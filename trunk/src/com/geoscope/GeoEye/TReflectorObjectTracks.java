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
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectTrack;

public class TReflectorObjectTracks {
	
	private TReflectorComponent Reflector;
	//.
	public ArrayList<TCoGeoMonitorObjectTrack> Tracks = new ArrayList<TCoGeoMonitorObjectTrack>();
	private Paint DrawPaint = new Paint();
	private float NodeRadius;
	
	public TReflectorObjectTracks(TReflectorComponent pReflector) {
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
	
	public void AddNewTrack(TCoGeoMonitorObjectTrack ObjectTrack) {
		Tracks.add(ObjectTrack);
	}
	
	public void AddNewTrack(byte[] TrackData, int idGeoMonitorObject, double Day, int Color) throws IOException, Exception {
		TCoGeoMonitorObjectTrack ObjectTrack = new TCoGeoMonitorObjectTrack(Color, TrackData);
		Tracks.add(ObjectTrack);
	}
	
	public void AddNewTrack(int idGeoMonitorObject, double Day, int Color) throws IOException, Exception {
		byte[] TrackData = GetTrackData(idGeoMonitorObject, Day, Color);
		TCoGeoMonitorObjectTrack ObjectTrack = new TCoGeoMonitorObjectTrack(Color, TrackData);
		Tracks.add(ObjectTrack);
	}
	
	public void RemoveTrack(int TrackIndex) {
		Tracks.remove(TrackIndex);
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
	                if (Size <= 0) throw new Exception(Reflector.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
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
    
	public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
		for (int I = 0; I < Tracks.size(); I++) {
			TCoGeoMonitorObjectTrack OT = Tracks.get(I);
			if (OT.flEnabled && (OT.Nodes != null)) {
				float[] ScreenNodes = new float[OT.NodesCount << 2];
				int Idx = 0;
				int ScrIdx = 0;
				TXYCoord C0 = RW.ConvertToScreen(OT.Nodes[Idx+1]/*X*/,OT.Nodes[Idx+2]/*Y*/);
				Idx += 3;				
				for (int J = 1; J < OT.NodesCount; J++) {
					TXYCoord C1 = RW.ConvertToScreen(OT.Nodes[Idx+1]/*X*/,OT.Nodes[Idx+2]/*Y*/);
					ScreenNodes[ScrIdx+0] = (float)C0.X;
					ScreenNodes[ScrIdx+1] = (float)C0.Y;
					ScreenNodes[ScrIdx+2] = (float)C1.X;
					ScreenNodes[ScrIdx+3] = (float)C1.Y;
					//.
					C0 = C1;
					ScrIdx += 4;
					Idx += 3;				
				}
				//.
				DrawPaint.setColor(OT.TrackColor);
				canvas.drawLines(ScreenNodes,DrawPaint);
				//.
				Idx = 0;
				canvas.drawCircle(ScreenNodes[Idx],ScreenNodes[Idx+1],NodeRadius,DrawPaint);
				Idx += 2;
				for (int J = 1; J < OT.NodesCount; J++) {
					canvas.drawCircle(ScreenNodes[Idx],ScreenNodes[Idx+1],NodeRadius,DrawPaint);
					Idx += 4;
				}
			}
		}
	}
	
	public synchronized AlertDialog CreateTracksSelectorPanel(Activity ParentActivity) {
    	final CharSequence[] _items = new CharSequence[Tracks.size()];
    	final boolean[] Mask = new boolean[Tracks.size()];
    	for (int I = 0; I < Tracks.size(); I++) {
    		TCoGeoMonitorObjectTrack OT = Tracks.get(I);
    		_items[I] = Integer.toString(I);
    		Mask[I] = OT.flEnabled;
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
    	builder.setTitle(R.string.SObjectTracks);
    	builder.setNegativeButton(Reflector.context.getString(R.string.SClose),null);
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

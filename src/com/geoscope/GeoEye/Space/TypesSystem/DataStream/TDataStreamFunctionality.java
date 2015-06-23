package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.URL.TURL;

public class TDataStreamFunctionality extends TComponentFunctionality {

	public TDataStreamFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		super(pTypeFunctionality,pidComponent);
	}

	@Override
	public int ParseFromXMLDocument(byte[] XML) throws Exception {
    	try {
        	Document XmlDoc;
    		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
    		try {
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
    			factory.setNamespaceAware(true);     
    			DocumentBuilder builder = factory.newDocumentBuilder(); 			
    			XmlDoc = builder.parse(BIS); 
    		}
    		finally {
    			BIS.close();
    		}
    		Element RootNode = XmlDoc.getDocumentElement();
			int Version = Integer.parseInt(RootNode.getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
			return Version; //. ->
    	}
    	catch (Exception E) {
			throw new Exception("error of loading XML document: "+E.getMessage()); //. =>
    	}
	}
	
	@Override
	public TPropsPanel TPropsPanel_Create(Context context) {
		Intent Result = new Intent(context, TDataStreamPropsPanel.class);
		Result.putExtra("idTComponent",idTComponent());
		Result.putExtra("idComponent",idComponent);
		//.
		return (new TPropsPanel(idTComponent(),idComponent,Result));
	}
	
	@Override
	public TURL GetDefaultURL() throws Exception {
		return (new com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.PropsPanel.TURL(idComponent));
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() throws Exception {
		return (new TThumbnailImageComposition(BitmapFactory.decodeResource(Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_component_datastream)));
	}
	
	public byte[] GetDescriptorData() throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(Server.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(TypeFunctionality.idType)+"/"+"Co"+"/"+Long.toString(idComponent)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"0"/*command*/+","+"1"/*Parameters version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		try {
			HttpURLConnection Connection = Server.OpenConnection(URL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[Connection.getContentLength()];
					int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
					if (Size != Data.length)
						throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
					//.
					return Data; //. ->
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
	}
	
	public TDataStreamDescriptor GetStreamDescriptor() throws Exception {
		return (new TDataStreamDescriptor(GetDescriptorData())); //. ->
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
    	Intent intent = new Intent(context, TDataStreamPropsPanel.class);
		intent.putExtra("idTComponent",idTComponent());
		intent.putExtra("idComponent",idComponent);
    	context.startActivity(intent);
	}
}

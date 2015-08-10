package com.geoscope.GeoEye.Space.TypesSystem.DATAFile;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.URL.TURL;

public class TDATAFileFunctionality extends TComponentFunctionality {

	public String TypeID = "";
	
	public TDATAFileFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		super(pTypeFunctionality,pidComponent);
	}

	@Override
	public int ParseFromXMLDocument(Element XMLNode) throws Exception {
    	try {
    		XMLDocumentRootNode = XMLNode;
    		//.
			int Version = Integer.parseInt(XMLDocumentRootNode.getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
			switch (Version) {
			case 1:
				try {
					Node node;
	    			TypeID = "";
	    			node = TMyXML.SearchNode(XMLDocumentRootNode,"TypeID");
	    			if (node != null) {
	    				node = node.getFirstChild();
	        			if (node != null)
	        				TypeID = node.getNodeValue();
	    			}
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing XML document: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown XML document version, version: "+Integer.toString(Version)); //. =>
			}
			return Version; //. ->
    	}
    	catch (Exception E) {
			throw new Exception("error of loading XML document: "+E.getMessage()); //. =>
    	}
	}

	@Override
	public void SetName(String Value) throws Exception {
		SetDataName(Value);
	}
	
	public void SetDataName(String DataName) throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(Server.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(TypeFunctionality.idType)+"/"+"Co"+"/"+Long.toString(idComponent)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command*/+","+DataName.replace(',', ';');
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
				try {
		            //. response
		            int response = Connection.getResponseCode();
		            if (response != HttpURLConnection.HTTP_OK) { 
						String ErrorMessage = Connection.getResponseMessage();
						byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
						ErrorMessage = new String(ErrorMessageBA,"windows-1251");
		            	throw new IOException(Server.context.getString(R.string.SServerError)+ErrorMessage); //. =>
		            }
				} catch (ConnectException CE) {
					throw new ConnectException(Server.context.getString(R.string.SNoServerConnection)); //. =>
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() throws Exception {
		TURL URL = GetAsURL();
		if (URL != null)
			return URL.GetThumbnailImageComposition(); //. ->
		return super.GetThumbnailImageComposition();
	}
	
	private TURL GetAsURL() throws Exception {
		if (TURL.IsTypeOf(TypeID)) 
			return TURL.GetURL(TypeID, Server.User, XMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
		TURL URL = GetAsURL();
		if (URL != null)
			URL.Open(context, Params);
	}
}

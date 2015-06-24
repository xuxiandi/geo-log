package com.geoscope.GeoEye.Space.TypesSystem.DATAFile;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
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

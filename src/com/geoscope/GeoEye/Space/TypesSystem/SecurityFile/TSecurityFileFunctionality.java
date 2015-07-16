package com.geoscope.GeoEye.Space.TypesSystem.SecurityFile;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.app.AlertDialog;
import android.content.Context;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;

public class TSecurityFileFunctionality extends TComponentFunctionality {

	public String _Name = "";
	public String _Info = "";
	public String _Domains = "";
	
	public TSecurityFileFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		super(pTypeFunctionality,pidComponent);
	}
	
	@Override
	public int ParseFromXMLDocument(Element XMLNode) throws Exception {
    	try {
    		XMLDocumentRootNode = XMLNode;
    		//.
    		return FromXMLNode(XMLDocumentRootNode); //. ->
    	}
    	catch (Exception E) {
			throw new Exception("error of loading xml document: "+E.getMessage()); //. =>
    	}
	}
	
	public int FromXMLNode(Node ANode) throws Exception {
		int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		
		case 1:
			try {
				Node node = TMyXML.SearchNode(ANode,"Name").getFirstChild();
				if (node != null)
					_Name = node.getNodeValue();
				//.
				node = TMyXML.SearchNode(ANode,"Info").getFirstChild();
				if (node != null)
					_Info = node.getNodeValue();
				//.
				node = TMyXML.SearchNode(ANode,"Domains");
				if (node != null) {
					node = node.getFirstChild();
					if (node != null)
						_Domains = node.getNodeValue();
				}
			}
			catch (Exception E) {
    			throw new Exception("error of parsing XML: "+E.getMessage()); //. =>
			}
			return Version; //. ->
			
		default:
			return (-Version); //. ->
		}
	}
	
	public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws IOException {
		int Version = 1;
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. Name
        Serializer.startTag("", "Name");
        Serializer.text(_Name);
        Serializer.endTag("", "Name");
        //. Info
        Serializer.startTag("", "Info");
        Serializer.text(_Info);
        Serializer.endTag("", "Info");
        //. Domains
        if (_Domains.length() > 0) {
            Serializer.startTag("", "Domains");
            Serializer.text(_Domains);
            Serializer.endTag("", "Domains");
        }
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
		String SecurityFileInfo = _Name+"\n"+"("+_Info+")";
        if (_Domains.length() > 0)
        	SecurityFileInfo += "\n"+"["+_Domains+"]";
		//.
	    new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.SSecurity)
        .setMessage(SecurityFileInfo)
	    .setPositiveButton(R.string.SOk, null)
	    .show();
	}
}

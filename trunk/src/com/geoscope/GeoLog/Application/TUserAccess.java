package com.geoscope.GeoLog.Application;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;

public class TUserAccess {

	public static final String UserAccessFileName = "UserAccess.xml";
	
	public static String UserAccessFile() {
		return TGeoLogApplication.ProfileFolder()+"/"+UserAccessFileName;
	}
	
	public static boolean UserAccessFileExists() {
		File ARF = new File(UserAccessFile());
		return (ARF.exists());
	}
	
	public String UserAccessPassword = null;
	//.
	public String AdministrativeAccessPassword = null;
	
	public TUserAccess() {
		try {
			Load();
		} catch (Exception E) {
		}
	}
	
	private void Load() throws Exception {
		File F = new File(UserAccessFile());
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(F);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
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
		int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
				Node node = TMyXML.SearchNode(RootNode,"UserAccessPassword").getFirstChild();
				if (node != null) {
					UserAccessPassword = node.getNodeValue();
					char[] CA = UserAccessPassword.toCharArray();
					StringBuilder SB = new StringBuilder();
					for (int I = 0; I < (CA.length >> 1); I++)
						SB.append(CA[(I << 1)+1]);
					UserAccessPassword = SB.toString();
				}
				node = TMyXML.SearchNode(RootNode,"AdministrativeAccessPassword").getFirstChild();
				if (node != null) {
					AdministrativeAccessPassword = node.getNodeValue();
					char[] CA = AdministrativeAccessPassword.toCharArray();
					StringBuilder SB = new StringBuilder();
					for (int I = 0; I < (CA.length >> 1); I++)
						SB.append(CA[(I << 1)+1]);
					AdministrativeAccessPassword = SB.toString();
				}
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown administrative rights file version, version: "+Integer.toString(Version)); //. =>
		}
	}
}

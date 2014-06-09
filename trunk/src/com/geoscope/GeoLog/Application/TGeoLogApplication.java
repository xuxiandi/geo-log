package com.geoscope.GeoLog.Application;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.util.Xml;

import com.geoscope.GeoEye.UserAgentService.TUserAgentService;
import com.geoscope.GeoLog.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.TrackerService.TTrackerService;

public class TGeoLogApplication {

	public static final String	ApplicationBasePath = TGeoLogInstallator.ProgramInstallationPath;
	public static final String 	ApplicationFolderName = TGeoLogInstallator.ProgramFolderName;
	public static final String 	ApplicationFolder = TGeoLogInstallator.ProgramFolder;
	
	public static final String 	LogFolder = ApplicationFolder+"/"+"Log";

	public static final String 		ProfilesFolder = ApplicationFolder+"/"+"PROFILEs";
	//.
	public static ArrayList<String> GetProfileNames() {
		ArrayList<String> Result = new ArrayList<String>();
		File PF = new File(ProfilesFolder);
		if (!PF.exists())
			return Result; //. ->
		File[] Files = PF.listFiles();
		for (int I = 0; I < Files.length; I++) 
			if (Files[I].isDirectory()) 
				Result.add(Files[I].getName());
		return Result;
		
	}
	//.
	public static final String 	DefaultProfileName = "Default";
	//.
	public static String 		ProfileName() {
		try {
			TCurrentProfileFile CurrentProfileFile = new TCurrentProfileFile(); 
			if (CurrentProfileFile.CurrentProfileName != null)
				return CurrentProfileFile.CurrentProfileName;
			else
				return DefaultProfileName; //. ->
		}
		catch (Exception E) {
			return DefaultProfileName; //. ->
		}
	}
	//.
	public static void 			SetProfileName(String Value) throws Exception {
		TCurrentProfileFile CurrentProfileFile = new TCurrentProfileFile(); 
		CurrentProfileFile.CurrentProfileName = Value;
		CurrentProfileFile.Save();
	}
	//.
	public static String 		ProfileFolder() {
		return (ProfilesFolder+"/"+ProfileName());
	}
	
	public static class TCurrentProfileFile {
		
		public static final String CurrentProfileFile = ProfilesFolder+"/"+"Current";

		public String CurrentProfileName = null;
		
		public TCurrentProfileFile() throws Exception {
			Load();
		}
		
		public void Load() throws Exception {
			File CPF = new File(CurrentProfileFile);
			if (CPF.exists()) {
				byte[] XML;
				long FileSize = CPF.length();
				if (FileSize != 0) {
					FileInputStream FIS = new FileInputStream(CPF);
					try {
						XML = new byte[(int) FileSize];
						FIS.read(XML);
					} finally {
						FIS.close();
					}
					Document XmlDoc;
					ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory
								.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						XmlDoc = builder.parse(BIS);
					} finally {
						BIS.close();
					}
					int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
					NodeList NL;
					switch (Version) {
					case 1:
						NL = XmlDoc.getDocumentElement().getElementsByTagName("CurrentProfileName");
						CurrentProfileName = NL.item(0).getFirstChild().getNodeValue();
						break; // . >
						
					default:
						throw new Exception("unknown current profile file version, version: "+Integer.toString(Version)); // . =>
					}
				}
			}
		}
		
		public void Save() throws Exception {
			int CurrentProfileFileVersion = 1;
			String CPFN = CurrentProfileFile;
			String TCPFN = CPFN+".tmp";
		    XmlSerializer serializer = Xml.newSerializer();
		    FileWriter writer = new FileWriter(TCPFN);
			try {
				serializer.setOutput(writer);
				serializer.startDocument("UTF-8", true);
				serializer.startTag("", "ROOT");
				//.
				serializer.startTag("", "Version");
				serializer.text(Integer.toString(CurrentProfileFileVersion));
				serializer.endTag("", "Version");
				//.
				serializer.startTag("", "CurrentProfileName");
				if (CurrentProfileName != null)
					serializer.text(CurrentProfileName);
				serializer.endTag("", "CurrentProfileName");
				//.
				serializer.endTag("", "ROOT");
				serializer.endDocument();
			} finally {
				writer.close();
			}
			File TCPF = new File(TCPFN);
			File CPF = new File(CPFN);
			TCPF.renameTo(CPF);
		}
	}
	
	public static void Terminate(Context context) {
		TUserAgentService UserAgentService = TUserAgentService.GetService();
		if (UserAgentService != null)
			UserAgentService.StopServicing();
		TTrackerService TrackerService = TTrackerService.GetService();
		if (TrackerService != null)
			TrackerService.StopServicing();
		//.
		Intent UserAgentServiceLauncher = new Intent(context, TUserAgentService.class);
		context.stopService(UserAgentServiceLauncher);
		Intent TrackerServiceLauncher = new Intent(context, TTrackerService.class);
		context.stopService(TrackerServiceLauncher);
		//.
		System.exit(0);
	}
}

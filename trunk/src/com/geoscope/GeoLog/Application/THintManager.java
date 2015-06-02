package com.geoscope.GeoLog.Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;

import com.geoscope.GeoEye.R;

public class THintManager {
	
	public static final int HINT__Long_click_to_show_an_item_location 		= R.string.SLongClickToShowItemLocation;
	public static final int HINT__Long_click_to_show_an_object_panel 		= R.string.SLongClickToShowAnObjectPanel;
	public static final int HINT__Long_click_to_show_a_place			 	= R.string.SLongClickToShowAPlace;
	public static final int HINT__Long_click_to_edit_item_properties		= R.string.SLongClickToEditItemProperties;
	public static final int HINT__My_user_panel_hint						= R.string.SMyUserPanelHint;
	public static final int HINT__User_panel_hint							= R.string.SUserPanelHint;
	
	public static String GetHint(int HintID, Context context) {
		try {
			if (DisabledHints_ItemExists(HintID))
				return null; //. ->
		} catch (IOException IOE) {
			return null; //. ->
		} 
		return (context.getString(R.string.SHint)+context.getString(HintID));
	}
	
	public static void SetHintAsDisabled(int HintID) {
		try {
			DisabledHints_AddItem(HintID);
		} catch (IOException IOE) {
		}
	}
	
	public static final String DisabledHints_FileName = "DisabledHints.txt";
		
	private static boolean DisabledHints_ItemExists(int ItemID) throws IOException {
		ArrayList<Integer> AH = new ArrayList<Integer>();
		//.
		String FN = TGeoLogApplication.Help_Folder+"/"+DisabledHints_FileName;
		File F = new File(FN);
		if (F.exists()) {
			FileInputStream FIS = new FileInputStream(F);
			try {
				if (F.length() > 0) {
					byte[] BA = new byte[(int)F.length()];
					FIS.read(BA);
					String S = new String(BA,"utf-8");
					String[] SA = S.split(",");
					for (int I = 0; I < SA.length; I++)
						AH.add(Integer.parseInt(SA[I]));
				}
			}
			finally {
				FIS.close();
			}
		}
		//.
		for (int I = 0; I < AH.size(); I++)
			if (AH.get(I) == ItemID)
				return true; //. ->
		return false; 
	}

	private static void DisabledHints_AddItem(int ItemID) throws IOException {
		ArrayList<Integer> AH = new ArrayList<Integer>();
		//.
		String FN = TGeoLogApplication.Help_Folder+"/"+DisabledHints_FileName;
		File F = new File(FN);
		if (F.exists()) {
			FileInputStream FIS = new FileInputStream(F);
			try {
				if (F.length() > 0) {
					byte[] BA = new byte[(int)F.length()];
					FIS.read(BA);
					String S = new String(BA,"utf-8");
					String[] SA = S.split(",");
					for (int I = 0; I < SA.length; I++)
						AH.add(Integer.parseInt(SA[I]));
				}
			}
			finally {
				FIS.close();
			}
		}
		//.
		AH.add(Integer.valueOf(ItemID));
		//.
		StringBuilder SB = new StringBuilder();
		for (int I = 0; I < AH.size(); I++)
			if (I == 0)
				SB.append(AH.get(I).toString());
			else
				SB.append(","+AH.get(I).toString());
		byte[] BA = SB.toString().getBytes("utf-8");
		//.
		FileOutputStream FOS = new FileOutputStream(F);
		try {
			FOS.write(BA);
		}
		finally {
			FOS.close();
		}
	}

	public static void DisabledHints_RemoveAll() {
		String FN = TGeoLogApplication.Help_Folder+"/"+DisabledHints_FileName;
		File F = new File(FN);
		F.delete();
	}
}

package com.geoscope.Classes.Data.Types.Image.Compositions;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class TThumbnailImageComposition {

	public static class TMap {
		
		public static class TItem {
			
			public RectF DestRect;
			public Object LinkedObject;
			
			public TItem(RectF pDestRect, Object pLinkedObject) {
				DestRect = new RectF(pDestRect);
				LinkedObject = pLinkedObject;
			}
		}
		
		public ArrayList<TItem> Items = new ArrayList<TItem>(4);
		//.
		public TItem ItemByPosition = null;
		
		public void Clear() {
			Items.clear();
		}
		
		public void AddItem(RectF pDestRect, Object pLinkedObject) {
			Items.add(new TItem(pDestRect,pLinkedObject));
		}
		
		public int Count() {
			return Items.size(); 
		}
		
		public void Scale(float Scale) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TItem Item = Items.get(I);
				//.
				Item.DestRect.left *= Scale;
				Item.DestRect.top *= Scale;
				Item.DestRect.right *= Scale;
				Item.DestRect.bottom *= Scale;
			}
		}

		private TItem GetItemByPosition(float X, float Y) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TItem Item = Items.get(I);
				if (Item.DestRect.contains(X,Y))
					return Item; //. ->
			}
			return null;
		}

		public TItem CheckItemByPosition(float X, float Y) {
			ItemByPosition = GetItemByPosition(X,Y);
			return ItemByPosition;
		}
	}
	
	
	public Bitmap BMP;
	//.
	public TMap Map = new TMap();
	
	public TThumbnailImageComposition(Bitmap pBMP) {
		SetBitmap(pBMP);
	}
	
	public TThumbnailImageComposition(Bitmap pBMP, Object pLinkedObject) {
		super();
		//.
		SetBitmap(pBMP, pLinkedObject);
	}
	
	public void SetBitmap(Bitmap pBMP) {
		BMP = pBMP;
		//.
		Map.Clear();
	}
	
	public void SetBitmap(Bitmap pBMP, Object pLinkedObject) {
		SetBitmap(pBMP);
		//.
		Map.AddItem(new RectF(0,0, BMP.getWidth(),BMP.getHeight()), pLinkedObject);
	}
	
	public Bitmap TakeBitmap() {
		Bitmap _BMP = BMP;
		BMP = null;
		return _BMP;
	}
	
	public boolean HasLinkedObjects() {
		return (Map.Count() > 0);
	}
	
	public void ScaleMap(float Scale) {
		Map.Scale(Scale);
	}
}

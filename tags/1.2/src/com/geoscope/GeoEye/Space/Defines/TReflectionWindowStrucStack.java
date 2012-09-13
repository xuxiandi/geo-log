package com.geoscope.GeoEye.Space.Defines;

public class TReflectionWindowStrucStack {
	
	private TReflectionWindowStruc[]	Items;
	private int							Count;
	private int							Position;
	
	public TReflectionWindowStrucStack(int Capacity) {
		Items = new TReflectionWindowStruc[Capacity];
		Count = 0;
		Position = 0;
	}
	
	public void Push(TReflectionWindowStruc RWS) {
		if (Count > 0) {
			int Pos = Position;
			Pos--;
			if (Pos < 0)
				Pos += Items.length;
			if (Items[Pos].IsEqualTo(RWS))
				return; //. ->
		}
		//.
		Items[Position] = RWS;
		Position++;
		if (Position >= Items.length)
			Position = 0;
		if (Count < Items.length)
			Count++;
	}
	
	public TReflectionWindowStruc Pop() {
		if (Count == 0)
			return null; //. ->
		Position--;
		if (Position < 0)
			Position += Items.length;
		Count--;
		return Items[Position];
	}
}

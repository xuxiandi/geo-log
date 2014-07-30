package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import com.geoscope.Classes.Log.TDataConverter;

public class TReal48 {
	//. conversions: thanks goes to Richard Biffl
	
	public static final int Size = 6;
	
	public short WD0;
	public short WD1;
	public short WD2;

	public TReal48(byte[] BA, int Index)
	{
		WD0 = (short)(BA[Index+0]+(BA[Index+1] << 8));
		WD1 = (short)(BA[Index+2]+(BA[Index+3] << 8));
		WD2 = (short)(BA[Index+4]+(BA[Index+5] << 8));
	}

	public void Set(byte[] BA, int Index)
	{
		WD0 = (short)(BA[Index+0]+(BA[Index+1] << 8));
		WD1 = (short)(BA[Index+2]+(BA[Index+3] << 8));
		WD2 = (short)(BA[Index+4]+(BA[Index+5] << 8));
	}

	public double getValue() {
		short x = (short)(WD0 & 0x00FF);  /* Real biased exponent in x */
		short W0;
		short W1;
		short W2;
		short W3;
		/* when exponent is 0, value is 0.0 */
		if (x == 0)
			return 0; //. ->
		else 
		{
			W3 = (short)
				(((x + 894) << 4) |  /* adjust exponent bias */
				(WD2 & 0x8000) |  /* sign bit */
				((WD2 & 0x7800) >> 11));  /* begin significand */
			W2 = (short)
				((WD2 << 5) |  /* continue shifting significand */
				(WD1 >> 11));
			W1 = (short)
				((WD1 << 5) |
				(WD0 >> 11));
			W0 = (short)((WD0 & 0xFF00) << 5); /* mask real's exponent */
		};
		byte[] BA = new byte[8];
		BA[0] = (byte)(W0 & 0xFF); BA[1] = (byte)(W0 >> 8);
		BA[2] = (byte)(W1 & 0xFF); BA[3] = (byte)(W1 >> 8);
		BA[4] = (byte)(W2 & 0xFF); BA[5] = (byte)(W2 >> 8);
		BA[6] = (byte)(W3 & 0xFF); BA[7] = (byte)(W3 >> 8);
		//.
		double R = 0.0;
		try {
			R = TDataConverter.ConvertBEByteArrayToDouble(BA,0);
		}
		catch (Exception E) {
		}
		return R;
	}
	
	public void setValue(double value) throws Exception,IOException {
		byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(value);
		short W0 = (short)(BA[0] | (BA[1] << 8));
		short W1 = (short)(BA[2] | (BA[3] << 8));
		short W2 = (short)(BA[4] | (BA[5] << 8));
		short W3 = (short)(BA[6] | (BA[7] << 8));
		/* check for 0.0 */
		if ((W0 == 0x0000) &&
			(W1 == 0x0000) &&
			(W2 == 0x0000) &&
			/* ignore sign bit */
			((W3 & 0x7FFF) == 0x0000)) 
		{
			/* exponent and significand are both 0, so value is 0.0 */
			WD2 = WD1 = WD0 = 0x0000;
			/* sign bit is ignored ( -0.0 -> 0.0 ) */
			return; //. ->
		}
		/* test for maximum exponent value */
		if ((W3 & 0x7FF0) == 0x7FF0) 
		{
			/* value is either Inf or NaN */
			if ((W0 == 0x0000) &&
				(W1 == 0x0000) &&
				(W2 == 0x0000) &&
				((W3 & 0x000F) == 0x0000)) 
			{
				/* significand is 0, so value is Inf */
				/* value becomes signed maximum real, */
				/* and error code prInf is returned */
				WD1 = WD0 = (short)0xFFFF;
				WD2 = (short)(0x7FFF | (W3 & 0x8000)); /* retain sign bit */
				return; //. ->
			} 
			else 
			{
				/* significand is not 0, so value is NaN */
				/* value becomes 0.0, and prNaN code is returned */
				/* sign bit is ignored (no negative NaN) */
				WD2 = WD1 = WD0 = 0x0000;
				/* sign bit is ignored ( -NaN -> +NaN ) */
				return; //. ->
			}
		}
		/* round significand if necessary */
		if ((W0 & 0x1000) == 0x1000) 
		{
			/* significand's 40th bit set, so round significand up */
			if ((W0 & 0xE000) != 0xE000)
				/* room to increment 3 most significant bits */
				W0 += 0x2000;
			else 
			{
				/* carry bit to next element */
				W0 = 0x0000;
				/* carry from 0th to 1st element */
				if (W1 != 0xFFFF)
					W1++;
				else 
				{
					W1 = 0x0000;
					/* carry from 1st to 2nd element */
					if (W2 != 0xFFFF)
						W2++;
					else 
					{
						W2 = 0x0000;
						/* carry from 2nd to 3rd element */
						/* significand may overflow into exponent */
						/* exponent not full, so won't overflow */
						W3++;
					}
				}
			}
		}
		/* get exponent for underflow/overflow tests */
		short x = (short)((W3 & 0x7FF0) >> 4);
		/* test for underflow */
		if (x < 895) 
		{
			/* value is below real range */
			WD2 = WD1 = WD0 = 0x0000;
			if ((W3 & 0x8000) == 0x8000)
				/* sign bit was set, so value was negative */
				throw new Exception("negative overflow"); //. =>
			else
				/* sign bit was not set */
				throw new Exception("positive overflow"); //. =>
		}
		/* test for overflow */
		if (x > 1149) 
		{
			/* value is above real range */
			WD1 = WD0 = (short)0xFFFF;
			WD2 = (short)(0x7FFF | (W3 & 0x8000)); /* retain sign bit */
			return ; //. -> // prOverflow;
		}
		/* value is within real range */
		WD0 = (short)
			((x - 894) |  /* re-bias exponent */
			((W0 & 0xE000) >> 5) |  /* begin significand */
			(W1 << 11));
		WD1 = (short)((W1 >> 5) | (W2 << 11));
		WD2 = (short)((W2 >> 5) | ((W3 & 0x000F) << 11) | (W3 & 0x8000));  /* copy sign bit */
		return ;
	}
}

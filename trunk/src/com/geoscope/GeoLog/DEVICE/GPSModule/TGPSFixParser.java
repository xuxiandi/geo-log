/*
 * GpsPositionParser.java
 *
 * Copyright (C) 2005-2006 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;

/**
 *
 * @author Tommi
 */
public class TGPSFixParser {
    
    private static final String DELIMETER = ",";
    public static final double SpeedTransfFactor = 1.852;
    
    /** Creates a new instance of GpsPositionParser */
    public TGPSFixParser() 
    {
    }
    
    public static boolean Parse(String record, TGPSFixValue Fix) throws NumberFormatException,StringIndexOutOfBoundsException 
    {
        if (record.startsWith("$GPRMC") == true) 
        {
            String currentValue = record;
            int nextTokenIndex = currentValue.indexOf(DELIMETER);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // time of fix
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String TimeOfFix = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Warning
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String warning = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Lattitude
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String lattitude = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Lattitude direction
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String lattitudeDirection = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Longitude
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String longitude = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Longitude direction
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String longitudeDirection = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Ground speed
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String groundSpeed = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // Bearing
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String courseMadeGood = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            // date of fix
            nextTokenIndex = currentValue.indexOf(DELIMETER);
            String DateOfFix = currentValue.substring(0, nextTokenIndex);
            currentValue = currentValue.substring(nextTokenIndex+1);
            
            //. parsing to values
            
            double FixTimeDouble = 0.0;
            if ((TimeOfFix.length() > 0) && (DateOfFix.length() > 0))
            {
                int UtcHours = 0;
                int UtcMinutes = 0;
                int UtcSeconds = 0;
                //.
                UtcHours = Integer.parseInt(TimeOfFix.substring(0, 2));
                UtcMinutes = Integer.parseInt(TimeOfFix.substring(2, 4));
                UtcSeconds = Integer.parseInt(TimeOfFix.substring(4, 6));
                /*//. still not used UtcMilliseconds = 0;
                // Extract milliseconds if it is available
                if (TimeOfFix.length() > 7)
                UtcMilliseconds = Integer.decode(dateTimeOfFix.substring(7));*/
                //.
                int UtcDay = 0;
                int UtcMonth = 0;
                int UtcYear = 0;
                //.
                UtcDay = Integer.parseInt(DateOfFix.substring(0, 2));
                UtcMonth = Integer.parseInt(DateOfFix.substring(2, 4));
                UtcYear = Integer.parseInt(DateOfFix.substring(4, 6))+2000;
                //.
                OleDate FixTime = new OleDate(UtcYear,UtcMonth,UtcDay,UtcHours,UtcMinutes,UtcSeconds);
                FixTimeDouble = FixTime.toDouble();
            }
            
            double longitudeDouble = 0.0;
            double latitudeDouble = 0.0;
            double speed = 0.0;
            double bearing = 0.0;
            if ((longitude.length() > 0) && (lattitude.length() > 0)) 
            {
                longitudeDouble = parseValue(longitude, false);
                if(longitudeDirection.equals("E") == false) 
                    longitudeDouble = -longitudeDouble;
                
                latitudeDouble = parseValue(lattitude, true);
                if(lattitudeDirection.equals("N") == false) 
                    latitudeDouble = -latitudeDouble;
                
                if (groundSpeed.length() > 0)
                    speed = Double.parseDouble(groundSpeed)*SpeedTransfFactor;

                if (courseMadeGood.length() > 0)
                    bearing = Double.parseDouble(courseMadeGood);
            }
            //.
            if (warning.equals("A") == true) 
                Fix.setValues(OleDate.UTCCurrentTimestamp(),FixTimeDouble, latitudeDouble,longitudeDouble,0.0, speed, bearing, TGPSFixValue.FixUnknownPrecision);
            else
            {
                Fix.SetFixAsUnAvailable(OleDate.UTCCurrentTimestamp(),FixTimeDouble);
            }
            return true; //. ->
        } 
        else 
        {
            // Unknown record type
            return false;
        }
    }
    
    public static double ParsePrecision(String record, double LastPrecision) 
    {
        if (record.startsWith("$GPGSA")) 
        {
            String currentValue = record;
            int nextTokenIndex;
            String S = "";
            
            //. get HDOP sentence
            for (int I = 0; I < 17; I++)
            {
                nextTokenIndex = currentValue.indexOf(DELIMETER);
                S = currentValue.substring(0, nextTokenIndex);
                currentValue = currentValue.substring(nextTokenIndex+1);
            }
            
            if (S.equals(""))
                return LastPrecision; //. ->
            
            double HDOP = Double.parseDouble(S);
            return (HDOP*6.0); //. ->
        } 
        else
            return LastPrecision;
    }
    
    /**
     * Convert latitude or longitude from NMEA format to Google's decimal degree
     * format.
     */
    private static double parseValue(String valueString, boolean isLongitude) throws NumberFormatException 
    {
        int degreeInteger = 0;
        double minutes = 0.0;
        if( isLongitude==true ) 
        {
            degreeInteger = Integer.parseInt(valueString.substring(0, 2));
            minutes = Double.parseDouble( valueString.substring(2) );
        }
        else 
        {
            degreeInteger = Integer.parseInt(valueString.substring(0, 3));
            minutes = Double.parseDouble( valueString.substring(3) );
        }
        double degreeDecimals = minutes / 60.0;
        double degrees = degreeInteger + degreeDecimals;
        return degrees;
    }
    
}

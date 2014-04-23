/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.geoscope.GeoLog.Utils;

import java.util.*;

    public class TDateTime
    {
        private Date DT = new Date();
        private Calendar C = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        public int year;
        public int month;
        public int date;
        public int hrs;
        public int min;
        public int sec;
        
        public TDateTime() {
        }
        
		public TDateTime(int _year, int _month, int _date, int _hrs, int _min, int _sec)
        {
            year = _year;
            month = _month;
            date = _date;
            hrs = _hrs;
            min = _min;
            sec = _sec;
        }
        
        public void SetDateTime(Date DateTime)
        {            
            C.setTime(DateTime);
            year = C.get(Calendar.YEAR);
            month = C.get(Calendar.MONTH)+1;
            date = C.get(Calendar.DATE);
            hrs = C.get(Calendar.HOUR_OF_DAY);
            min = C.get(Calendar.MINUTE);
            sec = C.get(Calendar.SECOND);
        }
        
        public Date GetDateTime() {
        	C.set(year,month-1,date,hrs,min,sec);
        	return C.getTime();
        }
        
        public void SetTimeStamp(long TimeStamp)
        {
            DT.setTime(TimeStamp);
            SetDateTime(DT);
        }
        
        public int getYear()
        {
            return year;
        }
        
        public void setYear(int Value)
        {
            year = Value;
        }
        
        public int getMonth()
        {
            return month;
        }
        
        public void setMonth(int Value)
        {
            month = Value;
        }
        
        public int getDate()
        {
            return date;
        }
        
        public void setDate(int Value)
        {
            date = Value;
        }
        
        public int getHours()
        {
            return hrs;
        }
        
        public void setHours(int Value)
        {
            hrs = Value;
        }
        
        public int getMinutes()
        {
            return min;
        }
        
        public void setMinutes(int Value)
        {
            min = Value;
        }
        
        public int getSeconds()
        {
            return sec;
        }
        
        public void setSeconds(int Value)
        {
            sec = Value;
        }     
    }
    
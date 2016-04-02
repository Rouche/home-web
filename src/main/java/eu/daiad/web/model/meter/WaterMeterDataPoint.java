package eu.daiad.web.model.meter;

import org.joda.time.DateTime;

public class WaterMeterDataPoint {

	public long timestamp = 0;

	public float volume = 0;

	public float difference = 0;
	
	public int getWeek() {
		return (new DateTime(timestamp)).getWeekOfWeekyear();
	}

}

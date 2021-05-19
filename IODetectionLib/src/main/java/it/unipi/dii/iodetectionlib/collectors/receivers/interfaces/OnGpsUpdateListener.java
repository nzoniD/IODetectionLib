package it.unipi.dii.iodetectionlib.collectors.receivers.interfaces;

public interface OnGpsUpdateListener
{
	void onGpsUpdate(int satellites, long lastFixMillis, int fixSatellites);
}

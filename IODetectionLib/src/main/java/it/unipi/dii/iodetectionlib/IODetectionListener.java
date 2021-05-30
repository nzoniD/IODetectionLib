package it.unipi.dii.iodetectionlib;

/* Listeners of I/O detection must implement this interface. */
public interface IODetectionListener
{
	/* It will be call periodically with the last IODetectionResult */
	void onDetectionChange(IODetectionResult result);
}

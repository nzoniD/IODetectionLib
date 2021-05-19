package it.unipi.dii.iodetectionlib;

public enum IOStatus
{
	OUTDOOR,
	INDOOR,
	UNKNOWN;

	@Override
	public String toString()
	{
		switch (this) {
			case INDOOR:
				return "Indoor";
			case OUTDOOR:
				return "Outdoor";
			case UNKNOWN:
			default:
				return "Unknown";
		}
	}
}

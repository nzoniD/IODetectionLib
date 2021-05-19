package it.unipi.dii.iodetectionlib.collectors.ml;

public class Feature
{
	private final FeatureId id;
	private final float value;

	public Feature(FeatureId id, float value)
	{
		this.id = id;
		this.value = value;
	}

	public FeatureId getFeatureId()
	{
		return id;
	}

	public float getValue()
	{
		return value;
	}

	public float getNormalizedValue()
	{
		try {
			float max = (float)FeatureMetadata.getMaxValue(id);
			float value = getValue();
			return value > max ? max : (value/max);
		} catch (IllegalArgumentException ex) {
			return getValue();
		}
	}

	public String toString()
	{
		return "id: " + id.name() + " " + value;
	}
}

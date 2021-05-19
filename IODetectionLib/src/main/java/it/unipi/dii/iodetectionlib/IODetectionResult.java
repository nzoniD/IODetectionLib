package it.unipi.dii.iodetectionlib;

public class IODetectionResult
{
	private static final float FLOAT_COMPARISON_DELTA = 0.01f;
	private static float confidenceThreshold = 0.1f;
	private final float value;
	private final float validatedValue;

	public IODetectionResult(float value, float validatedValue)
	{
		this.value = value;
		this.validatedValue = validatedValue;
	}

	public IODetectionResult(float value)
	{
		this(value, Float.NaN);
	}

	public IODetectionResult(float value, IODetectionResult oldResult)
	{
		this(value, oldResult.getValidatedRawValue());
	}

	public static void setConfidenceThreshold(float threshold)
	{
		confidenceThreshold = threshold;
	}

	public float getRawValue()
	{
		return value;
	}

	public float getValidatedRawValue()
	{
		if (Float.isNaN(validatedValue) || getValidatedConfidence() < confidenceThreshold)
			return Float.NaN;
		return validatedValue;
	}

	public IOStatus getIOStatus()
	{
		if (Float.isNaN(value) || floatEqual(value, 0.5f))
			return IOStatus.UNKNOWN;
		return value > 0.5f ? IOStatus.INDOOR : IOStatus.OUTDOOR;
	}

	public IOStatus getValidatedIOStatus()
	{
		if (Float.isNaN(validatedValue) || floatEqual(validatedValue, 0.5f)
			|| getValidatedConfidence() < confidenceThreshold)
			return IOStatus.UNKNOWN;
		return validatedValue > 0.5f ? IOStatus.INDOOR : IOStatus.OUTDOOR;
	}

	public float getConfidence()
	{
		if (Float.isNaN(value))
			return Float.NaN;
		return Math.abs(value - 0.5f);
	}

	public float getValidatedConfidence()
	{
		if (Float.isNaN(validatedValue))
			return Float.NaN;
		return Math.abs(validatedValue - 0.5f);
	}

	private boolean floatEqual(float f1, float f2)
	{
		return f2 > f1 - FLOAT_COMPARISON_DELTA && f2 < f1 + FLOAT_COMPARISON_DELTA;
	}

	@Override
	public String toString()
	{
		return "status: " + getIOStatus() + " (" + getConfidence() + ") validated: " + getValidatedIOStatus() + " (" + getValidatedConfidence() + ")";
	}
}

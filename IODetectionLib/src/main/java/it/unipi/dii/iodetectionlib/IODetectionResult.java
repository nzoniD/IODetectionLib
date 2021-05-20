package it.unipi.dii.iodetectionlib;

public class IODetectionResult
{
	private static final float FLOAT_COMPARISON_DELTA = 0.01f;
	private static float confidenceThreshold = 0.2f;
	private final float value;
	private final float validatedValue;

	public IODetectionResult(float value, float validatedValue)
	{
		this.value = value;
		if (getConfidence() >= confidenceThreshold)
			this.validatedValue = value;
		else
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
		if (Float.isNaN(value) || isDifferentFromUnknown(value))
			return IOStatus.UNKNOWN;
		return value > 0.5f ? IOStatus.INDOOR : IOStatus.OUTDOOR;
	}

	public IOStatus getValidatedIOStatus()
	{
		if (Float.isNaN(validatedValue) || isDifferentFromUnknown(validatedValue)
			|| getValidatedConfidence() < confidenceThreshold)
			return IOStatus.UNKNOWN;
		return validatedValue > 0.5f ? IOStatus.INDOOR : IOStatus.OUTDOOR;
	}

	public float getConfidence()
	{
		if (Float.isNaN(value))
			return Float.NaN;
		return Math.abs(value - 0.5f) * 2.0f;
	}

	public float getValidatedConfidence()
	{
		if (Float.isNaN(validatedValue))
			return Float.NaN;
		return Math.abs(validatedValue - 0.5f) * 2.0f;
	}

	private static boolean isDifferentFromUnknown(float f1)
	{
		return (float) 0.5 > f1 - FLOAT_COMPARISON_DELTA && (float) 0.5 < f1 + FLOAT_COMPARISON_DELTA;
	}

	@Override
	public String toString()
	{
		return "status: " + getIOStatus() + " (" + getConfidence() + ") validated: " + getValidatedIOStatus() + " (" + getValidatedConfidence() + ")";
	}
}

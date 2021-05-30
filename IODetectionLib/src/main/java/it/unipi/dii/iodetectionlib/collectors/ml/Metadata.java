package it.unipi.dii.iodetectionlib.collectors.ml;

import android.content.Context;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.support.metadata.schema.NormalizationOptions;
import org.tensorflow.lite.support.model.Model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class Metadata
{
	private final int[] featureVectorShape;
	private final DataType featureVectorDataType;
	private final Tensor.QuantizationParams featureVectorQuantizationParams;
	private final float[] featureVectorMean;
	private final float[] featureVectorStddev;
	private final int[] iodetectionShape;
	private final DataType iodetectionDataType;
	private final Tensor.QuantizationParams iodetectionQuantizationParams;

	private Metadata(ByteBuffer buffer, Model model) throws IOException
	{
		MetadataExtractor extractor = new MetadataExtractor(buffer);
		Tensor featureVectorTensor = model.getInputTensor(0);
		featureVectorShape = featureVectorTensor.shape();
		featureVectorDataType = featureVectorTensor.dataType();
		featureVectorQuantizationParams = featureVectorTensor.quantizationParams();
		NormalizationOptions featureVectorNormalizationOptions =
			(NormalizationOptions) extractor.getInputTensorMetadata(0).processUnits(0).options(new NormalizationOptions());
		FloatBuffer featureVectorMeanBuffer = featureVectorNormalizationOptions.meanAsByteBuffer().asFloatBuffer();
		featureVectorMean = new float[featureVectorMeanBuffer.limit()];
		featureVectorMeanBuffer.get(featureVectorMean);
		FloatBuffer featureVectorStddevBuffer = featureVectorNormalizationOptions.stdAsByteBuffer().asFloatBuffer();
		featureVectorStddev = new float[featureVectorStddevBuffer.limit()];
		featureVectorStddevBuffer.get(featureVectorStddev);
		Tensor iodetectionTensor = model.getOutputTensor(0);
		iodetectionShape = iodetectionTensor.shape();
		iodetectionDataType = iodetectionTensor.dataType();
		iodetectionQuantizationParams = iodetectionTensor.quantizationParams();
	}

	public static Metadata createModelMetadata(Context context) throws IOException
	{
		Model model = Model.createModel(context, "IODetectorModel.tflite", new Model.Options.Builder().build());
		return new Metadata(model.getData(), model);
	}

	public int[] getFeatureVectorShape()
	{
		return Arrays.copyOf(featureVectorShape, featureVectorShape.length);
	}

	public DataType getFeatureVectorDataType()
	{
		return featureVectorDataType;
	}

	public Tensor.QuantizationParams getFeatureVectorQuantizationParams()
	{
		return featureVectorQuantizationParams;
	}

	public float[] getFeatureVectorMean()
	{
		return Arrays.copyOf(featureVectorMean, featureVectorMean.length);
	}

	public float[] getFeatureVectorStddev()
	{
		return Arrays.copyOf(featureVectorStddev, featureVectorStddev.length);
	}

	public int[] getIodetectionShape()
	{
		return Arrays.copyOf(iodetectionShape, iodetectionShape.length);
	}

	public DataType getIodetectionDataType()
	{
		return iodetectionDataType;
	}

	public Tensor.QuantizationParams getIodetectionQuantizationParams()
	{
		return iodetectionQuantizationParams;
	}
}

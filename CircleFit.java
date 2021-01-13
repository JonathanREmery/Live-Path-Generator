import java.lang.Math;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class CircleFit {
    public static double[] fit(double[][] points) {
        double[][] coeffecientMatrixArray = new double[points.length][points[0].length+1];
        double[] outputVectorArray = new double[points.length];

        for (int i = 0; i < points.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < points[i].length+1; j++) {
                coeffecientMatrixArray[i][j] = (j == points[i].length ? 1.0 : 2*points[i][j]);
                if (j != points[i].length) {
                    sum += -Math.pow(points[i][j], 2);
                }
            }
            outputVectorArray[i] = sum;
        }

        RealMatrix coeffecientMatrix = MatrixUtils.createRealMatrix(coeffecientMatrixArray);
        RealVector outputVector = MatrixUtils.createRealVector(outputVectorArray);

        RealVector solutionVector = MatrixUtils.inverse(coeffecientMatrix).operate(outputVector);
        solutionVector = solutionVector.mapMultiply(-1.0);

        double[] solutionVectorArray = solutionVector.toArray();

        double sum = 0.0;
        for (int i = 0; i < solutionVectorArray.length-1; i++) {
            sum += Math.pow(solutionVectorArray[i]-points[0][i], 2);
        }
        
        solutionVectorArray[solutionVectorArray.length-1] = Math.sqrt(sum);

        return solutionVectorArray;
    }
}

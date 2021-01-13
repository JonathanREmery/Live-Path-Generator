import java.lang.Math;

public class PathGenerator {
	public static double[][] generatePath(double[][] points) {
		double[] polynomialCoeffecients = PolynomialFit.fit(points);

		double maxX = -9999.0;
		for (int i = 0; i < points.length; i++) {
			if (points[i][0] > maxX) {
				maxX = points[i][0];
			}
		}

		double[][] path = new double[(int)Math.ceil(maxX/0.1)+1][2];

		for (int i = 0; i < path.length; i++) {
			path[i][0] = i*0.1;
			path[i][1] = getDesiredState(polynomialCoeffecients, i*0.1);
		}
		
		return path;
	}

	public static double[][] generatePath(double[][] points, double[] headings) {
		double[] derivatives = convertHeadingsToDerivatives(headings);

		double[] polynomialCoeffecients = PolynomialFit.fit(points, derivatives);

		double maxX = -9999.0;
		for (int i = 0; i < points.length; i++) {
			if (points[i][0] > maxX) {
				maxX = points[i][0];
			}
		}

		double[][] path = new double[(int)Math.ceil(maxX/0.1)+1][2];

		for (int i = 0; i < path.length; i++) {
			path[i][0] = i*0.1;
			path[i][1] = getDesiredState(polynomialCoeffecients, i*0.1);
		}
		
		return path;
	}

	public static double[] convertHeadingsToDerivatives(double[] headings) {
		double[] derivatives = new double[headings.length];
		for (int i = 0; i < derivatives.length; i++) {
			derivatives[i] = Math.tan(headings[i] * Math.PI / 180.0);
		}
		return derivatives;
	}

	private static double getDesiredState(double[] polynomialCoeffecients, double x) {
		double sum = 0.0;
		for (int i = 0; i < polynomialCoeffecients.length; i++) {
			sum += polynomialCoeffecients[i]*(Math.pow(x, polynomialCoeffecients.length-(i+1)));
		}
		return sum;
	}
}

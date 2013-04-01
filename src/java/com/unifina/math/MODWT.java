package com.unifina.math;

public class MODWT {
	
	public static double[] incrementMODWTForward(double[] signal, int N, double[] wfilter, double[] sfilter) {
		double[] WV = new double[2];

		int k = N;
		for (int n = 0; n < wfilter.length; n++) {
			k --;
			if (k < 0)
				k += N;

			WV[0] += wfilter[n] * signal[k];
			WV[1] += sfilter[n] * signal[k];
		}
		
		return WV;
	}
	
	public static double incrementMODWTInverse(double[] W, int N, double[] wfilter, double[] sfilter) {
		double newV = 0;

		int k = N;
		for (int n = 0; n < wfilter.length; n++) {
			k ++;
			if (k >= N)
				k -= N;

			newV += (wfilter[n] * W[k]) /*+ (sfilter[n] * WV[1][k]) */; // is zero
		}
			
		return newV;
	}
	
	public static double[][] stepForwardMODWT(double[] oldV, int N, int j, double[] wfilter, double[] sfilter) {
		double[][] WV = new double[2][N];
		int dec = (int) Math.pow(2.0, j - 1);

		for (int t = 0; t < N; t++) {
			int k = t;
			WV[0][t] = wfilter[0] * oldV[k];
			WV[1][t] = sfilter[0] * oldV[k];

			for (int n = 1; n < wfilter.length; n++) {
				k -= dec;
				if (k < 0)
					k += N;

				WV[0][t] += wfilter[n] * oldV[k];
				WV[1][t] += sfilter[n] * oldV[k];
			}
		}
		return WV;
	}
	
	public static double[] stepInverseMODWT(double[][] WV, int N, int j, double[] wfilter, double[] sfilter) {
		double[] newV = new double[N];
		int inc = (int) Math.pow(2.0, j - 1);

		for (int t = 0; t < N; t++) {
			int k = t;
			newV[t] = (wfilter[0] * WV[0][k]) + (sfilter[0] * WV[1][k]);
			for (int n = 1; n < wfilter.length; n++) {
				k += inc;
				if (k >= N)
					k -= N;

				newV[t] += (wfilter[n] * WV[0][k]) + (sfilter[n] * WV[1][k]);
			}
		}
		return newV;
	}
	
}

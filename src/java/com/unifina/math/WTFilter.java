package com.unifina.math;


public class WTFilter {
	
	double[] h;
	double[] g;
	int length;
	boolean modwt;
	
	public WTFilter(String name, boolean modwt, int level) {
		this.modwt = modwt;
		
		if (name.equals("d4")) {
			length = 4;
		    g = new double[] {
		              0.4829629131445341,
		              0.8365163037378077,
		              0.2241438680420134,
		              -0.1294095225512603
		    };
		    
		    if (modwt)
		    	for (int i=0;i<g.length;i++)
		    		g[i] = g[i] / Math.sqrt(2);
		    
		    h = qmf(g,true);
		}
		else throw new IllegalArgumentException("Unknown filter name: "+name);
		
		if (level > 1) {
			double[][] hg = equivalent(h,g,level);
			h = hg[0];
			g = hg[1];
			length = h.length;
		}
	}
	
	public static double[] qmf(double[] x, boolean inverse) {
		int L = x.length;
		double[] y = new double[L];
		if (!inverse) {
			for (int l=0;l<L;l++)
				y[l] = Math.pow(-1, l+1) * x[L-1-l];
		}
		else {
			for (int l=0;l<L;l++)
				y[l] = Math.pow(-1, l) * x[L-1-l];
		}
		return y;
	}
	
	public static double[][] equivalent(double[] h, double[] g, int J) {
		int L = h.length;
		int lastL = L;
		double[] lastH = h;
		double[] lastG = g;
		
		for (int j=2;j<=J;j++) {
			int newL = ((int)Math.pow(2,j) - 1)*(L-1) + 1;
			double[] hj = new double[newL];
			double[] gj = new double[newL];

			for (int l=0; l<newL; l++) {
				int u = l;
				double gMult = (u>=L ? 0 : g[u]);
				double hjl = gMult * lastH[0];
				double gjl = gMult * lastG[0];
				
				for (int k=1; k<lastL; k++) {
					u -= 2;
					
					if (u<0 || u>=L)
						gMult = 0;
					else gMult = g[u];
					
					hjl = hjl + gMult * lastH[k];
					gjl = gjl + gMult * lastG[k];
				}
				
				hj[l] = hjl;
				gj[l] = gjl;
			}
			lastH = hj;
			lastG = gj;
			lastL = newL;
		}
		
		double[][] result = new double[2][];
		result[0] = lastH;
		result[1] = lastG;
		return result;
	}

	public double[] getH() {
		return h;
	}

	public void setH(double[] h) {
		this.h = h;
	}

	public double[] getG() {
		return g;
	}

	public void setG(double[] g) {
		this.g = g;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isModwt() {
		return modwt;
	}

	public void setModwt(boolean modwt) {
		this.modwt = modwt;
	}
}

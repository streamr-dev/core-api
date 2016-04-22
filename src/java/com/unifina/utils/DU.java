package com.unifina.utils;

/**
 * Double utils.
 * @author Henri
 *
 */
public class DU {
	private static final double pow = 8;
	private static final double epsilon = Math.pow(10, -pow);
	private static final double epsilonInverse = 1.0 / epsilon;

	/* Can't clean (or do precise math with) numbers that would exceed the Long.MAX_VALUE when divided by epsilon */
	private static final double maxCleanable = Long.MAX_VALUE * epsilon;
	
	public static int comp(double d1, double d2) {
		if (d1==d2 || Math.abs(d1-d2)<epsilon) return 0;
		else if (d1<d2) return -1;
		else return 1;
	}
	
	public static boolean eq(double d1, double d2) {
		return d1==d2 || Math.abs(d1-d2)<epsilon;
	}

	/** Round to nearest multiple of epsilon to clean away precision variance */
	public static double clean(double d) {
		if (Math.abs(d) > maxCleanable) { return d; }
		long fixedPoint = Math.round(d * epsilonInverse);
		return ((double)fixedPoint) * epsilon;
	}

	/**
	 * Rounds the price to the nearest multiple of tick, rounding down if the
	 * argument down is true, up otherwise.
	 */
	public static double roundTo(double price, double tick, boolean down) {
		double priceInTicks = down ? Math.floor(DU.clean(price/tick)) : Math.ceil(DU.clean(price/tick));
		return clean(priceInTicks * tick);
	}
	
	public static double roundTo(double price, double tick) {
		double timesInOne = 1/tick;
		double rounded = Math.round(price*timesInOne) / timesInOne;
		return rounded;
	}
	
}

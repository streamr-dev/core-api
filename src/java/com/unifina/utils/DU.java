package com.unifina.utils;

/**
 * Double utils.
 * @author Henri
 *
 */
public class DU {
	private static final double pow = 8;
	private static final double big = Math.pow(10,pow);
	private static final double epsilon = 1/pow;
	
	public static int comp(double d1, double d2) {
		if (d1==d2 || Math.abs(d1-d2)<epsilon) return 0;
		else if (d1<d2) return -1;
		else return 1;
	}
	
	public static boolean eq(double d1, double d2) {
		return d1==d2 || Math.abs(d1-d2)<epsilon;
	}
	
	public static double clean(double d) {
		long temp=Math.round(d*big);
		return ((double)temp)/big;
	}
	
	
	/**
	 * Rounds the price to the nearest multiple of tick, rounding down if the
	 * argument down is true, up otherwise.
	 */
	public static double roundTo(double price, double tick, boolean down) {
		double priceInTicks = down ? Math.floor(DU.clean(price/tick)) : Math.ceil(DU.clean(price/tick));
		return clean(priceInTicks * tick);
	}
	
}

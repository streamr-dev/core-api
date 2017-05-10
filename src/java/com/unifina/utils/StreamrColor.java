package com.unifina.utils;

import java.awt.Color;
import java.io.Serializable;

public class StreamrColor implements Serializable {
	private int red;
	private int green;
	private int blue;
	private double alpha;

	public StreamrColor(int red, int green, int blue, double alpha) {
		if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
			throw new IllegalArgumentException("RGB value must be between 0 and 255. r: " + red + ", g: " + green + ", b: " + blue);
		}
		if (alpha < 0.0 || alpha > 1.0) {
			throw new IllegalArgumentException("Alpha value must be between 0.0 and 1.0. a: " + alpha);
		}
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public StreamrColor(int red, int green, int blue) {
		this(red, green, blue, 1.0);
	}

	public StreamrColor(Color c) {
		this(c.getRed(), c.getGreen(), c.getBlue());
	}

	public StreamrColor(Color c, double alpha) {
		this(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	public int getRed() {
		return red;
	}
	public int getGreen() {
		return green;
	}
	public int getBlue() {
		return blue;
	}
	public double getAlpha() {
		return alpha;
	}

	public static StreamrColor parseRGBString(String rgb) {
		String[] splitted = rgb.split("rgba?\\(|\\, |\\,|\\)");
		int r = Integer.parseInt(splitted[1]);
		int g = Integer.parseInt(splitted[2]);
		int b = Integer.parseInt(splitted[3]);
		double a = 1.0;
		if (splitted.length > 4) {
			a = Double.parseDouble(splitted[4]);
		}
		return new StreamrColor(r, g, b, a);
	}

	@Override
	public String toString() {
		return "rgba(" + red + ", " + green + ", " + blue + ", " + alpha + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StreamrColor that = (StreamrColor) o;

		boolean alphasAreEqual = Math.abs(alpha - that.alpha) < Math.pow(10, -3);

		return red == that.red && green == that.green && blue == that.blue && alphasAreEqual;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = red;
		result = 31 * result + green;
		result = 31 * result + blue;
		temp = Double.doubleToLongBits(alpha);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}

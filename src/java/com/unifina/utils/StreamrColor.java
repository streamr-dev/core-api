package com.unifina.utils;


import java.awt.Color;
import java.io.Serializable;

public class StreamrColor implements Serializable{
	private int red;
	private int green;
	private int blue;

	public StreamrColor(int red, int green, int blue) {
		if(red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255)
			throw new IllegalArgumentException("RGB value must be between 0 and 255. r: " + red + ", g: " + green + ", b: " + blue);
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public StreamrColor(Color c) {
		this(c.getRed(), c.getGreen(), c.getBlue());
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

	@Override
	public String toString() {
		return "rgb(" + red + ", " + green + ", " + blue + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StreamrColor that = (StreamrColor) o;

		if (red != that.red) return false;
		if (green != that.green) return false;
		return blue == that.blue;

	}

	@Override
	public int hashCode() {
		int result = red;
		result = 31 * result + green;
		result = 31 * result + blue;
		return result;
	}
}

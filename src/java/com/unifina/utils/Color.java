package com.unifina.utils;


import java.io.Serializable;

public class Color implements Serializable{
	private int red;
	private int green;
	private int blue;

	public Color(int red, int green, int blue) {
		if(red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255)
			throw new IllegalArgumentException("RGB value must be between 0 and 255. r: " + red + ", g: " + green + ", b: " + blue);
		this.red = red;
		this.green = green;
		this.blue = blue;
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
}

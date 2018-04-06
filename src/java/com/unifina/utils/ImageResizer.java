package com.unifina.utils;

import com.unifina.api.ApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageResizer {
	private static final int HERO_WIDTH = 520;
	private static final int HERO_HEIGHT = 400;
	private static final int THUMB_WIDTH = 360;
	private static final int THUMB_HEIGHT = 210;
	public enum Size {
		HERO(HERO_WIDTH, HERO_HEIGHT),
		THUMB(THUMB_WIDTH, THUMB_HEIGHT);

		private final int width;
		private final int height;
		Size(final int width, final int height) {
			this.width = width;
			this.height = height;
		}
		public int width() { return width; }
		public int height() { return height; }
	}

	public byte[] resize(byte[] image, String filename, Size size) throws IOException {
		final BufferedImage original = ImageIO.read(new ByteArrayInputStream(image));
		final Image tmp = original.getScaledInstance(size.width(), size.height(), Image.SCALE_SMOOTH);
		final BufferedImage resized = new BufferedImage(size.width(), size.height(), original.getType());
		final Graphics2D g = resized.createGraphics();
		g.drawImage(tmp, 0, 0, null);
		g.dispose();

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(resized, guessImageFormat(filename), baos);
		return baos.toByteArray();
	}

	String guessImageFormat(final String filename) {
		if (filename == null || filename.length() == 0) {
			throw new ApiException(400, "FILENAME_REQUIRED", "file name is required");
		}
		return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
	}
}

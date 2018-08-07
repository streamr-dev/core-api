package com.unifina.utils;

import com.unifina.api.ApiException;
import org.imgscalr.Scalr;

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
		final BufferedImage source = ImageIO.read(new ByteArrayInputStream(image));
		BufferedImage tmp;
		// if source image is wider in shape than target image
		if (source.getWidth() / (double) source.getHeight() >= size.width() / (double) size.height()) {
			tmp = Scalr.resize(source, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, size.height());
			int x = (tmp.getWidth() - size.width()) / 2;
			if (x < 0) {
				x = 0;
			}
			if (x + size.width() > tmp.getWidth()) {
				x = 0;
			}
			final int y = 0;
			tmp = Scalr.crop(tmp, x, y, size.width(), size.height());
		} else { // else source image is taller in shape than target image
			tmp = Scalr.resize(source, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, size.width());
			int y = (tmp.getHeight() - size.height()) / 2;
			if (y < 0) {
				y = 0;
			}
			if (y + size.height() > tmp.getHeight()) {
				y = 0;
			}
			final int x = 0;
			tmp = Scalr.crop(tmp, x, y, size.width(), size.height());
		}

		final BufferedImage resized = new BufferedImage(size.width(), size.height(), source.getType());
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

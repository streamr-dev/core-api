package com.unifina.utils;

import com.unifina.api.ApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageVerifier {
	private final long maxSizeInBytes;
	private final int width;
	private final int height;

	public ImageVerifier(long maxSizeInBytes, int width, int height) {
		this.maxSizeInBytes = maxSizeInBytes;
		this.width = width;
		this.height = height;
	}

	public void verifyImage(byte[] imageBytes) throws FileTooLargeException, UnsupportedFileTypeException, UnexpectedImageDimensions {
		if (imageBytes.length > maxSizeInBytes) {
			throw new FileTooLargeException(imageBytes.length);
		}
		BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {
			throw new ApiException(500, "ERROR_READING_FILE", e.getMessage());
		}

		if (bufferedImage == null) {
			throw new UnsupportedFileTypeException();
		} else if (bufferedImage.getHeight() != height || bufferedImage.getWidth() != width) {
			throw new UnexpectedImageDimensions(bufferedImage);
		}
	}

	class FileTooLargeException extends ApiException {
		FileTooLargeException(long fileSize) {
			super(413, "FILE_TOO_LARGE", String.format("File size was %d bytes (> %d bytes)", fileSize, maxSizeInBytes));
		}
	}

	static class UnsupportedFileTypeException extends ApiException {
		UnsupportedFileTypeException() {
			super(415, "UNSUPPORTED_FILE_TYPE", "File type is not a recognized image format");
		}
	}

	class UnexpectedImageDimensions extends ApiException {
		UnexpectedImageDimensions(BufferedImage bufferedImage) {
			super(400, "UNEXPECTED_IMAGE_DIMENSIONS", String.format("Got %dx%d but expected %dx%d",
					bufferedImage.getWidth(), bufferedImage.getHeight(), width, height));
		}
	}

}

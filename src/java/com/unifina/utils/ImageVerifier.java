package com.unifina.utils;

import com.unifina.api.ApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageVerifier {
	private final long maxSizeInBytes;

	public ImageVerifier(long maxSizeInBytes) {
		this.maxSizeInBytes = maxSizeInBytes;
	}

	public void verifyImage(byte[] imageBytes) throws FileTooLargeException, UnsupportedFileTypeException {
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
			throw new UnsupportedFileTypeException(imageBytes.length);
		}
	}

	class FileTooLargeException extends ApiException {
		FileTooLargeException(long fileSize) {
			super(413, "FILE_TOO_LARGE", String.format("File size was %d bytes (> %d bytes)", fileSize, maxSizeInBytes));
		}
	}

	static class UnsupportedFileTypeException extends ApiException {
		UnsupportedFileTypeException(final long fileSize) {
			super(415, "UNSUPPORTED_FILE_TYPE",
					String.format("File type is not a recognized image format (size %d bytes)", fileSize));
		}
	}
}

package com.unifina.provider;

import java.net.URL;

public interface FileUploadProvider {
	URL uploadFile(String fileName, byte[] contents);

	void deleteFile(String fileName);
}

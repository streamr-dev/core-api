package core

import com.google.common.hash.Hashing
import com.unifina.provider.S3FileUploadProvider
import com.unifina.utils.ImageResizer
import grails.util.Holders
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger

final Logger log = Logger.getLogger("2018-05-23-fix-product-images.groovy")

databaseChangeLog = {
	changeSet(author: "kkn", id: "fix-product-images") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, image_url, thumbnail_url FROM product WHERE image_url IS NOT NULL") { row ->
					String id = row['id']
					String imageUrl = row['image_url']
					String thumbnailUrl = row['thumbnail_url']

					// download image
					byte[] bytes
					try {
						def input = new BufferedInputStream(new URL(imageUrl).openConnection().getInputStream())
						bytes = IOUtils.toByteArray(input)
						input.close()
					} catch (IOException e) {
						log.error("Failed to read " + imageUrl + ", skipping this file", e)
						return
					}

					def i = imageUrl.lastIndexOf(".")
					String ext
					if (i == -1) {
						ext = ".jpg"
					} else {
						ext = imageUrl.substring(i)
					}

					// scale image to thumbnail
					byte[] thumb = new ImageResizer().resize(bytes, "thumbimage" + ext, ImageResizer.Size.THUMB)
					// Use SHA-256 hash of the thumbnail bytes as the filename
					String thumbFilename = Hashing.sha256()
						.hashBytes(thumb)
						.toString()

					def s3 = new S3FileUploadProvider(
						(String) Holders.config.streamr.fileUpload.s3.region,
						(String) Holders.config.streamr.fileUpload.s3.bucket
					)
					// upload new thumbnail
					def newThumbUrl = s3.uploadFile("product-images/" + thumbFilename + ext, thumb).toString()

					// remove old thumbnail
					if (thumbnailUrl) {
						s3.deleteFile(thumbnailUrl)
					}

					// update row
					sql.execute('UPDATE product SET thumbnail_url = ? WHERE id = ?', [newThumbUrl, id])
				}
			}
		}
	}
}

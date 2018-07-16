package core

import com.google.common.hash.Hashing
import com.unifina.provider.S3FileUploadProvider
import com.unifina.utils.ImageResizer
import grails.util.Holders
import org.apache.commons.io.IOUtils

databaseChangeLog = {
	changeSet(author: "kkn", id: "fix-product-images") {
		grailsChange {
			change {
				sql.eachRow("SELECT id, image_url, thumbnail_url FROM product") { row ->
					String id = row['id']
					String imageUrl = row['image_url']
					String thumbnailUrl = row['thumbnail_url']

					// download image
					def url = new URL(imageUrl)
					def con = url.openConnection()
					con.setRequestProperty("Referer", "https://streamr.com")
					def input = new BufferedInputStream(con.getInputStream())
					def bytes = IOUtils.toByteArray(input)
					input.close()

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
					s3.deleteFile(thumbnailUrl)

					// update row
					sql.execute('UPDATE product SET thumbnail_url = ? WHERE id = ?', [newThumbUrl, id])
				}
			}
		}
	}
}

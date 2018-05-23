package core

import com.unifina.provider.S3FileUploadProvider
import com.unifina.utils.IdGenerator
import com.unifina.utils.ImageResizer
import grails.util.Holders
import org.apache.commons.io.IOUtils

databaseChangeLog = {
	changeSet(author: "kkn", id: "fix-product-images") {
		grailsChange {
			change {
				// User Agent is required for images hosted at pexels.com
				String chrome = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
				System.setProperty("http.agent", chrome)

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
					}
					ext = imageUrl.substring(i)

					// scale image to thumbnail
					def thumb = new ImageResizer().resize(bytes, "thumbimage" + ext, ImageResizer.Size.THUMB)

					// remove thumbnail
					def s3 = new S3FileUploadProvider(
						(String) Holders.config.streamr.fileUpload.s3.region,
						(String) Holders.config.streamr.fileUpload.s3.bucket
					)
					// upload new thumbnail
					def newThumbUrl = s3.uploadFile("product-images/" + IdGenerator.get() + ext, thumb).toString()

					s3.deleteFile(thumbnailUrl)

					// update row
					sql.execute('UPDATE product SET thumbnail_url = ? WHERE id = ?', [newThumbUrl, id])
				}
			}
		}
	}
}

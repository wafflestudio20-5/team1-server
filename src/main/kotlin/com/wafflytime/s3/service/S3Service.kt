package com.wafflytime.s3.service

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.util.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.util.UUID

@Service
class S3Service(
    private val s3Client: AmazonS3Client
) {

    @Value("\${cloud.aws.s3.bucket}")
    lateinit var bucket: String

    @Value("\${spring.profiles.active}")
    lateinit var baseDir: String

    fun uploadFile(file: MultipartFile) : String {
        val fileName = UUID.randomUUID().toString() + "-" + file.originalFilename
        val objMeta = ObjectMetadata()

        val bytes = IOUtils.toByteArray(file.inputStream)
        objMeta.contentLength = bytes.size.toLong()

        val byteArrayIs = ByteArrayInputStream(bytes)

        val s3FileKey = baseDir + fileName
        s3Client.putObject(PutObjectRequest(
            bucket, s3FileKey, byteArrayIs, objMeta
        ).withCannedAcl(CannedAccessControlList.PublicRead))
        return s3Client.getUrl(bucket, s3FileKey).toString()
    }
}

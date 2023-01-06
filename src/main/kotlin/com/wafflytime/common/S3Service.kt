package com.wafflytime.common

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.util.IOUtils
import com.wafflytime.board.database.image.ImageColumn
import com.wafflytime.board.dto.ImageResponse
import com.wafflytime.board.dto.ImageRequest
import com.wafflytime.board.dto.S3ImageUrlDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.util.Date
import java.util.UUID

@Service
class S3Service(
    private val s3Client: AmazonS3Client
) {

    @Value("\${cloud.aws.s3.bucket}")
    lateinit var bucket: String

    @Value("\${spring.profiles.active}")
    lateinit var baseDir: String

    private val expTimeMillis = 1000 * 60 * 2

    private val awsDnsSuffix: String = "amazonaws.com/"

    fun parseS3UrlToKey(s3Url: String) : String {
        return s3Url.split(awsDnsSuffix)[1]
    }
    fun getFolder() : String {
        val now = LocalDate.now()
        return "${baseDir}/${now.year}/${now.monthValue}/${now.dayOfYear}/"
    }

    fun getPreSignedUrl(s3FileKey: String, method: HttpMethod) : String {
        val expiration = Date()
        expiration.time += expTimeMillis
        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucket, s3FileKey)
            .withMethod(method)
            .withExpiration(expiration)
        val url = s3Client.generatePresignedUrl(generatePresignedUrlRequest)
        val preSignedUrl = url.toString()
        return preSignedUrl
    }

    fun uploadFile(file: MultipartFile) : String {
        val fileName = UUID.randomUUID().toString() + "-" + file.originalFilename
        val objMeta = ObjectMetadata()

        val bytes = IOUtils.toByteArray(file.inputStream)
        objMeta.contentLength = bytes.size.toLong()

        val byteArrayIs = ByteArrayInputStream(bytes)
        val s3FileKey = getFolder() + fileName
        s3Client.putObject(PutObjectRequest(
            bucket, s3FileKey, byteArrayIs, objMeta
        ).withCannedAcl(CannedAccessControlList.PublicRead))
        return s3Client.getUrl(bucket, s3FileKey).toString()
    }

    fun getPreSignedUrlsAndS3Urls(files: List<ImageRequest>?) : MutableList<S3ImageUrlDto>? {
        if (files == null) return null

        val s3ImageUrlDtoList = mutableListOf<S3ImageUrlDto>()
        files.forEach {
            val fileNameWithUUID = UUID.randomUUID().toString() + "-" + it.fileName
            val s3FileKey = getFolder() + fileNameWithUUID
            val preSignedUrl = getPreSignedUrl(s3FileKey, HttpMethod.PUT)
            val s3Url = s3Client.getUrl(bucket, s3FileKey).toString()

            s3ImageUrlDtoList.add(S3ImageUrlDto(s3Url, preSignedUrl, it.description))
        }
        return s3ImageUrlDtoList
    }

    fun getPreSignedUrlsFromS3Keys(images: List<ImageColumn>?) : List<ImageResponse>? {
        if (images == null) return null

        val imageResponseList = mutableListOf<ImageResponse>()
        images.forEach {
            imageResponseList.add(
                ImageResponse(getPreSignedUrl(parseS3UrlToKey(it.s3Urls), HttpMethod.GET), it.description)
            )
        }
        return imageResponseList
    }

    fun deleteFiles(images: List<ImageColumn>?) {
        images?.forEach {
            s3Client.deleteObject(bucket, parseS3UrlToKey(it.s3Urls))
        }
    }

    @Async("deleteS3FileExecutor")
    fun deleteListOfFiles(listOfImages: List<List<ImageColumn>?>) {
        listOfImages.forEach { deleteFiles(it) }
    }

}

package com.wafflytime.common

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.util.IOUtils
import com.wafflytime.post.database.image.ImageColumn
import com.wafflytime.post.dto.*
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

    fun gerPreSignedUrlAndS3Url(fileName: String) : S3ImageUrlDto {
        val fileNameWithUUID = UUID.randomUUID().toString() + "-" + fileName
        val s3FileKey = getFolder() + fileNameWithUUID
        val preSignedUrl = getPreSignedUrl(s3FileKey, HttpMethod.PUT)
        val s3Url = s3Client.getUrl(bucket, s3FileKey).toString()
        return S3ImageUrlDto(s3Url, preSignedUrl)
    }

    fun gerPreSignedUrlAndS3Url(file: ImageRequest) : S3PostImageUrlDto {
        val s3ImageUrlDto = gerPreSignedUrlAndS3Url(file.fileName)
        return S3PostImageUrlDto(
            file.imageId, file.fileName,
            s3ImageUrlDto.s3Url, s3ImageUrlDto.preSignedUrl, file.description)
    }

    fun getPreSignedUrlsAndS3Urls(files: List<ImageRequest>?): MutableList<S3PostImageUrlDto>? {
        if (files == null) return null
        return files.map { gerPreSignedUrlAndS3Url(it) }.toMutableList()
    }

    fun getPreSignedUrlsFromS3Keys(images: Map<String, ImageColumn>?) : List<ImageResponse>? {
        if (images == null) return null

        val imageResponseList = mutableListOf<ImageResponse>()
        images.forEach {
            imageResponseList.add(
                ImageResponse(it.value.imageId, it.key, getPreSignedUrl(parseS3UrlToKey(it.value.s3Url), HttpMethod.GET), it.value.description)
            )
        }
        return imageResponseList
    }

    fun getPreSignedUrlFromS3Key(s3Url: String?) : String? {
        if (s3Url == null) return null
        return getPreSignedUrl(parseS3UrlToKey(s3Url), HttpMethod.GET)
    }

    fun deleteFiles(images: Map<String, ImageColumn>?) {
        images?.forEach {
            s3Client.deleteObject(bucket, parseS3UrlToKey(it.value.s3Url))
        }
    }

    fun deleteFiles(imageFileNames: List<String?>?) {
        imageFileNames?.forEach {
            it?.let { s3Client.deleteObject(bucket, parseS3UrlToKey(it)) }
        }
    }

    fun deleteFile(s3FileKey: String?) {
        s3FileKey?.let { s3Client.deleteObject(bucket, parseS3UrlToKey(it)) }
    }

    @Async("deleteS3FileExecutor")
    fun deleteListOfFiles(listOfImages: List<Map<String, ImageColumn>?>) {
        listOfImages.forEach { deleteFiles(it) }
    }

    fun updateImageRequest(
        dbImages: Map<String, ImageColumn>?,
        request: UpdatePostRequest
    ): MutableList<S3PostImageUrlDto>? {
        if (dbImages == null) return getPreSignedUrlsAndS3Urls(request.images)
        if (request.images == null) return null

        /** TODO(??????)
        - ?????? ????????? ?????????????????? ????????? update request ?????? '?????????' ????????? ???????????? ????????? ???????????? ?????? ??????, images ?????????
        ????????? ????????? ????????? ??? ????????? ????????? ?????????????????? ??????(????????? ????????? ??????????????? ?????? ????????? ????????? UpdateRequest ??? ?????????).
        ??????????????? ?????? ????????? ?????????????????? ????????? ???????????? ??????????????? ???????????? ?????? ??? ??????
         **/
        val s3PostImageUrlDtoList = mutableListOf<S3PostImageUrlDto>()
        request.images.forEach {
            if (dbImages.containsKey(it.fileName)) {
                // ????????? client ?????? ????????? ???????????? ?????? ????????? ?????? ????????? state ??????????????? ?????? - ??? ?????? ????????? ???????????? ?????????
                // ?????? s3??? ????????? ???????????? s3 url??? ???????????? ????????? preSignedUrl??? null??? ????????????
                s3PostImageUrlDtoList.add(
                    S3PostImageUrlDto(
                        imageId = it.imageId,
                        fileName = it.fileName,
                        s3Url = dbImages.get(it.fileName)!!.s3Url,
                        preSignedUrl = null,
                        description = it.description
                    )
                )
            } else {
                // new image
                s3PostImageUrlDtoList.add(gerPreSignedUrlAndS3Url(it))
            }
        }
        deleteFiles(request.deletedFileNames?.map { dbImages.get(it)?.s3Url })
        return s3PostImageUrlDtoList
    }
}

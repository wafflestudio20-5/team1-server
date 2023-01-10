package com.wafflytime.common

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.util.IOUtils
import com.wafflytime.post.database.image.ImageColumn
import com.wafflytime.post.dto.ImageResponse
import com.wafflytime.post.dto.ImageRequest
import com.wafflytime.post.dto.S3ImageUrlDto
import com.wafflytime.post.dto.UpdatePostRequest
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

    fun gerPreSignedUrlAndS3Url(file: ImageRequest) : S3ImageUrlDto {
        val fileNameWithUUID = UUID.randomUUID().toString() + "-" + file.fileName
        val s3FileKey = getFolder() + fileNameWithUUID
        val preSignedUrl = getPreSignedUrl(s3FileKey, HttpMethod.PUT)
        val s3Url = s3Client.getUrl(bucket, s3FileKey).toString()
        return S3ImageUrlDto(file.imageId, file.fileName, s3Url, preSignedUrl, file.description)
    }

    fun getPreSignedUrlsAndS3Urls(files: List<ImageRequest>?): MutableList<S3ImageUrlDto>? {
        if (files == null) return null
        return files.map { gerPreSignedUrlAndS3Url(it) }.toMutableList()
    }

    fun getPreSignedUrlsFromS3Keys(images: Map<String, ImageColumn>?) : List<ImageResponse>? {
        if (images == null) return null

        val imageResponseList = mutableListOf<ImageResponse>()
        images.forEach {
            imageResponseList.add(
                ImageResponse(it.value.imageId, getPreSignedUrl(parseS3UrlToKey(it.value.s3Url), HttpMethod.GET), it.value.description)
            )
        }
        return imageResponseList
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

    @Async("deleteS3FileExecutor")
    fun deleteListOfFiles(listOfImages: List<Map<String, ImageColumn>?>) {
        listOfImages.forEach { deleteFiles(it) }
    }

    fun updateImageRequest(
        dbImages: Map<String, ImageColumn>?,
        request: UpdatePostRequest
    ): MutableList<S3ImageUrlDto>? {
        if (dbImages == null) return getPreSignedUrlsAndS3Urls(request.images)
        if (request.images == null) return null

        /** TODO(재웅)
        - 현재 방식은 클라이언트가 보통의 update request 처럼 '변화된' 필드에 데이터를 담아서 보내주는 것이 아닌, images 필드는
        반드시 유저의 게시물 그 상태를 그대로 전달해주어야 한다(사진이 바뀌지 않았더라도 사진 정보를 그대로 UpdateRequest 에 담아서).
        프론트에서 이걸 어떻게 전달해주는게 좋을지 프론트랑 얘개히보고 수정하면 좋을 듯 하다
         **/
        val s3ImageUrlDtoList = mutableListOf<S3ImageUrlDto>()
        request.images.forEach {
            if (dbImages.containsKey(it.fileName)) {
                // 현재는 client 바뀐 정보만 전달하는 것이 아니라 현재 이미지 state 전달한다고 가정 - 이 부분 추후에 프론트와 이야기
                // 이미 s3에 푸시된 이미지라 s3 url이 존재하기 때문에 preSignedUrl을 null로 넘겨준다
                s3ImageUrlDtoList.add(
                    S3ImageUrlDto(
                        imageId = it.imageId,
                        fileName = it.fileName,
                        s3Url = dbImages.get(it.fileName)!!.s3Url,
                        preSignedUrl = null,
                        description = it.description
                    )
                )
            } else {
                // new image
                s3ImageUrlDtoList.add(gerPreSignedUrlAndS3Url(it))
            }
        }
        deleteFiles(request.deletedFileNames?.map { dbImages.get(it)?.s3Url })
        return s3ImageUrlDtoList
    }
}

package com.wafflytime.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsConfiguration {

    @Value("\${cloud.aws.credentials.accessKey}")
    lateinit var acceessKey: String

    @Value("\${cloud.aws.credentials.accessSecret}")
    lateinit var accessSecret: String

    @Value("\${cloud.aws.region.static}")
    lateinit var region: String
    @Bean
    fun s3() : AmazonS3Client {
        val awsCredentials = BasicAWSCredentials(acceessKey, accessSecret)
        return AmazonS3ClientBuilder.standard().withCredentials(
            AWSStaticCredentialsProvider(awsCredentials)
        ).withRegion(region).build() as AmazonS3Client
    }
}
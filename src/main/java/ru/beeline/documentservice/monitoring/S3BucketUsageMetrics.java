/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class S3BucketUsageMetrics {

    private static final String METRIC_USED = "document_service_s3_bucket_used_bytes";
    private static final String METRIC_QUOTA = "document_service_s3_bucket_quota_bytes";

    private final MinioClient minioClient;
    private final String bucketName;
    private final AtomicLong usedBytes = new AtomicLong(-1);
    private final AtomicLong quotaBytes;
    private final boolean enabled;

    public S3BucketUsageMetrics(MinioClient minioClient,
                                MeterRegistry registry,
                                @Value("${aws.s3.bucket.name}") String bucketName,
                                @Value("${aws.s3.bucket.quota-bytes:20474836480}") long quotaBytesConfig,
                                @Value("${aws.s3.bucket.usage.metrics.enabled:true}") boolean enabled) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.quotaBytes = new AtomicLong(Math.max(0L, quotaBytesConfig));
        this.enabled = enabled;

        Gauge.builder(METRIC_USED, usedBytes, AtomicLong::get)
                .description("Занятое место в бакете, в байтах S3 (пересчитывается по расписанию; scrape /actuator/prometheus)")
                .baseUnit("bytes")
                .register(registry);

        Gauge.builder(METRIC_QUOTA, quotaBytes, AtomicLong::get)
                .description("Установка лимита размера бакета, в байтах (same value as aws.s3.bucket.quota-bytes)")
                .baseUnit("bytes")
                .register(registry);
    }

    @Scheduled(
            initialDelayString = "${aws.s3.bucket.usage.metrics.initial-delay-ms:5000}",
            fixedDelayString = "${aws.s3.bucket.usage.metrics.poll-interval-ms:3600000}"
    )
    public void poll() {
        if (!enabled) {
            return;
        }
        log.info("S3 bucket usage poll started for bucket {}", bucketName);
        try {
            long total = 0L;
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .recursive(true)
                            .build());
            for (Result<Item> result : objects) {
                Item item = result.get();
                if (!item.isDir()) {
                    total += item.size();
                }
            }
            usedBytes.set(total);
            double totalGB = total / (1024.0 * 1024.0 * 1024.0);
            long bucketLimitBytes = quotaBytes.get();
            double percentUsed = (total * 100.0) / bucketLimitBytes;
            log.info("Объем занятого места в S3: {} байт ({} GB). Занято {}% от лимита ({} GB).",
                    total, totalGB, String.format("%.2f", percentUsed), bucketLimitBytes / (1024.0 * 1024.0 * 1024.0));
        } catch (Exception e) {
            log.warn("S3 bucket usage poll failed for bucket {}: {}", bucketName, e.getMessage());
        }
    }
}

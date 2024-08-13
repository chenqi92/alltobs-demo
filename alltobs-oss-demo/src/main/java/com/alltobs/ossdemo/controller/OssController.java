package com.alltobs.ossdemo.controller;

import com.alltobs.oss.service.OssTemplate;
import com.alltobs.ossdemo.config.util.FileUtil;
import com.alltobs.ossdemo.config.util.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssTemplate ossTemplate;

    /**
     * 创建 Bucket
     *
     * @param bucketName bucket 名称
     * @return R
     */
    @PostMapping("/createBucket")
    public R<String> createBucket(@RequestParam String bucketName) {
        ossTemplate.createBucket(bucketName);
        return R.ok("Bucket created: " + bucketName);
    }

    /**
     * 获取所有 Bucket
     *
     * @return R
     */
    @GetMapping("/getAllBuckets")
    public R<List<String>> getAllBuckets() {
        return R.ok(ossTemplate.getAllBuckets());
    }

    /**
     * 获取 Bucket 属性
     *
     * @param bucketName bucket 名称
     * @return R
     */
    @GetMapping("/getBucket")
    public R<?> getBucket(@RequestParam String bucketName) {
        return R.ok(ossTemplate.getBucketProperties(bucketName));
    }

    /**
     * 删除 Bucket
     *
     * @param bucketName bucket 名称
     * @return R
     */
    @DeleteMapping("/removeBucket")
    public R<String> removeBucket(@RequestParam String bucketName) {
        ossTemplate.removeBucket(bucketName);
        return R.ok("Bucket removed: " + bucketName);
    }

    /**
     * 获取 Bucket 的所有对象 以文件前缀查找
     *
     * @param bucketName bucket 名称
     * @return R
     */
    @GetMapping("/getAllObjectsByPrefix")
    public R<Set<String>> getAllObjectsByPrefix(@RequestParam String bucketName, @RequestParam String prefix) {
        return R.ok(ossTemplate.getAllObjectsByPrefix(bucketName, prefix).stream().map(S3Object::key).collect(Collectors.toSet()));
    }

    /**
     * 查询带过期时间的文件链接，分钟
     *
     * @param bucketName bucket 名称
     * @return R
     */
    @GetMapping("/getObjectURL")
    public R<String> getObjectURL(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam int minutes) {
        return R.ok(ossTemplate.getObjectURL(bucketName, objectName, minutes));
    }

    /**
     * 获取文件对象
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @return R
     */
    @GetMapping("/getObject")
    public R<byte[]> getObject(@RequestParam String bucketName, @RequestParam String objectName) {
        try (var s3Object = ossTemplate.getObject(bucketName, objectName)) {
            return R.ok(s3Object.readAllBytes());
        } catch (IOException e) {
            return R.fail(e.getLocalizedMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @param response   HttpServletResponse
     */
    @GetMapping("/download")
    public void download(@RequestParam String bucketName, @RequestParam String objectName, HttpServletResponse response) {
        try (ResponseInputStream<GetObjectResponse> inputStream = ossTemplate.getObject(bucketName, objectName);
             OutputStream outputStream = response.getOutputStream()) {

            // 获取文件的Content-Type
            String contentType = inputStream.response().contentType();
            response.setContentType(contentType);

            // 设置响应头：Content-Disposition，用于浏览器下载文件时的文件名
            response.setHeader("Content-Disposition", "attachment; filename=\"" + objectName + "\"");

            // 直接将输入流中的数据传输到输出流
            inputStream.transferTo(outputStream);

            // 刷新输出流，确保所有数据已写出
            outputStream.flush();

        } catch (IOException e) {
            // 如果出错，设置响应状态为404
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 上传文件
     *
     * @param bucketName 桶名
     * @param file       文件
     * @return R
     */
    @PostMapping("/putObject")
    public R<String> putObject(@RequestParam String bucketName, @RequestParam MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            String uuid = UUID.randomUUID() + "." + FileUtil.getFileType(fileName);
            ossTemplate.putObject(bucketName, uuid, file.getInputStream());
            return R.ok("File uploaded: " + uuid);
        } catch (IOException e) {
            return R.fail("Failed to upload file: " + fileName);
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @return R
     */
    @DeleteMapping("/removeObject")
    public R<String> removeObject(@RequestParam String bucketName, @RequestParam String objectName) {
        ossTemplate.removeObject(bucketName, objectName);
        return R.ok("Object removed: " + objectName);
    }

    /**
     * 复制文件
     *
     * @param sourceBucketName      源桶名
     * @param sourceKey             源对象名
     * @param destinationBucketName 目标桶名
     * @param destinationKey        目标对象名
     * @return R
     */
    @PostMapping("/copyObject")
    public R<String> copyObject(@RequestParam String sourceBucketName, @RequestParam String sourceKey,
                                @RequestParam String destinationBucketName, @RequestParam String destinationKey) {
        ossTemplate.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
        return R.ok("Object copied from " + sourceKey + " to " + destinationKey);
    }

    /**
     * 获取文件 ACL
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @return R
     */
    @GetMapping("/getObjectAcl")
    public R<String> getObjectAcl(@RequestParam String bucketName, @RequestParam String objectName) {
        return R.ok(ossTemplate.getObjectAcl(bucketName, objectName).toString());
    }

    /**
     * 设置文件 ACL
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @param acl        ACL
     * @return R
     */
    @PostMapping("/setObjectAcl")
    public R<String> setObjectAcl(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam String acl) {
        ossTemplate.setObjectAcl(bucketName, objectName, acl);
        return R.ok("ACL set for object: " + objectName);
    }

    /**
     * 启用或禁用对象版本控制
     *
     * @param bucketName 桶名
     * @param enable     是否启用
     * @return R
     */
    @PostMapping("/setBucketVersioning")
    public R<String> setBucketVersioning(@RequestParam String bucketName, @RequestParam boolean enable) {
        ossTemplate.setBucketVersioning(bucketName, enable);
        return R.ok("Versioning set to " + (enable ? "Enabled" : "Suspended") + " for bucket: " + bucketName);
    }

    /**
     * 设置文件标签
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @return R
     */
    @PostMapping("/setObjectTags")
    public R<String> setObjectTags(@RequestParam String bucketName, @RequestParam String objectName, @RequestBody Map<String, String> tags) {
        ossTemplate.setObjectTags(bucketName, objectName, tags);
        return R.ok("Tags set for object: " + objectName);
    }

    /**
     * 获取文件标签
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @return R
     */
    @GetMapping("/getObjectTags")
    public R<Map<String, String>> getObjectTags(@RequestParam String bucketName, @RequestParam String objectName) {
        return R.ok(ossTemplate.getObjectTags(bucketName, objectName));
    }

    /**
     * 上传文件并设置过期时间
     *
     * @param bucketName 桶名
     * @param file       文件
     * @param days       过期天数只支持以天为单位删除文件
     * @return R
     */
    @PostMapping("putObjectWithExpiration")
    public R<String> putObjectWithExpiration(@RequestParam String bucketName, @RequestParam MultipartFile file, @RequestParam int days) {
        String fileName = file.getOriginalFilename();
        try {
            String uuid = UUID.randomUUID() + "." + FileUtil.getFileType(fileName);
            ossTemplate.putObjectWithExpiration(bucketName, uuid, file.getInputStream(), days);
            return R.ok("File uploaded: " + uuid);
        } catch (IOException e) {
            return R.fail("Failed to upload file: " + fileName);
        }
    }

    /**
     * 上传文件并加密
     *
     * @param bucketName 桶名
     * @param file       文件
     * @return R
     */
    @PostMapping("/uploadWithEncryption")
    public R<String> uploadWithEncryption(@RequestParam String bucketName,
                                          @RequestParam("file") MultipartFile file) {
        String uuid = UUID.randomUUID() + "." + FileUtil.getFileType(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectResponse response = ossTemplate.uploadWithEncryption(
                    bucketName,
                    uuid,
                    inputStream,
                    file.getSize(),
                    file.getContentType()
            );
            return R.ok("File uploaded with encryption: " + response.toString());
        } catch (IOException e) {
            return R.fail("File upload failed: " + e.getMessage());
        }
    }

    /**
     * 分片上传
     *
     * @param bucketName 桶名
     * @param file       文件
     * @return R
     */
    @PostMapping("/uploadMultipart")
    public R<String> uploadMultipart(@RequestParam String bucketName,
                                     @RequestParam MultipartFile file) throws IOException, InterruptedException {
        String objectName = file.getOriginalFilename();
        // 初始化分片上传
        String uploadId = ossTemplate.initiateMultipartUpload(bucketName, objectName);
        System.out.println("Upload ID: " + uploadId);

        // 将文件按部分大小（5MB）分块上传
        long partSize = 5 * 1024 * 1024;
        long fileSize = file.getSize();
        int partCount = (int) Math.ceil((double) fileSize / partSize);

        // 用于存储已上传的部分
        List<CompletedPart> completedParts = Collections.synchronizedList(new ArrayList<>());

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(partCount, 10));

        for (int i = 0; i < partCount; i++) {
            final int partNumber = i + 1;
            long startPos = i * partSize;
            long size = Math.min(partSize, fileSize - startPos);

            executor.submit(() -> {
                try (InputStream inputStream = file.getInputStream()) {
                    inputStream.skip(startPos);
                    byte[] buffer = new byte[(int) size];
                    int bytesRead = inputStream.read(buffer, 0, (int) size);

                    if (bytesRead > 0) {
                        CompletedPart part = ossTemplate.uploadPart(bucketName, objectName, uploadId, partNumber, buffer);
                        completedParts.add(part);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        // 检查是否成功上传了所有部分
        if (completedParts.size() == partCount) {
            // 在完成上传之前，按 partNumber 升序排序
            completedParts.sort(Comparator.comparing(CompletedPart::partNumber));

            // 完成分片上传
            ossTemplate.completeMultipartUpload(bucketName, objectName, uploadId, completedParts);
            return R.ok("Upload completed successfully uploadId: " + uploadId);
        } else {
            // 如果有部分上传失败，取消上传
            ossTemplate.abortMultipartUpload(bucketName, objectName, uploadId);
            return R.fail("Upload failed, some parts are missing.");
        }
    }

    /**
     * 断点续传
     *
     * @param bucketName 桶名
     * @param file       文件
     * @param uploadId   上传 ID
     * @return R
     */
    @PostMapping("/resumeMultipart")
    public R<String> resumeMultipart(@RequestParam String bucketName,
                                     @RequestParam MultipartFile file,
                                     @RequestParam String uploadId) throws IOException, InterruptedException {
        String objectName = file.getOriginalFilename();

        // 将文件读入内存
        byte[] fileBytes = file.getBytes();

        // 获取已经上传的部分
        List<CompletedPart> completedParts = ossTemplate.listParts(bucketName, objectName, uploadId);

        // 继续上传未完成的部分
        long partSize = 5 * 1024 * 1024;
        long fileSize = fileBytes.length;
        int partCount = (int) Math.ceil((double) fileSize / partSize);

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(partCount, 10));

        for (int i = completedParts.size(); i < partCount; i++) {
            final int partNumber = i + 1;
            long startPos = i * partSize;
            long size = Math.min(partSize, fileSize - startPos);

            executor.submit(() -> {
                try {
                    byte[] buffer = Arrays.copyOfRange(fileBytes, (int) startPos, (int) (startPos + size));
                    CompletedPart part = ossTemplate.uploadPart(bucketName, objectName, uploadId, partNumber, buffer);
                    synchronized (completedParts) {
                        completedParts.add(part);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        // 按 partNumber 升序排序
        completedParts.sort(Comparator.comparing(CompletedPart::partNumber));

        // 完成分片上传
        ossTemplate.completeMultipartUpload(bucketName, objectName, uploadId, completedParts);

        return R.ok("Upload resumed and completed successfully");
    }

    /**
     * 生成预签名 URL
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     * @param expiration 过期时间（秒）
     * @return R
     */
    @GetMapping("/generatePreSignedUrl")
    public R<String> generatePreSignedUrl(@RequestParam String bucketName,
                                          @RequestParam String objectName,
                                          @RequestParam int expiration) {
        String preSignedUrl = ossTemplate.generatePreSignedUrlForPut(bucketName, objectName, expiration);
        return R.ok(preSignedUrl);
    }
}

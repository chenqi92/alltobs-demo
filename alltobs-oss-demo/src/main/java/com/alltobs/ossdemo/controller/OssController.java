package com.alltobs.ossdemo.controller;

import cn.allbs.oss.service.OssTemplate;
import com.alltobs.ossdemo.config.util.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssTemplate ossTemplate;

    @PostMapping("/createBucket")
    public R<String> createBucket(@RequestParam String bucketName) {
        ossTemplate.createBucket(bucketName);
        return R.ok("Bucket created: " + bucketName);
    }

    @GetMapping("/getAllBuckets")
    public R<List<String>> getAllBuckets() {
        return R.ok(ossTemplate.getAllBuckets());
    }

    @GetMapping("/getBucket")
    public R<?> getBucket(@RequestParam String bucketName) {
        return R.ok(ossTemplate.getBucketProperties(bucketName));
    }

    @DeleteMapping("/removeBucket")
    public R<String> removeBucket(@RequestParam String bucketName) {
        ossTemplate.removeBucket(bucketName);
        return R.ok("Bucket removed: " + bucketName);
    }

    @GetMapping("/getAllObjectsByPrefix")
    public R<List<S3Object>> getAllObjectsByPrefix(@RequestParam String bucketName, @RequestParam String prefix) {
        return R.ok(ossTemplate.getAllObjectsByPrefix(bucketName, prefix));
    }

    @GetMapping("/getObjectURL")
    public R<String> getObjectURL(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam int minutes) {
        return R.ok(ossTemplate.getObjectURL(bucketName, objectName, minutes));
    }

    @GetMapping("/getObject")
    public R<byte[]> getObject(@RequestParam String bucketName, @RequestParam String objectName) {
        try (var s3Object = ossTemplate.getObject(bucketName, objectName)) {
            return R.ok(s3Object.readAllBytes());
        } catch (IOException e) {
            return R.fail(e.getLocalizedMessage());
        }
    }

    @PostMapping("/putObject")
    public R<String> putObject(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam MultipartFile file) {
        try {
            ossTemplate.putObject(bucketName, objectName, file.getInputStream());
            return R.ok("File uploaded: " + objectName);
        } catch (IOException e) {
            return R.fail("Failed to upload file: " + objectName);
        }
    }

    @DeleteMapping("/removeObject")
    public R<String> removeObject(@RequestParam String bucketName, @RequestParam String objectName) {
        ossTemplate.removeObject(bucketName, objectName);
        return R.ok("Object removed: " + objectName);
    }

    @PostMapping("/copyObject")
    public R<String> copyObject(@RequestParam String sourceBucketName, @RequestParam String sourceKey,
                                @RequestParam String destinationBucketName, @RequestParam String destinationKey) {
        ossTemplate.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
        return R.ok("Object copied from " + sourceKey + " to " + destinationKey);
    }

    @GetMapping("/getObjectAcl")
    public R<String> getObjectAcl(@RequestParam String bucketName, @RequestParam String objectName) {
        return R.ok(ossTemplate.getObjectAcl(bucketName, objectName).toString());
    }

    @PostMapping("/setObjectAcl")
    public R<String> setObjectAcl(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam String acl) {
        ossTemplate.setObjectAcl(bucketName, objectName, acl);
        return R.ok("ACL set for object: " + objectName);
    }

    @PostMapping("/setBucketVersioning")
    public R<String> setBucketVersioning(@RequestParam String bucketName, @RequestParam boolean enable) {
        ossTemplate.setBucketVersioning(bucketName, enable);
        return R.ok("Versioning set to " + (enable ? "Enabled" : "Suspended") + " for bucket: " + bucketName);
    }

    @PostMapping("/setObjectTags")
    public R<String> setObjectTags(@RequestParam String bucketName, @RequestParam String objectName, @RequestBody Map<String, String> tags) {
        ossTemplate.setObjectTags(bucketName, objectName, tags);
        return R.ok("Tags set for object: " + objectName);
    }

    @GetMapping("/getObjectTags")
    public R<Map<String, String>> getObjectTags(@RequestParam String bucketName, @RequestParam String objectName) {
        return R.ok(ossTemplate.getObjectTags(bucketName, objectName));
    }
}

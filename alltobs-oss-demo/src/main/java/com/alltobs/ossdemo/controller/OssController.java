package com.alltobs.ossdemo.controller;

import cn.allbs.oss.service.OssTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssTemplate ossTemplate;

    @PostMapping("/createBucket")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName) {
        ossTemplate.createBucket(bucketName);
        return ResponseEntity.ok("Bucket created: " + bucketName);
    }

    @GetMapping("/getAllBuckets")
    public ResponseEntity<List<Bucket>> getAllBuckets() {
        return ResponseEntity.ok(ossTemplate.getAllBuckets());
    }

    @GetMapping("/getBucket")
    public ResponseEntity<Optional<Bucket>> getBucket(@RequestParam String bucketName) {
        return ResponseEntity.ok(ossTemplate.getBucket(bucketName));
    }

    @DeleteMapping("/removeBucket")
    public ResponseEntity<String> removeBucket(@RequestParam String bucketName) {
        ossTemplate.removeBucket(bucketName);
        return ResponseEntity.ok("Bucket removed: " + bucketName);
    }

    @GetMapping("/getAllObjectsByPrefix")
    public ResponseEntity<List<S3Object>> getAllObjectsByPrefix(@RequestParam String bucketName, @RequestParam String prefix) {
        return ResponseEntity.ok(ossTemplate.getAllObjectsByPrefix(bucketName, prefix));
    }

    @GetMapping("/getObjectURL")
    public ResponseEntity<String> getObjectURL(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam int minutes) {
        return ResponseEntity.ok(ossTemplate.getObjectURL(bucketName, objectName, minutes));
    }

    @GetMapping("/getObject")
    public ResponseEntity<byte[]> getObject(@RequestParam String bucketName, @RequestParam String objectName) {
        try (var s3Object = ossTemplate.getObject(bucketName, objectName)) {
            return ResponseEntity.ok(s3Object.readAllBytes());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/putObject")
    public ResponseEntity<String> putObject(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam MultipartFile file) {
        try {
            ossTemplate.putObject(bucketName, objectName, file.getInputStream());
            return ResponseEntity.ok("File uploaded: " + objectName);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload file: " + objectName);
        }
    }

    @DeleteMapping("/removeObject")
    public ResponseEntity<String> removeObject(@RequestParam String bucketName, @RequestParam String objectName) {
        ossTemplate.removeObject(bucketName, objectName);
        return ResponseEntity.ok("Object removed: " + objectName);
    }

    @PostMapping("/copyObject")
    public ResponseEntity<String> copyObject(@RequestParam String sourceBucketName, @RequestParam String sourceKey,
                                             @RequestParam String destinationBucketName, @RequestParam String destinationKey) {
        ossTemplate.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
        return ResponseEntity.ok("Object copied from " + sourceKey + " to " + destinationKey);
    }

    @GetMapping("/getObjectAcl")
    public ResponseEntity<String> getObjectAcl(@RequestParam String bucketName, @RequestParam String objectName) {
        return ResponseEntity.ok(ossTemplate.getObjectAcl(bucketName, objectName).toString());
    }

    @PostMapping("/setObjectAcl")
    public ResponseEntity<String> setObjectAcl(@RequestParam String bucketName, @RequestParam String objectName, @RequestParam String acl) {
        ossTemplate.setObjectAcl(bucketName, objectName, acl);
        return ResponseEntity.ok("ACL set for object: " + objectName);
    }

    @PostMapping("/setBucketVersioning")
    public ResponseEntity<String> setBucketVersioning(@RequestParam String bucketName, @RequestParam boolean enable) {
        ossTemplate.setBucketVersioning(bucketName, enable);
        return ResponseEntity.ok("Versioning set to " + (enable ? "Enabled" : "Suspended") + " for bucket: " + bucketName);
    }

    @PostMapping("/setObjectTags")
    public ResponseEntity<String> setObjectTags(@RequestParam String bucketName, @RequestParam String objectName, @RequestBody Map<String, String> tags) {
        ossTemplate.setObjectTags(bucketName, objectName, tags);
        return ResponseEntity.ok("Tags set for object: " + objectName);
    }

    @GetMapping("/getObjectTags")
    public ResponseEntity<Map<String, String>> getObjectTags(@RequestParam String bucketName, @RequestParam String objectName) {
        return ResponseEntity.ok(ossTemplate.getObjectTags(bucketName, objectName));
    }
}

package com.alltobs.ossdemo.config.util;

import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 类 FileUtil
 *
 * @author ChenQi
 * &#064;date 2024/7/27
 */
public class FileUtil {

    /**
     * 计算文件的 MD5
     *
     * @param file 文件
     * @return String
     */
    public static String calculateMd5(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        }
    }

    /**
     * 获取文件类型
     *
     * @param fileName 文件名
     * @return String
     */
    public static String getFileType(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return null; // 文件没有后缀名
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * MultipartFile 转 File
     *
     * @param file 文件
     * @return File
     */
    public static File convertMultiPartToFile(MultipartFile file, String tempDir) throws Exception {
        File convFile = new File(tempDir, Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}

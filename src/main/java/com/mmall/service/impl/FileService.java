package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 描述：文件上传业务
 * 作者：NearJC
 * 时间：2019.4.26
 */
@Service("fileService")
public class FileService implements IFileService {

    private static Logger log = LoggerFactory.getLogger(FileService.class);

    @Override
    public String upload(MultipartFile multipartFile, String path) {
        // 获取文件名称
        String originalFilename = multipartFile.getOriginalFilename();
        // 获取文件后缀名
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        // 定义新文件名称
        String fileName = UUID.randomUUID().toString() + "." + suffixName;

        // 判断本地临时目录是否存在
        File tempDir = new File(path);
        if (!tempDir.exists()) {
            tempDir.setWritable(true);
            tempDir.mkdirs();
        }

        boolean uploaded;
        File targetFile = null;
        try {
            // 创建本地临时目标文件对象
            targetFile = new File(tempDir, fileName);
            // 存储到本地临时目录
            multipartFile.transferTo(targetFile);
            // 存储到FTP服务器
            uploaded = FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 删除本地临时文件
            targetFile.delete();
        } catch (IOException e) {
            uploaded = false;
            log.error("FTP连接释放异常", e);
        }

        if (!uploaded) {
            return null;
        }

        return targetFile.getName();
    }
}

package com.mmall.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * 描述：文件上传工具类
 * 作者：NearJC
 * 时间：2019.4.27
 */
@Slf4j
public class FTPUtil {

    private String ip;
    private int port;
    private String username;
    private String password;
    private FTPClient ftpClient;

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    public FTPUtil(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass);
        log.info("开始上传.....");
        boolean isSuccess = ftpUtil.uploadFile(fileList, "img");
        log.info("上传结束，结果:{}", isSuccess);

        return isSuccess;
    }

    private boolean uploadFile(List<File> fileList, String remote) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;

        try {
            // 连接服务器
            if (connectServer(ip, username, password)) {
                ftpClient.changeWorkingDirectory(remote);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                for (File targetFile : fileList) {
                    fis = new FileInputStream(targetFile);
                    ftpClient.storeFile(targetFile.getName(), fis);
                }
            }
        } catch (IOException e) {
            uploaded = false;
            log.error("上传图片到FTP服务器异常", e);
        } finally {
            fis.close();
            ftpClient.disconnect();
        }

        return uploaded;
    }

    private boolean connectServer(String ip, String user, String pass) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();

        try {
            ftpClient.connect(ip);    // 连接
            isSuccess = ftpClient.login(user, pass); // 登录
        } catch (IOException e) {
            log.error("FTP服务器连接异常", e);
        }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

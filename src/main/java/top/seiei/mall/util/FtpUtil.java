package top.seiei.mall.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FtpUtil {

    private static Log logger = LogFactory.getLog(FtpUtil.class);
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftppwd = PropertiesUtil.getProperty("ftp.password");
    private static int ftpPort = Integer.parseInt(PropertiesUtil.getProperty("ftp.port"));
    private static FTPClient ftpClient;

    /**
     * 上传文件列表到 ftp 服务器，默认的服务器参数在配置文件中获取
     * @param remotePath 存放到 ftp 服务器的文件目录，如数值为 "img" ，会在 ftp 服务器的存放文件的根目录下再创建名为 img 的文件夹
     * @param fileList 要上传的文件列表
     * @return 上传是否成功
     */
    public static boolean uploadFile(String remotePath, List<File> fileList) {
        boolean isSuccess = false;
        FileInputStream fileInputStream = null;
        if (connectFtpServer(ftpIp, ftpPort, ftpUser, ftppwd)) {
            try {
                // 更改存放路径，remotePath 为空表示不需要更改存放路径
                ftpClient.changeWorkingDirectory(remotePath);
                // 设置缓冲区
                ftpClient.setBufferSize(1024);
                // 设置编码
                ftpClient.setControlEncoding("UTF-8");
                // 设置文件为二进制模式，可以防止乱码
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 打开本地的被动模式
                ftpClient.enterLocalPassiveMode();
                // 循环 fileList 上传文件到 ftp 服务器
                for (File fileItem : fileList) {
                    fileInputStream = new FileInputStream(fileItem);
                    logger.info("开始上传 ftp 服务器文件：" + fileItem.getName());
                    ftpClient.storeFile(fileItem.getName(), fileInputStream);
                }
                isSuccess = true;
            } catch (IOException e) {
                logger.error("上传文件到ftp服务器异常", e);
            } finally {
                // 清理资源
                try {
                    ftpClient.disconnect();
                    if (fileInputStream != null){
                        fileInputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("关闭上传文件资源异常", e);
                }
            }
        }
        logger.info("上传 ftp 服务器文件结果：" + isSuccess);
        return isSuccess;
    }

    /**
     * 连接 ftp 服务器
     * @param ftpIp 服务器 ip
     * @param ftpPort 端口
     * @param ftpUser 用户名
     * @param ftppwd 密码
     * @return 是否连接成功
     */
    private static boolean connectFtpServer(String ftpIp,int ftpPort, String ftpUser, String ftppwd) {
        boolean isConnect = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpIp, ftpPort);
            isConnect = ftpClient.login(ftpUser, ftppwd);
        } catch (IOException e) {
            logger.error("连接ftp服务器异常", e);
        }
        return isConnect;
    }


}

package top.seiei.mall.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.seiei.mall.service.IFileService;
import top.seiei.mall.util.FtpUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


/**
 * 文件上传到 ftp 服务器中
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private static Log logger = LogFactory.getLog(FileServiceImpl.class);

    /**
     * MultpartFile 文件上传到 tomcat 服务器后再上传到 ftp 服务器，最后删除 tomcat 服务器中的文件，这里不是定义静态方法是因为使用 spring @service注解
     * @param file 上传文件
     * @param path 上传到 tomcat 服务器的路径
     * @return 上传文件名称
     */
    public String upload(MultipartFile file, String path) {

        // getName : 获取表单中文件组件的名字
        // getOriginalFilename : 获取上传文件的原名
        String fileName = file.getOriginalFilename();
        // 获取文件扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf("." ) + 1);
        // 自定义文件名称（包括扩展名）
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;

        // 查看存放文件目录是否存在，如果不存在就创建该目录
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            // 设置可写权限
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        // 创建存放文件
        File uploadFile = new File(path, uploadFileName);
        try {
            // 将上传文件保存到刚创建的存放文件中
            file.transferTo(uploadFile);
            // 将存放在 tomcat 服务器中的文件上传到 ftp 服务器根目录中的 img 目录中
            boolean result = FtpUtil.uploadFile("img", Lists.newArrayList(uploadFile));
            // 文件上传到 ftp 服务器之后无论是否成功，删除 tomcat 服务器中的文件
            uploadFile.delete();
            // 上传 ftp 不成功
            if (!result) {
                uploadFileName = null;
            }
        } catch (IOException e) {
            logger.error("上传文件到Tomcat服务器异常", e);
            return null;
        }
        return uploadFileName;
    }

}
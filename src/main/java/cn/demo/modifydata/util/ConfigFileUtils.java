package cn.demo.modifydata.util;

import cn.demo.modifydata.ConfigModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ConfigFileUtils {

    private static final String RBF_START_KEY = "rbf.start";
    private static final String RBF_END_KEY = "rbf.end";
    private static final String RZ_START_KEY = "rz.start";
    private static final String RZ_END_KEY = "rz.end";

    public static ConfigModel readConfigFile() {
        Properties properties = new Properties();
        ConfigModel result = new ConfigModel();
        try {
            properties.load(FileUtils.openInputStream(new File("config/config.properties")));
            String rbfStartStr = properties.getProperty(RBF_START_KEY);
            String rbfEndStr = properties.getProperty(RBF_END_KEY);
            if(StringUtils.isEmpty(StringUtils.trim(rbfStartStr))) {
                throw new RuntimeException("rbf.start配置信息不能为空！");
            }
            if(StringUtils.isEmpty(StringUtils.trim(rbfEndStr))) {
                throw new RuntimeException("rbf.end配置信息不能为空！");
            }

            String rzStartStr = properties.getProperty(RZ_START_KEY);
            String rzEndStr = properties.getProperty(RZ_END_KEY);

            if(StringUtils.isEmpty(StringUtils.trim(rzStartStr))) {
                throw new RuntimeException("rz.start配置信息不能为空！");
            }
            if(StringUtils.isEmpty(StringUtils.trim(rzEndStr))) {
                throw new RuntimeException("rz.end配置信息不能为空！");
            }

            result.setRbfStart(Integer.valueOf(rbfStartStr));
            result.setRbfEnd(Integer.valueOf(rbfEndStr));

            result.setRzStart(Integer.valueOf(rzStartStr));
            result.setRzEnd(Integer.valueOf(rzEndStr));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("读取配置文件失败！");
        }

        return result;
    }
}

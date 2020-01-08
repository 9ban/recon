package com.nineban.finance.recon.util;

import com.aliyun.oss.OSSClient;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;


@Component
public class OSSUtil  {
    private final Logger logger = LoggerFactory.getLogger(getClass());

//
    private String accessKeyId = "xxxx";
    private String secretAccessKey = "xxx";
    private String endpoint="xxx";
    private OSSClient client;
    private static final String WEB_BUCKET_NAME = "xxx";
    private static final String OSS_BUCKET_URL = "xxx";
    public OSSUtil(String accessKeyId, String secretAccessKey) {this(null, accessKeyId, secretAccessKey);}

    /**
     * Instantiates a new Oss util.
     *
     * @param endpoint        the endpoint
     * @param accessKeyId     the access key id
     * @param secretAccessKey the secret access key
     */
    public OSSUtil(String endpoint, String accessKeyId, String secretAccessKey) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.endpoint = endpoint;
        initClient(endpoint, accessKeyId, secretAccessKey);
    }
//
    public OSSUtil() {
        initClient(endpoint,accessKeyId,secretAccessKey);
    }
//
    /**
     * 初始化client
     *
     * @param endpoint
     * @param accessKeyId
     * @param secretAccessKey
     */
    private void initClient(String endpoint, String accessKeyId, String secretAccessKey) {
        if (client == null) {
            if (StringUtils.isNotEmpty(endpoint)) {
                client = new OSSClient(endpoint, accessKeyId, secretAccessKey);
            } else {
                client = new OSSClient(accessKeyId, secretAccessKey);
            }
        }
    }
    /**
     * Upload file string.
     *
     * @param folderName  the folder name
     * @param fileName    the file name
     * @param inputStream the input stream
     * @return string string
     * @throws Exception the exception
     */
    public String uploadFile(String folderName, String fileName, InputStream inputStream) throws Exception {
        if (inputStream != null) {
            StringBuilder key = new StringBuilder(folderName);
            if (!folderName.endsWith("/")) {
                key.append("/");
            }
            key.append(fileName);
            client.putObject(WEB_BUCKET_NAME, key.toString(), inputStream);
            return OSS_BUCKET_URL + key.toString();
        } else {
            return null;
        }
    }


}

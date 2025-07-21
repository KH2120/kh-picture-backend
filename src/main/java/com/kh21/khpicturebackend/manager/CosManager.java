package com.kh21.khpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.kh21.khpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.jsoup.internal.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;

@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;

    /**
     * 上传图片
     *
     * @param key  唯一值
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传图片 并解析
     *
     * @param key  唯一值
     * @param file 文件
     * @return
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        // 返回图片信息
        picOperations.setIsPicInfo(1);
        // 处理规则
        ArrayList<PicOperations.Rule> rules = new ArrayList<>();

        // 图片压缩
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        rules.add(compressRule);

        // 图片加载优化
        PicOperations.Rule thumbRule = new PicOperations.Rule();
        thumbRule.setBucket(cosClientConfig.getBucket());
        String thumbKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
        thumbRule.setFileId(thumbKey);
        thumbRule.setRule(String.format("imageMogr2/thumbnail/%sx%s", 128, 128));
        rules.add(thumbRule);


        // 返回
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载图片
     *
     * @param key 唯一值
     * @return
     */
    public COSObject getObecjt(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param url
     * @throws CosClientException
     */
    public void deleteObject(String url) throws CosClientException {
        String host = cosClientConfig.getHost();
        String key = url.replace(host, "");
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}

package com.kh21.khpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.kh21.khpicturebackend.config.CosClientConfig;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.manager.CosManager;
import com.kh21.khpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    protected CosClientConfig cosClientConfig;
    @Resource
    protected CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource      图片
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validPicture(inputSource);
        // 2. 文件上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFileName(inputSource);
        String uploadName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%S/%S", uploadPathPrefix, uploadName);
        // 3. 解析结果并返回
        File file = null;

        try {
            // 3. 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源（url，本地）
            processFile(inputSource, file);
            // 4. 上传图片到对象存储
            log.error("cosManager => {}", cosManager);

            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);

            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (objectList != null) {
                CIObject ciObject = objectList.get(0);
                return buildResult(originalFilename, ciObject);
            }


            // 5. 封装返回结果
            return buildResult(uploadPath, originalFilename, file, imageInfo);
        } catch (Exception e) {
            log.error("file upload error，filepath = {}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 4. 临时文件清理
            clearTempFile(file);
        }


    }

    /**
     * 封装返回结果
     *
     * @param originalFilename
     * @param ciObject
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject ciObject) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();

        Integer picWidth = ciObject.getWidth();
        Integer picHeight = ciObject.getHeight();
        double scale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + ciObject.getKey());
        uploadPictureResult.setPicName(originalFilename);
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(scale);
        uploadPictureResult.setPicFormat(ciObject.getFormat());
        uploadPictureResult.setPicSize(ciObject.getSize().longValue());
        return uploadPictureResult;
    }

    /**
     * 封装返回结果
     *
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @param imageInfo
     * @return
     */
    private UploadPictureResult buildResult(String uploadPath, String originalFilename, File file, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        double scale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicScale(scale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 处理输入源并生成本地临时文件
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    /**
     * 处理文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFileName(Object inputSource);


    /**
     * 文件校验
     *
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);


    /**
     * 清理临时文件
     *
     * @param file
     */
    public static void clearTempFile(File file) {
        if (file != null) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.error("file delete error，filepath = {}", file.getAbsoluteFile());
            }
        }
    }


}

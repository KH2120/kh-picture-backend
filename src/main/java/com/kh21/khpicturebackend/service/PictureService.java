package com.kh21.khpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh21.khpicturebackend.model.dto.picture.*;
import com.kh21.khpicturebackend.model.entity.Picture;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author KH2120
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-07-19 17:59:07
 */
public interface PictureService extends IService<Picture> {
    /**
     * 图片上传
     *
     * @param inputSource          文件
     * @param pictureUploadRequest 图片id
     * @param loginUser            上传用户
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 封装单个图片
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片分页
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 数据校验
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param reviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest reviewRequest, User loginUser);

    /**
     * 填充审核状态
     *
     * @param picture
     * @param user
     */
    void fillReviewParams(Picture picture, User user);

    void fillReviewParamsPlus(Picture picture, User user);

    /**
     * 批量抓图和创建
     *
     * @param pictureUploadbyBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadbyBatchRequest, User loginUser);

    /**
     * 删除图片
     *
     * @param oldPic
     */
    public void clearPictureFile(Picture oldPic);

    /**
     * 检查空间图片权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除托
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(Long pictureId, User loginUser);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 通过颜色找图片
     *
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureVOByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量处理
     *
     * @param pictureEditByBatchRequest
     * @param loginuser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginuser);
}

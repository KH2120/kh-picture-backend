package com.kh21.khpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh21.khpicturebackend.model.dto.picture.PictureQueryRequest;
import com.kh21.khpicturebackend.model.dto.picture.PictureUploadRequest;
import com.kh21.khpicturebackend.model.entity.Picture;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.PictureVO;
import com.kh21.khpicturebackend.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author KH2120
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-07-19 17:59:07
 */
public interface PictureService extends IService<Picture> {
    /**
     * 图片上传
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 图片id
     * @param loginUser            上传用户
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

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
}

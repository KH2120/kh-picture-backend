package com.kh21.khpicturebackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kh21.khpicturebackend.annotation.AuthCheck;
import com.kh21.khpicturebackend.common.BaseResponse;
import com.kh21.khpicturebackend.common.DeleteRequest;
import com.kh21.khpicturebackend.common.ResultUtils;
import com.kh21.khpicturebackend.constant.UserConstant;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.model.dto.picture.*;
import com.kh21.khpicturebackend.model.entity.Picture;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.enums.PicturereviewStatusEnum;
import com.kh21.khpicturebackend.model.vo.PictureTagCategory;
import com.kh21.khpicturebackend.model.vo.PictureVO;
import com.kh21.khpicturebackend.service.PictureService;
import com.kh21.khpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    /**
     * 图片上传
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> pictureUpload(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        log.info("pictureUploadRequest = {}", pictureUploadRequest.getId());
        User user = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, user);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id为空");
        }
        //校验数据
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        // 判断图片是否存在
        Picture oldPic = pictureService.getById(id);
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员才可以删除
        if (!oldPic.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean removed = pictureService.removeById(deleteRequest);
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     *
     * @param pictureUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        // 判断空
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 转实体类
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // list转为string
        String tags = JSONUtil.toJsonStr(pictureUpdateRequest.getTags());
        picture.setTags(tags);
        // 图片校验
        pictureService.validPicture(picture);
        // 判断图片是否存在
        Picture oldPic = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR);
        // 自动过审
        pictureService.fillReviewParams(picture, userService.getLoginUser(request));
        // 操作数据
        boolean updated = pictureService.updateById(picture);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片（不脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id) {
        // 判空
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 返回
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取图片封装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest request) {
        // 判空
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 返回
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页查询图片列表
     *
     * @param queryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest queryRequest) {
        // 查询
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(queryRequest));
        // 返回
        return ResultUtils.success(picturePage);

    }

    /**
     * 获取图片分页封装类
     *
     * @param queryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest queryRequest, HttpServletRequest request) {
        // 查询
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
//        防止爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        用户只允许获取已过审的图片
        queryRequest.setReviewStatus(PicturereviewStatusEnum.PASS.getValue());

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(queryRequest));
        // 返回
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        // 判断空
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 转实体类
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // list转为string
        String tags = JSONUtil.toJsonStr(pictureEditRequest.getTags());
        picture.setTags(tags);
        picture.setEditTime(new Date());
        // 图片校验
        pictureService.validPicture(picture);
        // 判断图片是否存在
        Picture oldPic = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可以编辑
        User loginUser = userService.getLoginUser(request);
        if (!oldPic.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 自动过审
        pictureService.fillReviewParams(picture, userService.getLoginUser(request));
        // 操作数据
        boolean updated = pictureService.updateById(picture);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片（管理员）
     *
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doReviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

}

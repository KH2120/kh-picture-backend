package com.kh21.khpicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kh21.khpicturebackend.annotation.AuthCheck;
import com.kh21.khpicturebackend.api.imageSearch.ImageSearchApiFacade;
import com.kh21.khpicturebackend.api.imageSearch.model.ImageSearchResult;
import com.kh21.khpicturebackend.common.BaseResponse;
import com.kh21.khpicturebackend.common.DeleteRequest;
import com.kh21.khpicturebackend.common.ResultUtils;
import com.kh21.khpicturebackend.constant.UserConstant;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.model.dto.picture.*;
import com.kh21.khpicturebackend.model.entity.Picture;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.enums.PicturereviewStatusEnum;
import com.kh21.khpicturebackend.model.vo.PictureTagCategory;
import com.kh21.khpicturebackend.model.vo.PictureVO;
import com.kh21.khpicturebackend.service.PictureService;
import com.kh21.khpicturebackend.service.SpaceService;
import com.kh21.khpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Autowired
    StringRedisTemplate redisTemplate;

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

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> pictureUploadByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        log.info("pictureUploadRequest = {}", pictureUploadRequest.getId());
        User user = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, user);
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
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureService.deletePicture(deleteRequest.getId(), userService.getLoginUser(request));
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
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
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
        // 空间权限校验
        Long spaceId = queryRequest.getSpaceId();
// 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            queryRequest.setReviewStatus(PicturereviewStatusEnum.PASS.getValue());
            queryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
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
        pictureService.editPicture(pictureEditRequest, userService.getLoginUser(request));
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

    /**
     * 图片抓取并创建
     *
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 获取图片封装类（缓存）
     *
     * @param queryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest queryRequest, HttpServletRequest request) {
        // 查询
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
//        防止爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        用户只允许获取已过审的图片
        queryRequest.setReviewStatus(PicturereviewStatusEnum.PASS.getValue());

        // 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(queryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKEy = "khpicture:listPictureVOByPage" + hashKey;

        // 1. 本地查询
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKEy);
        if (cachedValue != null) {
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(pictureVOPage);
        }


        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        // 查询以缓存数据
        cachedValue = valueOperations.get(cacheKEy);
        // 查询是否有缓存，有则返回
        if (cachedValue != null) {
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(pictureVOPage);
        }


        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(queryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        String cacheValue = JSONUtil.toJsonStr(pictureService.getPictureVOPage(picturePage, request));
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);

        LOCAL_CACHE.put(cacheKEy, cacheValue);

        valueOperations.set(cacheKEy, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        // 返回
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(oldPicture.getUrl());
        return ResultUtils.success(resultList);
    }



}

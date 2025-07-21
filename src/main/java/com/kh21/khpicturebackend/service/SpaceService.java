package com.kh21.khpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh21.khpicturebackend.model.dto.space.SpaceAddRequesst;
import com.kh21.khpicturebackend.model.dto.space.SpaceQueryRequest;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.SpaceVO;

/**
 * @author KH2120
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-07-21 15:43:22
 */
public interface SpaceService extends IService<Space> {
    /**
     * 校验空间
     *
     * @param space
     * @param isAdd
     */
    void validSpace(Space space, boolean isAdd);

    /**
     * 填充数据
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 获取空间封装类
     *
     * @param space
     * @return
     */
    SpaceVO getSpaceVO(Space space);

    /**
     * 获取QueryWrapper
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 添加空间
     *
     * @param spaceAddRequesst
     * @param loginUser
     * @return
     */
    Long addSpace(SpaceAddRequesst spaceAddRequesst, User loginUser);
}

package com.kh21.khpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kh21.khpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.kh21.khpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.kh21.khpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.SpaceUserVO;
import com.kh21.khpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author KH2120
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-07-25 15:30:38
 */
public interface SpaceUserService extends IService<SpaceUser> {
    /**
     * 添加空间成员
     *
     * @param spaceUserAddRequest
     * @return
     */
    Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验数据
     *
     * @param spaceUser
     * @param isAdd
     */
    void validSpaceUser(SpaceUser spaceUser, boolean isAdd);

    /**
     * 获取查询条件
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取封装类
     *
     * @param spaceUser
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser);

    /**
     * 获取封装类列表
     *
     * @param list
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> list);


}

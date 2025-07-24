package com.kh21.khpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kh21.khpicturebackend.model.dto.space.analyze.*;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.space.analyze.*;

import java.util.List;

/**
 * @author KH2120
 * @createDate 2025-07-21 15:43:22
 */
public interface SpaceAnalyzeService extends IService<Space> {
    /**
     * 空间利用分析
     *
     * @param request
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyzeResponse(SpaceUsageAnalyzeRequest request, User loginUser);

    /**
     * 图片分类分析
     *
     * @param request
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest request, User loginUser);

    /**
     * 标签分析
     *
     * @param request
     * @param loginUser
     * @return
     */
    List<SpaceTagsAnalyzeResponse> getSpaceTagsAnalyzeResponse(SpaceTagsAnalyzeRequest request, User loginUser);

    /**
     * 用户分析
     *
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */

    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 排行榜
     *
     * @param rankAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest rankAnalyzeRequest, User loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);
}

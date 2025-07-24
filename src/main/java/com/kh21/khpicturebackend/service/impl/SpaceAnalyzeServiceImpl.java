package com.kh21.khpicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh21.khpicturebackend.exception.BusinessException;
import com.kh21.khpicturebackend.exception.ErrorCode;
import com.kh21.khpicturebackend.exception.ThrowUtils;
import com.kh21.khpicturebackend.mapper.SpaceMapper;
import com.kh21.khpicturebackend.model.dto.space.analyze.*;
import com.kh21.khpicturebackend.model.entity.Picture;
import com.kh21.khpicturebackend.model.entity.Space;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.space.analyze.*;
import com.kh21.khpicturebackend.service.PictureService;
import com.kh21.khpicturebackend.service.SpaceAnalyzeService;
import com.kh21.khpicturebackend.service.SpaceService;
import com.kh21.khpicturebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author KH2120
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-07-21 15:43:22
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Autowired
    UserService userService;
    @Autowired
    SpaceService spaceService;

    @Autowired
    PictureService pictureService;

    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest request, User loginUser) {
        if (request.isQueryAll() || request.isQueryPublic()) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        } else {
            Long spaceId = request.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "房间不存在");
            // 仅本人或管理员
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    private void fillSpaceAnalyzeParam(SpaceAnalyzeRequest request, QueryWrapper<Picture> queryWrapper) {
        if (request.isQueryAll()) return;
        if (request.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = request.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }


    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyzeResponse(SpaceUsageAnalyzeRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        // 查询全部或公共空间
        if (request.isQueryAll() || request.isQueryPublic()) {
            boolean isAdmin = userService.isAdmin(loginUser);
            ThrowUtils.throwIf(!isAdmin, ErrorCode.NO_AUTH_ERROR);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            if (request.isQueryPublic()) {
                queryWrapper.isNull("spaceId");
            }
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = pictureObjList.stream().mapToLong(result ->
                    result instanceof Long ? (Long) result : 0
            ).sum();
            long usedCount = pictureObjList.size();
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(usedSize);
            response.setUsedCount(usedCount);
            response.setMaxSize(null);
            response.setSizeUsageRatio(null);
            response.setMaxCount(null);
            response.setCountUsageRatio(null);
            return response;
        } else {
            Long spaceId = request.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            spaceService.checkSpaceAuth(loginUser, space);
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getTotalSize());
            response.setUsedCount(space.getTotalCount());
            response.setMaxSize(space.getMaxSize());
            response.setMaxCount(space.getMaxCount());
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            response.setSizeUsageRatio(sizeUsageRatio);
            response.setCountUsageRatio(countUsageRatio);
            return response;
        }

    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyzeResponse(SpaceCategoryAnalyzeRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(request, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillSpaceAnalyzeParam(request, queryWrapper);
        queryWrapper.select(
                "category AS category",
                "COUNT AS count",
                "SUM(picSize) AS totalSize");

        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String category = result.get("category") != null ? (String) result.get("category") : "未分类";
            Long count = ((Number) result.get("count")).longValue();
            Long totalSize = ((Number) result.get("totalSize")).longValue();
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagsAnalyzeResponse> getSpaceTagsAnalyzeResponse(SpaceTagsAnalyzeRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(request, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillSpaceAnalyzeParam(request, queryWrapper);
        queryWrapper.select("tags");
        List<String> jsonStrList = pictureService.getBaseMapper().selectList(queryWrapper).stream()
                .filter(ObjectUtil::isNotNull).map(Object::toString)
                .collect(Collectors.toList());
        Map<String, Long> tagCountMap = jsonStrList.stream().flatMap(tagJsonStr -> JSONUtil.toList(tagJsonStr, String.class).stream()).collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
                .map(entry -> new SpaceTagsAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        fillSpaceAnalyzeParam(spaceUserAnalyzeRequest, queryWrapper);

        // 分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') AS period", "COUNT(*) AS count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) AS period", "COUNT(*) AS count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period", "COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        // 分组和排序
        queryWrapper.groupBy("period").orderByAsc("period");

        // 查询结果并转换
        List<Map<String, Object>> queryResult = pictureService.getBaseMapper().selectMaps(queryWrapper);
        return queryResult.stream()
                .map(result -> {
                    String period = result.get("period").toString();
                    Long count = ((Number) result.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(period, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest rankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(rankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权查看空间排行");
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "usedId", "totalSize").orderByDesc("totalSize").last("LIMIT" + rankAnalyzeRequest.getTopN());
        return spaceService.list(queryWrapper);
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 检查权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillSpaceAnalyzeParam(spaceSizeAnalyzeRequest, queryWrapper);

        // 查询所有符合条件的图片大小
        queryWrapper.select("picSize");
        List<Long> picSizes = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .map(size -> ((Number) size).longValue())
                .collect(Collectors.toList());

        // 定义分段范围，注意使用有序 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", picSizes.stream().filter(size -> size < 100 * 1024).count());
        sizeRanges.put("100KB-500KB", picSizes.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", picSizes.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
        sizeRanges.put(">1MB", picSizes.stream().filter(size -> size >= 1 * 1024 * 1024).count());

        // 转换为响应对象
        return sizeRanges.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


}





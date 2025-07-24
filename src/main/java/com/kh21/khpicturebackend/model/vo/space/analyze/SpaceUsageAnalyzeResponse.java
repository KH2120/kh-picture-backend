package com.kh21.khpicturebackend.model.vo.space.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUsageAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 1995289412454432550L;
    private Long usedSize;
    private Long maxSize;
    private Double sizeUsageRatio;
    private Long usedCount;
    private Long maxCount;
    private Double countUsageRatio;
}

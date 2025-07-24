package com.kh21.khpicturebackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = -8659606465591682587L;
    private String category;
    private Long count;
    private Long totalSize;
}

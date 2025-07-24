package com.kh21.khpicturebackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagsAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 3598809860262913827L;
    private String tag;
    private Long count;
}

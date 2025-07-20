package com.kh21.khpicturebackend.model.dto.picture;

import lombok.Data;

@Data
public class PictureUploadByBatchRequest {
    private String searchText;
    private Integer count = 10;
    /**
     * 名称前缀
     */
    private String namePrefix;

}

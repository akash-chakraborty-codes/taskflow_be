package com.jbs.tfv3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long authorId;
    private String authorEmail;

    private List<String> files;
}

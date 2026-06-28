package com.kahanchale.splitexpense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long createdBy;

    @NotEmpty
    private List<Long> memberIds;
}

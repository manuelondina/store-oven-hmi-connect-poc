package com.supermarket.ovenupdate.poc.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UnzippedFiles {
    private String name;
    private String size;
    private boolean uploaded;
    private String filePath;

    public boolean isUploaded() {
        return uploaded;
    }
}

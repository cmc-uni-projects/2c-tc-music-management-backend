package com.example.CMCmp3.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFileUploadService {

    String uploadFile(MultipartFile file) throws IOException;

}

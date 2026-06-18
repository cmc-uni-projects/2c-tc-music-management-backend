package com.example.CMCmp3.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Acl;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageService implements IFileUploadService {

    @Value("${firebase.bucket.name}")
    private String bucketName;

    /**
     * Tải file (ảnh/nhạc) lên Firebase Storage.
     * @param file File MultipartFile từ request
     * @return URL công khai (public URL) của file
     */
    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        Bucket bucket = StorageClient.getInstance().bucket();

        // 1. Tạo tên file độc nhất (để tránh trùng lặp)
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null) {
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFileName.substring(dotIndex);
            }
        }
        String newFileName = UUID.randomUUID().toString() + extension;

        // 2. Upload file
        Blob blob = bucket.create(newFileName, file.getBytes(), file.getContentType());

        // 3. (Quan trọng) Set file ở chế độ công khai (public read)
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        // 4. Trả về URL công khai
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, newFileName);
    }
}

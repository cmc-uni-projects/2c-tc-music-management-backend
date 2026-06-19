package com.example.CMCmp3.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.CMCmp3.service.IFileUploadService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Qualifier(value = "cloudinary-file-upload-service")
public class CloudinaryFileUploadService implements IFileUploadService {

    private final Cloudinary cloudinary;

    public CloudinaryFileUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        // Detect resource type automatically (image, video, raw)
        /**
         * "resource_type": "auto" → allows:
         * ✅ Images
         * ✅ Videos
         * ✅ Audio (music)
         * ✅ Other files
         */
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "auto" // IMPORTANT: supports image, video, audio
                )
        );

        // Return secure URL
        return uploadResult.get("secure_url").toString();
    }
}

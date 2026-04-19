package com.jbs.tfv3.service;


import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileManagerService {

    private final Path storageLocation;

    public FileManagerService(@Value("${file.upload-dir}") String storage) {
        this.storageLocation = Paths.get(storage).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String saveFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalName = Paths.get(file.getOriginalFilename())
                                   .getFileName()
                                   .toString();

        // prevent overwrite
        String storedFileName =
                UUID.randomUUID() + "_" + originalName;

        Path targetLocation =
                this.storageLocation.resolve(storedFileName).normalize();

        if (!targetLocation.startsWith(storageLocation)) {
            throw new SecurityException("Invalid file path");
        }

        try {
            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + storedFileName, e);
        }

        return storedFileName; // 👈 IMPORTANT
    }
}

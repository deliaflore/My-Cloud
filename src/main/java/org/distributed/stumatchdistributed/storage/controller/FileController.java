package org.distributed.stumatchdistributed.storage.controller;

import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.service.UserContextService;
import org.distributed.stumatchdistributed.storage.dto.FileMetadataDTO;
import org.distributed.stumatchdistributed.storage.entity.FileMetadata;
import org.distributed.stumatchdistributed.storage.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;
    private final UserContextService userContextService;

    public FileController(FileService fileService,
                          UserContextService userContextService) {
        this.fileService = fileService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public List<FileMetadataDTO> listFiles() {
        UserAccount user = userContextService.getCurrentUser();
        return fileService.listFiles(user).stream()
                .map(FileMetadataDTO::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/upload")
    public FileMetadataDTO upload(@RequestParam("file") MultipartFile file) {
        UserAccount user = userContextService.getCurrentUser();
        FileMetadata metadata = fileService.upload(user, file);
        return FileMetadataDTO.from(metadata);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID fileId) {
        UserAccount user = userContextService.getCurrentUser();
        return fileService.download(user, fileId);
    }

    @DeleteMapping("/{fileId}")
    public Map<String, Object> delete(@PathVariable UUID fileId) {
        UserAccount user = userContextService.getCurrentUser();
        fileService.delete(user, fileId);
        return Map.of("success", true, "message", "File deleted");
    }
}


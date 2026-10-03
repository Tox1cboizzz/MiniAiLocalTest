package org.example.ailocal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjectReaderService {

    @Value("${project.path}")
    private String projectPath;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".java",
            ".sql",
            ".xml",
            ".properties",
            ".yml",
            ".yaml",
            ".md"
    );

    public String readProject() throws IOException {

        Path root = Paths.get(projectPath);

        if (!Files.exists(root)) {
            throw new IOException("Không tìm thấy project: " + projectPath);
        }

        try (Stream<Path> paths = Files.walk(root)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isAllowedFile)
                    .filter(path -> !isIgnored(path))
                    .map(this::readFile)
                    .collect(Collectors.joining("\n\n"));
        }
    }

    private boolean isAllowedFile(Path path) {

        String fileName = path.getFileName()
                .toString()
                .toLowerCase();

        return ALLOWED_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
    }

    private boolean isIgnored(Path path) {

        String pathString = path.toString();

        return pathString.contains("\\target\\")
                || pathString.contains("\\.git\\")
                || pathString.contains("\\.idea\\");
    }

    private String readFile(Path path) {

        try {

            String content = Files.readString(path);

            return """
                    
                    ================================
                    FILE: %s
                    ================================
                    
                    %s
                    """.formatted(
                    path,
                    content
            );

        } catch (IOException e) {

            return """
                    ================================
                    FILE: %s
                    ================================
                    [Không đọc được file]
                    """.formatted(path);
        }
    }
}
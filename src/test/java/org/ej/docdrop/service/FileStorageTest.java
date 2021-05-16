package org.ej.docdrop.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FileStorageTest {

    @TempDir
    static Path storagePath;
    private final FileStorage storage = new FileStorage(storagePath.toString());


    @ParameterizedTest(name = "{index} {2}")
    @MethodSource("functionsProvider")
    @DisplayName("Put moves the original file")
    void moveOriginalFile(Function<FileStorage, BiConsumer<UUID, Path>> put, Function<FileStorage, Function<UUID, Path>> get, String type) throws IOException {
        // Given
        Path testFile = storagePath.resolve("my-test-file");
        Files.writeString(testFile, "test contents ...");

        // When
        put.apply(storage).accept(UUID.randomUUID(), testFile);

        // Then
        assertThat(Files.exists(testFile)).isFalse();
    }

    @ParameterizedTest(name = "{index} {2}")
    @MethodSource("functionsProvider")
    @DisplayName("Put throws exception when moving file fails")
    void throwException(Function<FileStorage, BiConsumer<UUID, Path>> put, Function<FileStorage, Function<UUID, Path>> get, String type) {
        // Given
        Path nonExistingFile = storagePath.resolve("does-not-exists");

        // When, Then
        assertThatThrownBy(() -> put.apply(storage).accept(UUID.randomUUID(), nonExistingFile)).isInstanceOf(StorageException.class);
    }

    @ParameterizedTest(name = "{index} {2}")
    @MethodSource("functionsProvider")
    @DisplayName("get returns original file")
    void returnOriginalFile(Function<FileStorage, BiConsumer<UUID, Path>> put, Function<FileStorage, Function<UUID, Path>> get, String type) throws IOException {
        // Given
        Path testFile = storagePath.resolve("to-fetch");
        Files.writeString(testFile, "fetched ...");
        UUID fileId = UUID.randomUUID();

        // When
        put.apply(storage).accept(fileId, testFile);
        Path returnedFile = get.apply(storage).apply(fileId);

        // Then
        assertThat(Files.readString(returnedFile)).isEqualTo("fetched ...");
    }

    @ParameterizedTest(name = "{index} {2}")
    @MethodSource("functionsProvider")
    @DisplayName("get throws exception when file cannot be found")
    void filePathThrowsError(Function<FileStorage, BiConsumer<UUID, Path>> put, Function<FileStorage, Function<UUID, Path>> get, String type) {
        // Given
        UUID nonExistingFileId = UUID.randomUUID();

        // When, Then
        assertThatThrownBy(() -> get.apply(storage).apply(nonExistingFileId)).isInstanceOf(RuntimeException.class);
    }

    static Stream<Arguments> functionsProvider() {
        Function<FileStorage, BiConsumer<UUID, Path>> putFile = (FileStorage storage) -> storage::putFile;
        Function<FileStorage, BiConsumer<UUID, Path>> putThumbnailFile = (FileStorage storage) -> storage::putThumbnail;

        Function<FileStorage, Function<UUID, Path>> file = (FileStorage storage) -> storage::getFilePath;
        Function<FileStorage, Function<UUID, Path>> thumbnail = (FileStorage storage) -> storage::getThumbnailPath;

        return Stream.of(arguments(putFile, file, "file"), arguments(putThumbnailFile, thumbnail, "thumbnail"));
    }
}

package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.*;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.*;
import com.mpatric.mp3agic.Mp3File;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SongService {

    private static final Logger logger = LoggerFactory.getLogger(SongService.class);

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final SongLikeRepository songLikeRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final NotificationService notificationService;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongListenLogRepository songListenLogRepository;
    private final AlbumSongRepository albumSongRepository;

    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found in database"));
    }

    private Optional<User> getOptionalAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername());
        }
        return Optional.empty();
    }

    // 1. HELPERS(Logic phụ trợ)

    private Set<Artist> fetchArtistsByIds(Set<Long> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(artistRepository.findAllById(artistIds));
    }

    private Set<Tag> fetchTagsByIds(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(tagRepository.findAllById(tagIds));
    }

    private int calculateDuration(String filePath) {
        File tempFile = null;
        try {
            File fileToRead;
            // Nếu là URL (Firebase/Cloud), phải tải về file tạm mới đọc được metadata
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                tempFile = File.createTempFile("song_duration_calc_", ".mp3");
                try (InputStream in = new URL(filePath).openStream();
                     FileOutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                fileToRead = tempFile;
            } else {
                fileToRead = new File(filePath);
            }

            if (fileToRead.exists()) {
                Mp3File mp3File = new Mp3File(fileToRead);
                return (int) mp3File.getLengthInSeconds();
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Warning: Không thể tính duration cho file: " + filePath + ". Lỗi: " + e.getMessage());
            return 0;
        } finally {
            // Quan trọng: Xóa file tạm sau khi dùng xong
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // 2. HELPERS: MAPPERS (Chuyển đổi dữ liệu)

    public SongDTO toDTO(Song s) {
        SongDTO dto = new SongDTO();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());
        dto.setDuration(s.getDuration());
        dto.setFilePath(s.getFilePath());
        dto.setImageUrl(s.getImageUrl());
        dto.setListenCount(s.getListenCount());
        dto.setLikeCount(s.getLikeCount());
        dto.setDescription(s.getDescription());
        dto.setCreatedAt(s.getCreatedAt());

        if (s.getLyrics() != null) {
            List<LyricLineDTO> lyricLineDTOS = s.getLyrics().stream()
                    .map(l -> new LyricLineDTO(l.getTime(), l.getText()))
                    .collect(Collectors.toList());
            dto.setLyrics(lyricLineDTOS);
        } else {
            dto.setLyrics(Collections.emptyList());
        }

        if (s.getArtists() != null && !s.getArtists().isEmpty()) {
            dto.setArtistName(s.getArtists().stream()
                    .map(Artist::getName)
                    .collect(Collectors.joining(", ")));

            Set<ArtistDTO> artistDTOS = s.getArtists().stream()
                    .map(a -> {
                        ArtistDTO artistDto = new ArtistDTO();
                        artistDto.setId(a.getId());
                        artistDto.setName(a.getName());
                        artistDto.setImageUrl(a.getImageUrl());
                        return artistDto;
                    })
                    .collect(Collectors.toSet());
            dto.setArtists(artistDTOS);
        } else {
            dto.setArtists(Collections.emptySet());
            dto.setArtistName("");
        }

        if (s.getTags() != null) {
            Set<TagDTO> tagDTOS = s.getTags().stream()
                    .map(t -> {
                        TagDTO tDto = new TagDTO();
                        tDto.setId(t.getId());
                        tDto.setName(t.getName());
                        return tDto;
                    })
                    .collect(Collectors.toSet());
            dto.setTags(tagDTOS);
        } else {
            dto.setTags(Collections.emptySet());
        }

        if (s.getUploader() != null) {
            User uploader = s.getUploader();
            dto.setUploader(new SongDTO.UploaderDTO(uploader.getId(), uploader.getDisplayName()));
        }

        return dto;
    }

    private Song convertToEntity(CreateSongDTO dto) {
        Song song = new Song();
        song.setTitle(dto.getTitle());
        song.setFilePath(dto.getFilePath());
        song.setImageUrl(dto.getImageUrl());
        song.setDescription(dto.getDescription());
        song.setListenCount(0L);
        song.setLikeCount(0L);
        song.setArtists(fetchArtistsByIds(dto.getArtistIds()));
        song.setTags(fetchTagsByIds(dto.getTagIds()));
        song.setDuration(calculateDuration(dto.getFilePath()));

        return song;
    }

    private SongSearchResponseDTO toSongSearchResponseDTO(Song s) {
        SongSearchResponseDTO dto = new SongSearchResponseDTO();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());

        if (s.getArtists() != null && !s.getArtists().isEmpty()) {
            dto.setArtistName(s.getArtists().stream()
                    .map(Artist::getName)
                    .collect(Collectors.joining(", ")));
        } else {
            dto.setArtistName("");
        }

        if (s.getTags() != null) {
            Set<TagDTO> tagDTOS = s.getTags().stream()
                    .map(t -> {
                        TagDTO tDto = new TagDTO();
                        tDto.setId(t.getId());
                        tDto.setName(t.getName());
                        return tDto;
                    })
                    .collect(Collectors.toSet());
            dto.setTags(tagDTOS);
        } else {
            dto.setTags(Collections.emptySet());
        }

        return dto;
    }

    // 3. READ OPERATIONS (Đọc dữ liệu)

    @Transactional(readOnly = true)
    public Page<SongDTO> getAllSongs(Pageable pageable) {
        return songRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SongDTO getById(Long id) {
        Song song = songRepository.findApprovedById(id)
                .orElseThrow(() -> new NoSuchElementException("Approved song not found: " + id));
        return toDTO(song);
    }

    @Transactional(readOnly = true)
    public SongDTO getSongByTitle(String title) {
        Song song = songRepository.findFirstByTitleContainingIgnoreCase(title)
                .orElseThrow(() -> new NoSuchElementException("Song not found with title: " + title));
        return toDTO(song);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSongResource(Long id) throws MalformedURLException {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + id));

        Resource resource = new UrlResource(song.getFilePath());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Could not read file: " + song.getFilePath());
        }

        // Sanitize title to create a valid filename
        String filename = song.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".mp3";

        Map<String, Object> songData = new HashMap<>();
        songData.put("resource", resource);
        songData.put("filename", filename);

        return songData;
    }

    // --- TOP CHARTS (Sử dụng logic Repository trả về List Entity) ---

    @Transactional(readOnly = true)
    public List<SongDTO> getTopSongs(int limit) {
        return songRepository.findTopByListenCount(PageRequest.of(0, limit))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getTopNewReleases(int limit) {
        return songRepository.findTopByCreatedAt(PageRequest.of(0, limit))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getTopMostLiked(int limit) {
        return songRepository.findTopByLikeCount(PageRequest.of(0, limit))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getUploadedSongsForCurrentUser(String query) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info(">>> [getUploadedSongs] Current User ID: {}, Email: {}", currentUser.getId(), currentUser.getEmail());
        logger.info(">>> [getUploadedSongs] Search Query: '{}'", query);

        List<Song> songs;

        if (query != null && !query.trim().isEmpty()) {
            songs = songRepository.findByUploaderIdAndTitleContaining(currentUser.getId(), query);
            logger.info(">>> [getUploadedSongs] Found {} songs with query.", songs.size());
        } else {
            songs = songRepository.findByUploader(currentUser);
            logger.info(">>> [getUploadedSongs] Found {} songs without query.", songs.size());
        }

        return songs.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getFavoriteSongsForCurrentUser() {
        // 1. Get current user
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found in database"));

        // 2. Get liked songs using the new repository method
        List<Song> likedSongs = songRepository.findLikedSongsByUserId(currentUser.getId());

        // 3. Map to DTOs and return
        return likedSongs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getSongsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        List<Song> songs = songRepository.findByUploader(user);
        return songs.stream()
                .filter(song -> song.getStatus() == SongStatus.APPROVED)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> findSongsByArtistName(String artistName) {
        List<Song> songs = songRepository.findAllByArtistsNameContainingIgnoreCase(artistName);
        return songs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getSimilarSongs(Long songId, int limit) {
        Song originalSong = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + songId));

        Set<Long> artistIds = originalSong.getArtists().stream()
                .map(Artist::getId)
                .collect(Collectors.toSet());

        Set<Long> tagIds = originalSong.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        if (artistIds.isEmpty() && tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Song> similarSongs = songRepository.findSimilarSongs(songId, artistIds, tagIds, PageRequest.of(0, limit));

        return similarSongs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getSimilarSongsByTitle(String title, int limit) {
        return songRepository.findFirstByTitleContainingIgnoreCase(title)
                .map(song -> getSimilarSongs(song.getId(), limit))
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public boolean isUploader(Long songId) {
        User currentUser = getCurrentAuthenticatedUser();
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + songId));
        return song.getUploader() != null && song.getUploader().getId().equals(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> findSongsByMood(String mood, int limit) {
        List<Song> songs = songRepository.findAllByTagsNameContainingIgnoreCase(mood);
        Collections.shuffle(songs);
        List<Song> limitedSongs = songs.stream().limit(limit).collect(Collectors.toList());
        return limitedSongs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getRecommendationsForUser(Long userId, int limit) {
        // 1. Get user's listening history
        List<Long> listenedSongIds = songListenLogRepository.findListenedSongIdsByUserId(userId);

        // 2. Get user's favorite artists and tags
        List<Long> topArtistIds = songListenLogRepository.findTopArtistIdsForUser(userId, PageRequest.of(0, 5));
        List<Long> topTagIds = songListenLogRepository.findTopTagIdsForUser(userId, PageRequest.of(0, 5));

        if (topArtistIds.isEmpty() && topTagIds.isEmpty()) {
            return getTopNewReleases(limit); // Fallback to new releases if no history
        }

        // 3. Find recommended songs
        List<Song> recommendedSongs = songRepository.findRecommendedSongs(
                topArtistIds,
                topTagIds,
                listenedSongIds.isEmpty() ? List.of(-1L) : listenedSongIds, // Ensure not empty list for query
                PageRequest.of(0, limit)
        );

        // 4. If not enough recommendations, fill with new releases
        if (recommendedSongs.size() < limit) {
            List<SongDTO> newReleases = getTopNewReleases(limit);
            List<SongDTO> currentRecommendations = recommendedSongs.stream().map(this::toDTO).collect(Collectors.toList());
            newReleases.removeAll(currentRecommendations);
            currentRecommendations.addAll(newReleases.subList(0, Math.min(newReleases.size(), limit - currentRecommendations.size())));
            return currentRecommendations;
        }

        return recommendedSongs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 4. WRITE OPERATIONS (Ghi dữ liệu)

    @Transactional
    public SongDTO createSong(CreateSongDTO createSongDTO) {
        Song song = convertToEntity(createSongDTO);
        Song savedSong = songRepository.save(song);
        return toDTO(savedSong);
    }

    @Transactional
    public SongDTO createSongWithUpload(String title, String description, Set<Long> artistIds, Set<Long> tagIds, MultipartFile songFile, MultipartFile imageFile) {
        try {
            // 0. Get current user
            User currentUser = getCurrentAuthenticatedUser();

            // 1. Store files (SỬ DỤNG FIREBASE)
            String songFilePath = firebaseStorageService.uploadFile(songFile); // <-- SỬA LẠI
            String imageFilePath = firebaseStorageService.uploadFile(imageFile); // <-- SỬA LẠI

            // 2. Create new Song entity
            Song song = new Song();
            song.setTitle(title);
            song.setDescription(description);
            song.setFilePath(songFilePath); // <-- URL từ Firebase
            song.setImageUrl(imageFilePath); // <-- URL từ Firebase
            song.setUploader(currentUser); // Set the uploader

            // 3. Set default values and relationships
            song.setListenCount(0L);
            song.setLikeCount(0L);
            song.setArtists(fetchArtistsByIds(artistIds));
            song.setTags(fetchTagsByIds(tagIds));

            // Set status based on user role
            if (currentUser.getRole() == Role.ADMIN) {
                song.setStatus(SongStatus.APPROVED);
            } else {
                song.setStatus(SongStatus.PENDING);
            }

            // Calculate duration (HÀM calculateDuration SẼ TỰ XỬ LÝ URL)
            // String fullSongPath = Paths.get("uploads").resolve(songFilePath)... // <-- XÓA DÒNG CŨ
            song.setDuration(calculateDuration(songFilePath)); // <-- TRUYỀN THẲNG URL VÀO

            // Save and return DTO
            Song savedSong = songRepository.save(song);
            return toDTO(savedSong);

        } catch (IOException ex) {
            // Ném ra lỗi nếu Firebase upload thất bại
            throw new RuntimeException("Không thể upload file bài hát. Vui lòng thử lại!", ex);
        }
    }

    @Transactional
    public SongDTO updateUploadedSong(Long id,
                                      String title,
                                      String description,
                                      Set<Long> artistIds,
                                      Set<Long> tagIds,
                                      MultipartFile newSongFile,
                                      MultipartFile newImageFile,
                                      String status) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + id));

        User currentUser = getCurrentAuthenticatedUser();
        boolean isOwner = song.getUploader() != null && Objects.equals(song.getUploader().getId(), currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài hát này");
        }

        if (title != null && !title.isBlank()) {
            song.setTitle(title.trim());
        }
        if (description != null) {
            song.setDescription(description);
        }
        if (artistIds != null) {
            song.setArtists(fetchArtistsByIds(artistIds));
        }
        if (tagIds != null) {
            song.setTags(fetchTagsByIds(tagIds));
        }
        // New logic to update status
        if (status != null && !status.isBlank()) {
            try {
                SongStatus newStatus = SongStatus.valueOf(status.toUpperCase());
                song.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                // Optional: handle invalid status string, e.g., throw an exception or log a warning
                // For now, we'll just ignore invalid status values
            }
        }

        try {
            if (newSongFile != null && !newSongFile.isEmpty()) {
                String songFilePath = firebaseStorageService.uploadFile(newSongFile);
                song.setFilePath(songFilePath);
                song.setDuration(calculateDuration(songFilePath));
            }

            if (newImageFile != null && !newImageFile.isEmpty()) {
                String imageFilePath = firebaseStorageService.uploadFile(newImageFile);
                song.setImageUrl(imageFilePath);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Không thể cập nhật file bài hát. Vui lòng thử lại!", ex);
        }

        Song updatedSong = songRepository.save(song);
        return toDTO(updatedSong);
    }

    @Transactional
    public SongDTO updateSong(Long id, CreateSongDTO updateDTO) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + id));

        // 1. Cập nhật thông tin cơ bản (Partial Update - check null)
        if (updateDTO.getTitle() != null) song.setTitle(updateDTO.getTitle());
        if (updateDTO.getDescription() != null) song.setDescription(updateDTO.getDescription());
        if (updateDTO.getImageUrl() != null) song.setImageUrl(updateDTO.getImageUrl());

        // 2. Cập nhật File & Duration (Nếu file thay đổi thì tính lại duration)
        if (updateDTO.getFilePath() != null && !updateDTO.getFilePath().equals(song.getFilePath())) {
            song.setFilePath(updateDTO.getFilePath());
            song.setDuration(calculateDuration(updateDTO.getFilePath()));
        }

        // 3. Cập nhật Quan hệ (Sử dụng Helper)
        if (updateDTO.getArtistIds() != null) {
            song.setArtists(fetchArtistsByIds(updateDTO.getArtistIds()));
        }
        if (updateDTO.getTagIds() != null) {
            song.setTags(fetchTagsByIds(updateDTO.getTagIds()));
        }

        Song updatedSong = songRepository.save(song);
        return toDTO(updatedSong);
    }

    @Transactional
    public SongDTO addLyricsToSong(Long songId, AddLyricsDTO addLyricsDTO) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + songId));

        song.getLyrics().clear();

        if (addLyricsDTO.getLyrics() != null) {
            List<LyricLine> newLyrics = addLyricsDTO.getLyrics().stream()
                    .map(dto -> new LyricLine(dto.getTime(), dto.getText()))
                    .collect(Collectors.toList());
            song.getLyrics().addAll(newLyrics);
        }

        Song updatedSong = songRepository.save(song);
        return toDTO(updatedSong);
    }

        @Transactional
        public void deleteSong(Long id) {
            Song song = songRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Song not found: " + id));

            albumSongRepository.deleteBySongId(id);
            playlistSongRepository.deleteBySongId(id);
            songRepository.delete(song);
        }

        // 5. LIKE/UNLIKE OPERATIONS

        @Transactional

        public void likeSong(Long songId) {

            // Get current user and song
            String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));

            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new NoSuchElementException("Song not found: " + songId));

            // Check if already liked
            SongLikeId likeId = new SongLikeId(currentUser.getId(), song.getId());

            if (songLikeRepository.existsById(likeId)) {
                return;
            }

            // Create new like and update count

            SongLike songLike = new SongLike(currentUser, song);
            songLikeRepository.save(songLike);
            song.setLikeCount(song.getLikeCount() + 1);
            songRepository.save(song);

            if (song.getUploader() != null) {
                notificationService.createAndSendNotification(
                        currentUser,                   // Sender
                        song.getUploader(),            // Recipient
                        NotificationType.LIKE_SONG,    // Type
                        currentUser.getDisplayName() + " đã thích bài hát: " + song.getTitle(), // Message
                        song.getId()                   // Reference ID
                );
            }
        }

        @Transactional
        public void unlikeSong(Long songId) {

            String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new NoSuchElementException("Song not found: " + songId));
            // Find the like
            SongLikeId likeId = new SongLikeId(currentUser.getId(), song.getId());

            SongLike songLike = songLikeRepository.findById(likeId)
                    .orElse(null); // Find the like to delete

            // If like exists, delete it and update count

            if (songLike != null) {
                songLikeRepository.delete(songLike);
                        song.setLikeCount(Math.max(0, song.getLikeCount() - 1)); // Avoid negative counts
                        songRepository.save(song);
                    }
        }

        @Transactional
        public void incrementListenCount(Long songId) {
            Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + songId));

            // 1. Tăng tổng lượt nghe trên bài hát
            song.setListenCount(song.getListenCount() + 1);
            songRepository.save(song);

            // 2. Ghi log chi tiết lượt nghe này
            User user = getOptionalAuthenticatedUser().orElse(null);
            SongListenLog logEntry = new SongListenLog(song, user, java.time.LocalDateTime.now());
            songListenLogRepository.save(logEntry);
        }

        @Transactional(readOnly = true)
        public Page<SongDTO> getSongsByStatus(SongStatus status, Pageable pageable) {

        return songRepository.findAllByStatus(status, pageable).map(this::toDTO);
        }

        @Transactional
        public SongDTO changeSongStatus(Long songId, SongStatus newStatus) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + songId));

        song.setStatus(newStatus);
        Song updatedSong = songRepository.save(song);

        return toDTO(updatedSong);
        }

    @Transactional(readOnly = true)
    public List<SongSearchResponseDTO> searchByLyric(String query) {
        List<Song> songs = songRepository.searchByLyric(query);
        return songs.stream()
                .map(this::toSongSearchResponseDTO)
                .collect(Collectors.toList());
    }
}

    
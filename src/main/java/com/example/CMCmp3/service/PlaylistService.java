package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.ArtistDTO;
import com.example.CMCmp3.dto.CreatePlaylistDTO;
import com.example.CMCmp3.dto.PlaylistDTO;
import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.dto.UpdatePlaylistDTO; // Import UpdatePlaylistDTO
import com.example.CMCmp3.dto.UpdatePlaylistSongsDTO;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.PlaylistRepository;
import com.example.CMCmp3.repository.UserRepository;
import com.example.CMCmp3.repository.SongRepository;
import com.example.CMCmp3.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Optional;
import com.example.CMCmp3.repository.PlaylistLikeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class PlaylistService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongService songService;
    private final SongRepository songRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final ArtistRepository artistRepository;
    private final PlaylistLikeRepository playlistLikeRepository;
    private final NotificationService notificationService;

    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails)principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found in database"));
    }

    @Transactional
    public void toggleLikePlaylist(Long playlistId) {
        User currentUser = getCurrentAuthenticatedUser();
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + playlistId));

        PlaylistLikeId likeId = new PlaylistLikeId(currentUser.getId(), playlist.getId());
        Optional<PlaylistLike> existingLike = playlistLikeRepository.findById(likeId);

        if (existingLike.isPresent()) {
            // Unlike
            playlistLikeRepository.delete(existingLike.get());
            playlist.setLikeCount(Math.max(0, playlist.getLikeCount() - 1));
        } else {
            // Like
            PlaylistLike newLike = new PlaylistLike(likeId, currentUser, playlist, LocalDateTime.now());
            playlistLikeRepository.save(newLike);
            playlist.setLikeCount(playlist.getLikeCount() + 1);

            if (playlist.getOwner() != null) {
                notificationService.createAndSendNotification(
                        currentUser,                   // Sender
                        playlist.getOwner(),            // Recipient
                        NotificationType.LIKE_PLAYLIST,    // Type
                        currentUser.getDisplayName() + " đã thích playlist: " + playlist.getTitle(), // Message
                        playlist.getId()
                );
            }
        }
        playlistRepository.save(playlist);
    }
    // --- MAPPING ---
    private PlaylistDTO toDTO(Playlist p) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setImageUrl(p.getImageUrl());
        dto.setPlayCount(p.getPlayCount());
        dto.setLikeCount(p.getLikeCount());
        dto.setCreatedAt(p.getCreatedAt());

        // Tính số bài hát (thông qua bảng trung gian playlistSongs)
        if (p.getPlaylistSongs() != null) {
            dto.setSongCount(p.getPlaylistSongs().size());
            // Nếu muốn trả về list ID bài hát:
             dto.setSongs(p.getPlaylistSongs().stream().map(ps -> ps.getSong().getId()).collect(Collectors.toList()));
        } else {
            dto.setSongCount(0);
        }

        // Lấy thông tin chủ sở hữu (User)
        if (p.getOwner() != null) {
            dto.setOwner(new PlaylistDTO.OwnerDTO(p.getOwner().getId(), p.getOwner().getDisplayName()));
        }
        dto.setPrivacy(p.getPrivacy().name()); // Map privacy enum to String

        // Map associated artists
        if (p.getArtists() != null && !p.getArtists().isEmpty()) {
            dto.setArtists(p.getArtists().stream().map(this::toArtistDTO).collect(Collectors.toList()));
        } else {
            dto.setArtists(List.of()); // Return empty list if no artists
        }

        try {
            User currentUser = getCurrentAuthenticatedUser();
            boolean liked = p.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
            dto.setLikedByCurrentUser(liked);
        } catch (RuntimeException e) {
            dto.setLikedByCurrentUser(false);
        }

        return dto;
    }

    // Helper method to convert Artist entity to ArtistDTO
    private ArtistDTO toArtistDTO(Artist artist) {
        ArtistDTO dto = new ArtistDTO();
        dto.setId(artist.getId());
        dto.setName(artist.getName());
        dto.setImageUrl(artist.getImageUrl());
        return dto;
    }

    // --- LOGIC ---

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getAll() {
        User currentUser = null;
        try {
            currentUser = getCurrentAuthenticatedUser();
        } catch (RuntimeException e) {
            // User is not authenticated, currentUser remains null
        }

        List<Playlist> playlists = playlistRepository.findAll();
        final User finalCurrentUser = currentUser;

        return playlists.stream()
                .filter(p -> {
                    if (p.getPrivacy() == PlaylistPrivacy.PUBLIC) {
                        return true;
                    }
                    if (finalCurrentUser != null) {
                        // Admins can see all playlists
                        if (finalCurrentUser.getRole() == Role.ADMIN) {
                            return true;
                        }
                        // Users can see their own private playlists
                        if (p.getOwner() != null && p.getOwner().getId().equals(finalCurrentUser.getId())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlaylistDTO getById(Long id) {
        Playlist p = playlistRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + id));

        // Privacy Check
        if (p.getPrivacy() == PlaylistPrivacy.PRIVATE) {
            logger.debug("Playlist with ID {} is private. Checking authorization.", id);
            try {
                User currentUser = getCurrentAuthenticatedUser();
                boolean isOwner = p.getOwner() != null && p.getOwner().getId().equals(currentUser.getId());
                boolean isAdmin = currentUser.getRole() == Role.ADMIN;

                logger.debug("Current user ID: {}, Role: {}. Playlist owner ID: {}. Is Owner? {}, Is Admin? {}",
                        currentUser.getId(), currentUser.getRole(), p.getOwner() != null ? p.getOwner().getId() : "null", isOwner, isAdmin);

                if (!isOwner && !isAdmin) {
                    logger.warn("Access denied for user {} to private playlist {}", currentUser.getEmail(), id);
                    throw new AccessDeniedException("You are not authorized to access this playlist.");
                }
                logger.debug("Access granted for user {} to private playlist {}", currentUser.getEmail(), id);
            } catch (RuntimeException e) {
                 // This catches the case where the user is not authenticated (from getCurrentAuthenticatedUser)
                logger.warn("Access denied for unauthenticated user to private playlist {}", id);
                throw new AccessDeniedException("You must be logged in to access this private playlist.");
            }
        }

        return toDTO(p);
    }

    @Transactional(readOnly = true)
    public List<PlaylistDTO> findMyPlaylists() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return playlistRepository.findByOwner(currentUser).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getSongsByPlaylistId(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + playlistId));

        // Authorization check for private playlists
        if (playlist.getPrivacy() == PlaylistPrivacy.PRIVATE) {
            try {
                User currentUser = getCurrentAuthenticatedUser();
                if (!playlist.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
                    throw new AccessDeniedException("You are not authorized to access songs in this playlist.");
                }
            } catch (RuntimeException e) {
                throw new AccessDeniedException("You must be logged in to access songs in this private playlist.");
            }
        }

        return playlist.getPlaylistSongs().stream()
                .map(playlistSong -> songService.toDTO(playlistSong.getSong()))
                .collect(Collectors.toList());
    }

    @Transactional
    public PlaylistDTO updatePlaylist(Long playlistId, UpdatePlaylistDTO dto) {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + playlistId));

        // Authorization check
        if (!playlist.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You are not authorized to modify this playlist.");
        }

        // Handle image file update
        MultipartFile imageFile = dto.getImageFile();
        if (imageFile != null) {
            if (!imageFile.isEmpty()) {
                try {
                    String newImageUrl = firebaseStorageService.uploadFile(imageFile);
                    playlist.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Could not upload image for playlist: " + e.getMessage());
                }
            } else {
                // If an empty file is sent, it means the user wants to remove the image
                playlist.setImageUrl(null);
            }
        }

        // Update basic fields
        Optional.ofNullable(dto.getName())
                .filter(name -> !name.trim().isEmpty())
                .ifPresent(playlist::setTitle);

        Optional.ofNullable(dto.getDescription())
                .filter(description -> !description.trim().isEmpty())
                .ifPresent(playlist::setDescription);

        // Update privacy only if provided in DTO
        if (dto.getPrivacy() != null && !dto.getPrivacy().trim().isEmpty()) {
            playlist.setPrivacy(PlaylistPrivacy.valueOf(dto.getPrivacy().toUpperCase()));
        }

        // Handle artistIds update
        if (dto.getArtistIds() != null && !dto.getArtistIds().isEmpty()) {
            Set<Long> artistIds = Arrays.stream(dto.getArtistIds().split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .map(Long::parseLong)
                                        .collect(Collectors.toSet());

            List<Artist> artists = artistRepository.findAllById(artistIds);

            if (artists.size() != artistIds.size()) {
                // Some artist IDs were not found
                Set<Long> foundArtistIds = artists.stream().map(Artist::getId).collect(Collectors.toSet());
                artistIds.removeAll(foundArtistIds);
                throw new NoSuchElementException("Artists not found with IDs: " + artistIds);
            }
            playlist.setArtists(new HashSet<>(artists));
        } else {
            // If artistIds is null or empty, clear existing artists
            playlist.setArtists(new HashSet<>());
        }


        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return toDTO(updatedPlaylist);
    }

    @Transactional
    public List<SongDTO> updateSongsInPlaylist(Long playlistId, UpdatePlaylistSongsDTO dto) {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found with ID: " + playlistId));

        // Authorization check
        if (!playlist.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You are not authorized to modify this playlist.");
        }

        Set<PlaylistSong> currentPlaylistSongs = playlist.getPlaylistSongs();

        // Handle additions
        if (dto.getAdd() != null && !dto.getAdd().isEmpty()) {
            for (Long songId : dto.getAdd()) {
                Song songToAdd = songRepository.findById(songId)
                        .orElseThrow(() -> new NoSuchElementException("Song not found with ID: " + songId));

                // Check for duplicates
                boolean alreadyExists = currentPlaylistSongs.stream()
                        .anyMatch(ps -> ps.getSong().getId().equals(songId));

                if (!alreadyExists) {
                    PlaylistSong newPlaylistSong = PlaylistSong.builder()
                            .id(new PlaylistSongId(playlistId, songId))
                            .playlist(playlist)
                            .song(songToAdd)
                            .order(currentPlaylistSongs.size() + 1) // Assign order
                            .build();
                    currentPlaylistSongs.add(newPlaylistSong);
                }
            }
        }

        // Handle removals
        if (dto.getRemove() != null && !dto.getRemove().isEmpty()) {
            currentPlaylistSongs.removeIf(ps -> dto.getRemove().contains(ps.getSong().getId()));
        }

        playlist.setPlaylistSongs(currentPlaylistSongs); // Update the set
        playlistRepository.save(playlist); // Save changes to the playlist and its songs

        return getSongsByPlaylistId(playlistId); // Return the updated list of songs
    }

    // Lấy Top Playlists (Tương tự như SongService)
    @Transactional(readOnly = true)
    public List<PlaylistDTO> getTopPlaylistsByPlayCount(int limit) {
        List<PlaylistPrivacy> privacyLevels = getPrivacyLevelsForCurrentUser();
        return playlistRepository.findTopByPlayCount(PageRequest.of(0, limit), privacyLevels)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getTopPlaylistsByLikeCount(int limit) {
        List<PlaylistPrivacy> privacyLevels = getPrivacyLevelsForCurrentUser();
        return playlistRepository.findTopByLikeCount(PageRequest.of(0, limit), privacyLevels)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getTopNewPlaylists(int limit) {
        List<PlaylistPrivacy> privacyLevels = getPrivacyLevelsForCurrentUser();
        return playlistRepository.findTopByCreatedAt(PageRequest.of(0, limit), privacyLevels)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private List<PlaylistPrivacy> getPrivacyLevelsForCurrentUser() {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser.getRole() == Role.ADMIN) {
                return Arrays.asList(PlaylistPrivacy.PUBLIC, PlaylistPrivacy.PRIVATE);
            }
        } catch (RuntimeException e) {
            // User not authenticated
        }
        return Arrays.asList(PlaylistPrivacy.PUBLIC);
    }

    @Transactional
    public PlaylistDTO createPlaylist(CreatePlaylistDTO dto) {
        // Lấy User hiện tại đang đăng nhập
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = null;
        // Upload image to Firebase if it exists
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            try {
                imageUrl = firebaseStorageService.uploadFile(dto.getImageFile());
            } catch (Exception e) {
                // Handle file upload exception
                throw new RuntimeException("Could not upload image: " + e.getMessage());
            }
        }

        Playlist p = new Playlist();
        p.setTitle(dto.getName()); // Use dto.getName()
        p.setDescription(dto.getDescription());
        p.setImageUrl(imageUrl); // Set the uploaded image URL
        p.setOwner(currentUser); // Gán chủ sở hữu
        p.setPlayCount(0L);
        p.setLikeCount(0L);
        p.setCommentCount(0L);

        // Set privacy with a default of PRIVATE if not provided
        if (dto.getPrivacy() == null || dto.getPrivacy().trim().isEmpty()) {
            p.setPrivacy(PlaylistPrivacy.PRIVATE);
        } else {
            p.setPrivacy(PlaylistPrivacy.valueOf(dto.getPrivacy().toUpperCase()));
        }

        return toDTO(playlistRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getLikedPlaylistsForCurrentUser() {
        // 1. Get current user
        User currentUser = getCurrentAuthenticatedUser();

        // 2. Get liked playlists using the new repository method
        List<Playlist> likedPlaylists = playlistRepository.findLikedPlaylistsByUserId(currentUser.getId());

        // 3. Map to DTOs and return
        return likedPlaylists.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePlaylist(Long id) {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found"));

        // Check if current user is the owner or an ADMIN
        if (!playlist.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You are not authorized to delete this playlist.");
        }

        playlistRepository.deleteById(id);
    }
}
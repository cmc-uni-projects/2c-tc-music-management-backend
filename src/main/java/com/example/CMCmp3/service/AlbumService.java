package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.*;
import com.example.CMCmp3.entity.*;
import com.example.CMCmp3.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final SongService songService;
    private final SongRepository songRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final ArtistRepository artistRepository;
    private final AlbumLikeRepository albumLikeRepository;
    private final NotificationService notificationService;

    private User getCurrentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found in database"));
    }

    // ✅ HƯỚNG A: chỉ ADMIN hoặc ARTIST có artistVerificationStatus=APPROVED mới được tạo album
    private void assertCanCreateAlbum(User user) {
        if (user == null) throw new AccessDeniedException("Chưa đăng nhập.");

        boolean isAdmin = user.getRole() == Role.ADMIN;

        boolean isVerifiedArtist =
                user.getRole() == Role.ARTIST
                        && user.getArtistVerificationStatus() == ArtistVerificationStatus.APPROVED;

        if (!isAdmin && !isVerifiedArtist) {
            throw new AccessDeniedException("Bạn cần được xác thực nghệ sĩ (hoặc là ADMIN) để tạo album.");
        }
    }

    @Transactional
    public void toggleLikeAlbum(Long albumId) {
        User currentUser = getCurrentAuthenticatedUser();
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found with ID: " + albumId));

        AlbumLikeId likeId = new AlbumLikeId(currentUser.getId(), album.getId());
        Optional<AlbumLike> existingLike = albumLikeRepository.findById(likeId);

        if (existingLike.isPresent()) {
            albumLikeRepository.delete(existingLike.get());
            album.setLikeCount(Math.max(0, album.getLikeCount() - 1));
        } else {
            AlbumLike newLike = new AlbumLike(likeId, currentUser, album, LocalDateTime.now());
            albumLikeRepository.save(newLike);
            album.setLikeCount(album.getLikeCount() + 1);

            if (album.getOwner() != null) {
                notificationService.createAndSendNotification(
                        currentUser,
                        album.getOwner(),
                        NotificationType.LIKE_ALBUM,
                        currentUser.getDisplayName() + " đã thích album: " + album.getTitle(),
                        album.getId()
                );
            }
        }
        albumRepository.save(album);
    }

    private AlbumDTO toDTO(Album a) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setImageUrl(a.getImageUrl());
        dto.setPlayCount(a.getPlayCount());
        dto.setLikeCount(a.getLikeCount());
        dto.setCreatedAt(a.getCreatedAt());

        if (a.getAlbumSongs() != null) {
            dto.setSongCount((int) a.getAlbumSongs().stream()
                    .map(AlbumSong::getSong)
                    .filter(Objects::nonNull)
                    .count());

            dto.setSongIds(a.getAlbumSongs().stream()
                    .map(AlbumSong::getSong)
                    .filter(Objects::nonNull)
                    .map(Song::getId)
                    .collect(Collectors.toList()));
        } else {
            dto.setSongCount(0);
            dto.setSongIds(List.of());
        }

        if (a.getOwner() != null) {
            User owner = a.getOwner();

            // ✅ HƯỚNG A: verified = ADMIN hoặc ARTIST có status APPROVED
            boolean isVerified = owner.getRole() == Role.ADMIN
                    || (owner.getRole() == Role.ARTIST
                    && owner.getArtistVerificationStatus() == ArtistVerificationStatus.APPROVED);

            dto.setOwner(new AlbumDTO.OwnerDTO(
                    owner.getId(),
                    owner.getDisplayName(),
                    owner.getRole().name(),
                    isVerified
            ));
        }

        if (a.getArtists() != null && !a.getArtists().isEmpty()) {
            dto.setArtists(a.getArtists().stream().map(this::toArtistDTO).collect(Collectors.toList()));
        } else {
            dto.setArtists(List.of());
        }

        try {
            User currentUser = getCurrentAuthenticatedUser();
            boolean liked = a.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
            dto.setLikedByCurrentUser(liked);
        } catch (RuntimeException e) {
            dto.setLikedByCurrentUser(false);
        }

        return dto;
    }

    private ArtistDTO toArtistDTO(Artist artist) {
        ArtistDTO dto = new ArtistDTO();
        dto.setId(artist.getId());
        dto.setName(artist.getName());
        dto.setImageUrl(artist.getImageUrl());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO> getAll() {
        List<Album> albums = albumRepository.findAll();
        return albums.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlbumDTO getById(Long id) {
        Album a = albumRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Album not found with ID: " + id));
        return toDTO(a);
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getSongsByAlbumId(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found with ID: " + albumId));

        return album.getAlbumSongs().stream()
                .map(albumSong -> songService.toDTO(albumSong.getSong()))
                .collect(Collectors.toList());
    }

    @Transactional
    public AlbumDTO updateAlbum(Long albumId, UpdateAlbumDTO dto) {
        User currentUser = getCurrentAuthenticatedUser();
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found with ID: " + albumId));

        if (!album.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You are not authorized to modify this album.");
        }

        MultipartFile imageFile = dto.getImageFile();
        if (imageFile != null) {
            if (!imageFile.isEmpty()) {
                try {
                    String newImageUrl = firebaseStorageService.uploadFile(imageFile);
                    album.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Could not upload image for album: " + e.getMessage());
                }
            } else {
                album.setImageUrl(null);
            }
        }

        Optional.ofNullable(dto.getTitle())
                .filter(title -> !title.trim().isEmpty())
                .ifPresent(album::setTitle);

        Optional.ofNullable(dto.getDescription())
                .filter(description -> !description.trim().isEmpty())
                .ifPresent(album::setDescription);

        if (dto.getArtistIds() != null && !dto.getArtistIds().isEmpty()) {
            Set<Long> artistIds = Arrays.stream(dto.getArtistIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());

            List<Artist> artists = artistRepository.findAllById(artistIds);

            if (artists.size() != artistIds.size()) {
                Set<Long> foundArtistIds = artists.stream().map(Artist::getId).collect(Collectors.toSet());
                artistIds.removeAll(foundArtistIds);
                throw new NoSuchElementException("Artists not found with IDs: " + artistIds);
            }
            album.setArtists(new HashSet<>(artists));
        } else {
            album.setArtists(new HashSet<>());
        }

        Album updatedAlbum = albumRepository.save(album);
        return toDTO(updatedAlbum);
    }

    @Transactional
    public List<SongDTO> updateSongsInAlbum(Long albumId, UpdateAlbumSongsDTO dto) {
        User currentUser = getCurrentAuthenticatedUser();
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NoSuchElementException("Album not found with ID: " + albumId));

        if (!album.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You are not authorized to modify this album.");
        }

        Set<AlbumSong> currentAlbumSongs = album.getAlbumSongs();

        if (dto.getAdd() != null && !dto.getAdd().isEmpty()) {
            for (Long songId : dto.getAdd()) {
                Song songToAdd = songRepository.findById(songId)
                        .orElseThrow(() -> new NoSuchElementException("Song not found with ID: " + songId));

                if (!currentUser.getRole().equals(Role.ADMIN)
                        && (songToAdd.getUploader() == null || !songToAdd.getUploader().getId().equals(currentUser.getId()))) {
                    throw new AccessDeniedException("You are not authorized to add song with ID " + songId + " to this album because you are not its uploader.");
                }

                boolean alreadyExists = currentAlbumSongs.stream()
                        .anyMatch(as -> as.getSong().getId().equals(songId));

                if (!alreadyExists) {
                    AlbumSong newAlbumSong = AlbumSong.builder()
                            .id(new AlbumSongId(albumId, songId))
                            .album(album)
                            .song(songToAdd)
                            .order(currentAlbumSongs.size() + 1)
                            .build();
                    currentAlbumSongs.add(newAlbumSong);
                }
            }
        }

        if (dto.getRemove() != null && !dto.getRemove().isEmpty()) {
            currentAlbumSongs.removeIf(as -> dto.getRemove().contains(as.getSong().getId()));
        }

        album.setAlbumSongs(currentAlbumSongs);
        albumRepository.save(album);

        return getSongsByAlbumId(albumId);
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO> getTopAlbumsByPlayCount(int limit) {
        return albumRepository.findTopByPlayCount(PageRequest.of(0, limit))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO> getTopAlbumsByLikeCount(int limit) {
        return albumRepository.findTopByLikeCount(PageRequest.of(0, limit))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO> getTopNewAlbums(int limit) {
        return albumRepository.findTopByCreatedAt(PageRequest.of(0, limit))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public AlbumDTO createAlbum(CreateAlbumDTO dto) {
        User currentUser = getCurrentAuthenticatedUser();
        assertCanCreateAlbum(currentUser);

        String imageUrl = null;
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            try {
                imageUrl = firebaseStorageService.uploadFile(dto.getImageFile());
            } catch (Exception e) {
                throw new RuntimeException("Could not upload image: " + e.getMessage());
            }
        }

        Album a = new Album();
        a.setTitle(dto.getTitle());
        a.setDescription(dto.getDescription());
        a.setImageUrl(imageUrl);
        a.setOwner(currentUser);
        a.setPlayCount(0L);
        a.setLikeCount(0L);
        a.setCommentCount(0L);

        return toDTO(albumRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO> getLikedAlbumsForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Album> likedAlbums = albumRepository.findLikedAlbumsByUserId(currentUser.getId());
        return likedAlbums.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO> getAlbumsForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return albumRepository.findByOwner(currentUser).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlbum(Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Album not found"));

        if (!album.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("You are not authorized to delete this album.");
        }

        albumRepository.deleteById(id);
    }
}

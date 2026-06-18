package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.*;
import com.example.CMCmp3.entity.Artist;
import com.example.CMCmp3.entity.Role;
import com.example.CMCmp3.entity.Song;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.repository.ArtistRepository;
import com.example.CMCmp3.repository.SongRepository;
import com.example.CMCmp3.repository.UserRepository; // Import UserRepository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final UserRepository userRepository; // Inject UserRepository

    @Transactional(readOnly = true)
    public List<ArtistDTO> getAllArtists() {
        return artistRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArtistDTO getArtistById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Artist not found: " + id));
        return toDTO(artist);
    }

    /**
     * Lấy danh sách bài hát của ca sĩ (Quan trọng)
     */
    @Transactional(readOnly = true)
    public List<SongDTO> getSongsByArtistId(Long id) {
        // Lưu ý: Trong SongRepository cần có method findAllByArtistsId(Long artistId)
        return songRepository.findAllByArtistsIdAndStatus(id, com.example.CMCmp3.entity.SongStatus.APPROVED)
                .stream()
                .map(this::toSongDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArtistDTO> findArtistsBySongTitle(String songTitle) {
        List<Song> songs = songRepository.findByTitleContainingIgnoreCase(songTitle);
        return songs.stream()
                .flatMap(song -> song.getArtists().stream())
                .distinct()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }



    @Transactional
    public ArtistDTO createArtistWithUpload(String name, MultipartFile imageFile) {
        if (artistRepository.existsByName(name)) {
            throw new RuntimeException("Artist name already exists: " + name);
        }

        try {
            String imageUrl = firebaseStorageService.uploadFile(imageFile); // <-- DÙNG FIREBASE
            Artist artist = new Artist();
            artist.setName(name);
            artist.setImageUrl(imageUrl); // Lưu URL từ Firebase
            artist.setSongCount(0L);
            Artist savedArtist = artistRepository.save(artist);
            return toDTO(savedArtist);

        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file ảnh nghệ sĩ. Vui lòng thử lại!", ex);
        }
    }

    @Transactional
    public ArtistDTO updateArtist(Long id, UpdateArtistDTO updateDTO) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Artist not found: " + id));

        // Logic update một phần (Partial Update)
        if (updateDTO.getName() != null && !updateDTO.getName().equals(artist.getName())) {
            // Có thể thêm check trùng tên ở đây nếu muốn
            artist.setName(updateDTO.getName());
        }

        if (updateDTO.getImageUrl() != null) {
            artist.setImageUrl(updateDTO.getImageUrl());
        }

        Artist savedArtist = artistRepository.save(artist);
        return toDTO(savedArtist);
    }

    @Transactional
    public void deleteArtist(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new NoSuchElementException("Artist not found: " + id);
        }
        // Có thể thêm logic: Không cho xóa nếu Artist đang có bài hát
        artistRepository.deleteById(id);
    }


    private ArtistDTO toDTO(Artist a) {
        ArtistDTO dto = new ArtistDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setImageUrl(a.getImageUrl());

        // Determine if the artist is verified
        Optional<User> associatedUser = userRepository.findByArtist(a);
        dto.setVerified(associatedUser.map(user -> user.getRole() == Role.ARTIST).orElse(false));

        return dto;
    }


    private SongDTO toSongDTO(Song s) {
        SongDTO dto = new SongDTO();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());

        // 1. Map duration (Mới thêm)
        dto.setDuration(s.getDuration());

        dto.setImageUrl(s.getImageUrl());
        dto.setFilePath(s.getFilePath());
        dto.setListenCount(s.getListenCount());
        dto.setLikeCount(s.getLikeCount());
        dto.setDescription(s.getDescription());
        dto.setCreatedAt(s.getCreatedAt());

        // 2. Map danh sách Artists (Thay vì chỉ 1 tên như cũ)
        if (s.getArtists() != null) {
            Set<ArtistDTO> artistDTOS = s.getArtists().stream()
                    .map(this::toDTO) // Tái sử dụng hàm toDTO ở trên
                    .collect(Collectors.toSet());
            dto.setArtists(artistDTOS);
        }

        // 3. Map danh sách Tags (Thay vì Label)
        if (s.getTags() != null) {
            Set<TagDTO> tagDTOS = s.getTags().stream()
                    .map(t -> {
                        TagDTO tDto = new TagDTO();
                        tDto.setId(t.getId());
                        tDto.setName(t.getName());
                        // tDto.setDescription(t.getDescription()); // Nếu DTO có field này
                        return tDto;
                    })
                    .collect(Collectors.toSet());
            dto.setTags(tagDTOS);
        }

        return dto;
    }
}
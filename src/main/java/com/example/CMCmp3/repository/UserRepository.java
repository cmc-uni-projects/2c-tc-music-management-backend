package com.example.CMCmp3.repository;

import com.example.CMCmp3.entity.Artist;
import com.example.CMCmp3.entity.ArtistVerificationStatus;
import com.example.CMCmp3.entity.Role;
import com.example.CMCmp3.entity.User;
import com.example.CMCmp3.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================
    // BASIC QUERIES
    // =========================
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmailAndStatus(String email, UserStatus status);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findAllByDisplayNameContainingIgnoreCase(String query);

    Optional<User> findByArtist(Artist artist);

    // =========================
    // ✅ BACKFILL CORE (legacy)
    // ARTIST + NONE => APPROVED
    // =========================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        update User u
        set u.artistVerificationStatus = com.example.CMCmp3.entity.ArtistVerificationStatus.APPROVED
        where u.role = com.example.CMCmp3.entity.Role.ARTIST
          and u.artistVerificationStatus = com.example.CMCmp3.entity.ArtistVerificationStatus.NONE
    """)
    int backfillArtistStatusForLegacyUsers();
}

package com.example.CMCmp3.service;

import com.example.CMCmp3.dto.ArtistDTO;
import com.example.CMCmp3.dto.SongDTO;
import com.example.CMCmp3.repository.SongListenLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final SongService songService;
    private final SongListenLogRepository songListenLogRepository;

    // Cấu hình cho biểu đồ đường
    private static final int STEP_MINUTES =120; // 2 giờ
    private static final int WINDOW_POINTS = 12; // 12 điểm trong 24 giờ
    private static final DateTimeFormatter HHmm = DateTimeFormatter.ofPattern("HH:mm");

    public Map<String, Object> getRealtime() {
        // === PHẦN 1: LẤY TOP 3 BÀI HÁT HIỆN TẠI ===

        // Lấy top 3 bài được định nghĩa bởi SongService (thường theo listenCount)
        List<SongDTO> currentTop3Songs = songService.getTopSongs(3);
        if (currentTop3Songs.isEmpty()) {
            return Map.of(
                    "timeline", Collections.emptyList(),
                    "vn", Collections.emptyList(),
                    "usuk", Collections.emptyList(),
                    "kpop", Collections.emptyList(),
                    "lineChartData", Collections.emptyList(),
                    "lineChartMetadata", Collections.emptyMap(),
                    "top3", Collections.emptyList(),
                    "items", Collections.emptyList(),
                    "lastUpdated", System.currentTimeMillis()
            );
        }

        // Căn chỉnh thời gian về mốc 2 giờ chẵn gần nhất (e.g., 15:30 -> 14:00)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime alignedNow = now.withMinute(0).withSecond(0).withNano(0);
        if (alignedNow.getHour() % 2 != 0) {
            alignedNow = alignedNow.minusHours(1);
        }

        // === PHẦN 2: TÍNH TOÁN DỮ LIỆU CHO BIỂU ĐỒ ĐƯỜNG ===

        List<String> timeline = new ArrayList<>(WINDOW_POINTS);
        List<Map<String, Object>> lineChartData = new ArrayList<>(WINDOW_POINTS);

        // Thêm lại 3 mảng tương ứng top1 / top2 / top3 để FE cũ còn dùng
        List<Integer> vn = new ArrayList<>(WINDOW_POINTS);   // top 1
        List<Integer> usuk = new ArrayList<>(WINDOW_POINTS); // top 2
        List<Integer> kpop = new ArrayList<>(WINDOW_POINTS); // top 3

        for (int i = 0; i < WINDOW_POINTS; i++) {
            // endTime: mốc 2h, chạy từ 24h trước đến hiện tại
            LocalDateTime endTime = alignedNow.minusMinutes((long) (WINDOW_POINTS - 1 - i) * STEP_MINUTES);
            LocalDateTime startTime = endTime.minusMinutes(STEP_MINUTES);

            String timeLabel = endTime.format(HHmm);
            timeline.add(timeLabel);

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("time", timeLabel);

            // Lấy lượt nghe thô cho từng bài trong khoảng [startTime, endTime)
            Map<Long, Long> rawListensInInterval = new HashMap<>();
            long totalListensInInterval = 0L;
            for (SongDTO song : currentTop3Songs) {
                long listens = songListenLogRepository.countListensForSongBetween(
                        song.getId(), startTime, endTime
                );
                rawListensInInterval.put(song.getId(), listens);
                totalListensInInterval += listens;
            }

            if (totalListensInInterval <= 0) {
                // Không có lượt nghe nào trong khoảng này → cho tất cả = 0
                for (SongDTO song : currentTop3Songs) {
                    dataPoint.put("song_" + song.getId(), 0);
                }
                vn.add(0);
                usuk.add(0);
                kpop.add(0);
            } else {
                // Tính phần trăm cho từng bài
                for (int idx = 0; idx < currentTop3Songs.size(); idx++) {
                    SongDTO song = currentTop3Songs.get(idx);
                    long raw = rawListensInInterval.getOrDefault(song.getId(), 0L);
                    int percentage = (int) Math.round(raw * 100.0 / totalListensInInterval);
                    dataPoint.put("song_" + song.getId(), percentage);

                    // map theo index: 0 -> vn, 1 -> usuk, 2 -> kpop
                    if (idx == 0) vn.add(percentage);
                    else if (idx == 1) usuk.add(percentage);
                    else if (idx == 2) kpop.add(percentage);
                }

                // Nếu ít hơn 3 bài (hiếm) thì fill thêm 0 cho mảng còn lại
                while (vn.size() < i + 1) vn.add(0);
                while (usuk.size() < i + 1) usuk.add(0);
                while (kpop.size() < i + 1) kpop.add(0);
            }

            lineChartData.add(dataPoint);
        }

        // === PHẦN 3: METADATA CHO CÁC SERIES (lineChartMetadata) ===

        Map<String, Map<String, Object>> lineChartMetadata = new HashMap<>();
        for (SongDTO song : currentTop3Songs) {
            Map<String, Object> songMeta = new HashMap<>();
            songMeta.put("id", song.getId());
            songMeta.put("title", song.getTitle());
            String artists = song.getArtists().stream()
                    .map(ArtistDTO::getName)
                    .collect(Collectors.joining(", "));
            songMeta.put("artists", artists);
            songMeta.put("cover", song.getImageUrl());
            lineChartMetadata.put("song_" + song.getId(), songMeta);
        }

        // === PHẦN 4: TOP 3 BẢNG XẾP HẠNG (HIỂN THỊ BÊN TRÁI) ===

        List<Map<String, Object>> top3Formatted = new ArrayList<>();
        long totalTop3OverallListens = currentTop3Songs.stream()
                .mapToLong(SongDTO::getListenCount)
                .sum();
        if (totalTop3OverallListens <= 0) totalTop3OverallListens = 1;

        for (int i = 0; i < currentTop3Songs.size(); i++) {
            SongDTO song = currentTop3Songs.get(i);
            Map<String, Object> row = new HashMap<>();
            row.put("rank", i + 1);
            row.put("id", song.getId());
            row.put("title", song.getTitle());
            String artists = song.getArtists().stream()
                    .map(ArtistDTO::getName)
                    .collect(Collectors.joining(", "));
            row.put("artists", artists);
            row.put("cover", song.getImageUrl());
            row.put("listenCount", song.getListenCount());

            double percentage = (double) song.getListenCount() / totalTop3OverallListens * 100.0;
            row.put("percent", (int) Math.round(percentage));

            top3Formatted.add(row);
        }

        // === PHẦN 4B: LẤY TOP 100 CHO DANH SÁCH CHI TIẾT (items) ===
        List<SongDTO> top100Songs = songService.getTopSongs(100);
        List<Map<String, Object>> chartItems = new ArrayList<>();
        for (int i = 0; i < top100Songs.size(); i++) {
            SongDTO song = top100Songs.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("id", song.getId());
            item.put("title", song.getTitle());
            String artists = song.getArtists().stream()
                    .map(ArtistDTO::getName)
                    .collect(Collectors.joining(", "));
            item.put("artists", artists);
            item.put("cover", song.getImageUrl());
            item.put("duration", song.getDuration());
            item.put("listenCount", song.getListenCount());
            chartItems.add(item);
        }

        // === PHẦN 5: TRẢ VỀ ===

        return Map.of(
                // cũ – FE ZingChartSection đang dùng
                "timeline", timeline,
                "vn", vn,
                "usuk", usuk,
                "kpop", kpop,

                // mới – cho tooltip / cấu hình nâng cao
                "lineChartData", lineChartData,
                "lineChartMetadata", lineChartMetadata,

                // top3 hiển thị bên trái
                "top3", top3Formatted,
                // danh sách chi tiết (dùng cho popup, modal, v.v.)
                "items", chartItems,

                "lastUpdated", System.currentTimeMillis()
        );
    }

    public List<Map<String, String>> getWeeklyTiles() {
        return List.of(
                Map.of("code","vn","title","BÀI HÁT VIỆT NAM","cover","/top100-vpop.png"),
                Map.of("code","usuk","title","US-UK","cover","/top100.png"),
                Map.of("code","kpop","title","K-POP","cover","/top100.png")
        );
    }
}

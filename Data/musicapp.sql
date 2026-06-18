drop database musicapp
create database musicapp
use musicapp
TRUNCATE TABLE playlists;
INSERT INTO playlists (id, title, description, image_url, play_count, like_count, created_at, owner_id) VALUES (1, 'Maybe you like', 'Popular songs', 'https://i.pinimg.com/736x/14/53/be/1453be3f6f3e5d02f65a67ef795d35a4.jpg', 10, 10, '2025-02-02 00:00:00', 1), (2, 'Recommended for you', 'My personal favorite songs', 'https://i.pinimg.com/736x/1d/a2/e9/1da2e931af8b657d985a01e54860b6c4.jpg', 10, 10, '2025-02-02 00:00:00', 1), (3, 'Top Hits', 'The most popular songs right now', 'https://i.pinimg.com/736x/6d/da/db/6ddadb2f383f1d05a74327fa6016bd7e.jpg', 10, 10, '2025-02-02 00:00:00', 1), (4, 'Chill Vibes', 'Relaxing and soothing tracks', 'https://i.pinimg.com/1200x/c9/1f/9a/c91f9aa538bc6445052aa031c56fc6dd.jpg', 10, 10, '2025-02-02 00:00:00', 1), (5, 'Workout Mix', 'High energy songs to keep you moving', 'https://i.pinimg.com/1200x/01/bb/48/01bb4808452e8e888ac12ca00e85945b.jpg', 10, 10, '2025-02-02 00:00:00', 1);

INSERT INTO artists (id, name, image_url, song_count) VALUES
(1, 'Sơn Tùng M-TP', NULL, 2),
(2, 'babysis', NULL, 1),
(3, 'single mom', NULL, 1),
(4, 'Phan Mạnh Quỳnh', NULL, 3),
(5, 'Jen Hoang', NULL, 1),
(6, 'EvanDrago', NULL, 1),
(7, 'DJ Mix', NULL, 1);


INSERT INTO tags (id, name, description) VALUES
(1, 'V-Pop', 'Nhạc Pop Việt Nam'),
(2, 'Rap Việt', 'Nhạc Rap/Hip-hop Việt Nam'),
(3, 'US-UK', 'Nhạc Pop Âu Mỹ'),
(4, 'K-Pop', 'Nhạc Pop Hàn Quốc');


SET FOREIGN_KEY_CHECKS = 0;
SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO songs (id, title, duration, image_url, file_path, listen_count, like_count, description, created_at) VALUES
(11, 'Chúng ta của hiện tại', 200, 'https://i.pinimg.com/1200x/dc/e9/b5/dce9b590b593d06d7ab33fececde2017.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/SƠN TÙNG M-TP  CHÚNG TA CỦA HIỆN TẠI  OFFICIAL MUSIC VIDEO - Sơn Tùng M-TP Official.mp3', 0, 0, 'nhạc anh Tùng đẳng cấp', '2025-02-02 00:00:00'),
(2, 'Âm thầm bên em', 200, 'https://i.pinimg.com/736x/3e/1c/15/3e1c15d1c94692e375f883225975e790.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/Âm Thầm Bên Em  OFFICIAL MUSIC VIDEO  Sơn Tùng M-TP - Sơn Tùng M-TP Official.mp3', 0, 0, 'nhạc anh Tùng đẳng cấp', '2025-02-02 00:00:00'),
(3, 'Trap Queen', 180, 'https://i.pinimg.com/736x/6b/6d/97/6b6d97045deaa2db88de6e535a0f6ac2.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/Trap Queen - Adriana Gomez  Eightfold X MKJ Remix (Lyrics + Vietsub) ♫ - Top Tik Tok.mp3', 0, 0, 'nhạc alime', '2025-02-02 00:00:00'),
(4, 'Sakura', 190, 'https://i.pinimg.com/736x/8b/43/f2/8b43f2e7a607e6795d45e2ee9283b121.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/Sakura Anata ni Deaete Yokatta - 5 centimet per second - Lyric Kara HD - Iloveokoloko.mp3', 0, 0, 'nhạc đẳng cấp', '2025-02-02 00:00:00'),
(5, 'NHAT', 210, 'https://i.pinimg.com/736x/8e/a9/bb/8ea9bb20873d431f10e4a17573614f11.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/NHẠT - PHAN MẠNH QUỲNH [OFFICIAL MUSIC VIDEO] - Phan Mạnh Quỳnh Official.mp3', 0, 0, 'nhạc đẳng cấp', '2025-02-02 00:00:00'),
(6, 'Yêu 5', 230, 'https://i.pinimg.com/1200x/31/fb/38/31fb38281ee4dbb36c1da744f0966dad.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/NHẠT - PHAN MẠNH QUỲNH [OFFICIAL MUSIC VIDEO] - Phan Mạnh Quỳnh Official.mp3', 0, 0, 'nhạc đẳng cấp', '2025-02-02 00:00:00'),
(7, 'Khi phải quyên đi', 220, 'https://i.pinimg.com/736x/ba/bf/d3/babfd323a2d3f4d926c8ccecced3f511.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/Khi Phải Quên Đi  Phan Mạnh Quỳnh  Official Music Video - Phan Mạnh Quỳnh Official.mp3', 0, 0, 'nhạc đẳng cấp', '2025-02-02 00:00:00'),
(8, 'Có chàng trai viết lên cây', 250, 'https://i.pinimg.com/736x/c5/4b/d4/c54bd4dfddf7a2454af43daf71789dcb.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/Có Chàng Trai Viết Lên Cây - Phan Mạnh Quỳnh  AUDIO LYRIC OFFICIAL - Phan Mạnh Quỳnh Official.mp3', 0, 0, 'nhạc đẳng cấp', '2025-02-02 00:00:00'),
(9, 'Lay All Your Love On Me', 260, 'https://i.pinimg.com/736x/9e/aa/1c/9eaa1c462d0b4c1e1bb2fdb24118f0dc.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/[Lyrics+Vietsub] Abba-Lay All Your Love On Me (Slowed+Reverb) - S P R I N G.mp3', 0, 0, 'nhạc đẳng cấp', '2025-02-02 00:00:00'),
(10, 'Dance Beat', 270, 'https://i.pinimg.com/736x/66/b7/09/66b7097f77173017a43c5395df1c8d6f.jpg', '/home/tagiahuy/Documents/Final Project/CMCmp3_BE/Data/Music/Lyrics YÊU 5 - Rhymastic - Jen Hoang.mp3', 0, 0, 'nhạc đẳng cấp', '2025-02-02 00:00:00');


INSERT INTO song_artists (song_id, artist_id) VALUES
(1, 1),  -- Chúng ta của hiện tại - Sơn Tùng M-TP
(2, 1),  -- Âm thầm bên em - Sơn Tùng M-TP
(3, 2),  -- Trap Queen - babysis
(4, 3),  -- Sakura - single mom
(5, 4),  -- NHAT - Phan Mạnh Quỳnh
(6, 5),  -- Yêu 5 - Jen Hoang
(7, 4),  -- Khi phải quyên đi - Phan Mạnh Quỳnh
(8, 4),  -- Có chàng trai viết lên cây - Phan Mạnh Quỳnh
(9, 6),  -- Lay All Your Love On Me - EvanDrago
(10, 7); -- Dance Beat - DJ Mix


INSERT INTO song_tags (song_id, tag_id) VALUES
(1, 1),  -- pop
(2, 1),  -- pop
(3, 2),  -- nightcore
(4, 3),  -- japanese song
(5, 1),  -- pop
(6, 1),  -- pop
(7, 1),  -- pop
(8, 1),  -- pop
(9, 1),  -- pop
(10, 1); -- pop



INSERT INTO playlist_songs (playlist_id, song_id, song_order, added_at) VALUES
-- Playlist 1: Maybe you like (Tất cả 10 bài hát)
(1, 1, 1, NOW()),
(1, 2, 2, NOW()),
(1, 3, 3, NOW()),
(1, 4, 4, NOW()),
(1, 5, 5, NOW()),
(1, 6, 6, NOW()),
(1, 7, 7, NOW()),
(1, 8, 8, NOW()),
(1, 9, 9, NOW()),
(1, 10, 10, NOW()),
-- Playlist 2: Recommended for you (Bài 3, 4, 5, 6, 7)
(2, 3, 1, NOW()),
(2, 4, 2, NOW()),
(2, 5, 3, NOW()),
(2, 6, 4, NOW()),
(2, 7, 5, NOW()),
-- Playlist 3: Top Hits (Bài 5, 6, 9, 10)
(3, 5, 1, NOW()),
(3, 6, 2, NOW()),
(3, 9, 3, NOW()),
(3, 10, 4, NOW()),
-- Playlist 4: Chill Vibes (Bài 7, 8)
(4, 7, 1, NOW()),
(4, 8, 2, NOW()),
-- Playlist 5: Workout Mix (Bài 9, 10)
(5, 9, 1, NOW()),
(5, 10, 2, NOW());


INSERT INTO song_lyrics (song_id, text, time) VALUES
(12, '[intro]', 0),
(12, 'Baby, take my hand', 2.6467809113924052),
(12, 'I want you to be my husband', 4.801441692161998),
(12, 'Cause you\'re my Iron Man', 7.64507065580353),
(12, 'And I love you 3000', 9.701307410448559),
(12, 'Baby, take a chance', 12.249664295716405),
(12, 'Cause I want this to be something', 15.093293259357935),
(12, 'Straight out of a Hollywood movie', 18.527466379746837),
(12, 'I see you standing there', 22.935211967410584),
(12, 'In your Hulk outerwear', 26.46780911392405),
(12, 'And all I can think', 29.114590025316456),
(12, 'Is where is the ring', 31.761370936708865),
(12, 'Cause I know you wanna ask', 34.40815184810127),
(12, 'Scared the moment will pass', 37.054932759493674),
(12, 'I can see it in your eyes', 39.70171367088608),
(12, 'Just take me by surprise', 42.348494582278484),
(12, 'And all my friends they tell me they see', 45.27987977807379),
(12, 'You planning to get on one knee', 50.2888373164557),
(12, 'But I want it to be out of the blue', 54.805674724214796),
(12, 'So make sure I have no clues', 60.798872523842306),
(12, 'When you ask', 64.42989369648035),
(12, 'Baby, take my hand', 66.08418043304388),
(12, 'I want you to be my husband', 68.14041718768893),
(12, 'Cause you\'re my Iron Man', 70.98404615133049),
(12, 'And I love you 3000', 73.63082706272286),
(12, 'Baby, take a chance', 77.36027226148546),
(12, 'Cause I want this to be something', 79.51493304225505),
(12, 'Straight out of a Hollywood movie', 81.76801784914922),
(12, '[Verse 2]', 85.79273512628549),
(12, 'Now we\'re having dinner', 87.06157967193401),
(12, 'And baby you\'re my winner', 90.0036326617001),
(12, 'I see the way you smile', 93.14253370371532),
(12, 'You\'re thinking about the aisle', 95.29719448448492),
(12, 'You reach in your pocket', 98.33767150037558),
(12, 'Emotion unlocking', 100.59075630726971),
(12, 'And before you could ask', 103.13911319253756),
(12, 'I answer too fast', 105.39219799943173),
(12, 'And all my friends they tell me they see', 108.22515916910245),
(12, 'You planing to get on one knee', 114.61205307322827),
(12, 'So now I can\'t stop thinking about you', 118.93204242873824),
(12, 'I figured out all the clues', 124.92524022836577),
(12, 'So now I ask', 127.86729321813183),
(12, 'Baby, take my hand', 130.50340633555348),
(12, 'I want you to be my husband', 132.65806711632305),
(12, 'Cause you\'re my Iron Man', 134.9111519232172),
(12, 'And I love you 3000', 136.86896465173768),
(12, 'Baby, take a chance', 140.49998582437573),
(12, 'Cause I want this to be something', 142.75307063126988),
(12, 'Straight out of a Hollywood movie', 145.3014275165377),
(12, '[Post-Chorus]', 150.01511297654594),
(12, 'No spoilers please', 152.66189388793833),
(12, 'No spoilers please', 158.75351571369043),
(12, 'Baby, take my hand', 161.88174896173487),
(12, 'I want you to be my husband', 164.4301058470027),
(12, 'Cause you\'re my Iron Man', 166.97846273227054),
(12, 'And I love you 3000', 169.3299715652893),
(12, 'Baby, take a chance', 172.86256871180274),
(12, 'Cause I want this to be something', 175.0172294925723),
(12, 'Straight out of a Hollywood movie, Baby', 177.86085845621383),
(12, '[Post-Chorus]', 182.96824002072032),
(12, 'No spoilers please', 185.22132482761447),
(12, 'No spoilers please', 191.214522627242),
(12, 'No spoilers please', 195.43608795662738),
(12, 'And I love you 3000', 201.23243770400583);



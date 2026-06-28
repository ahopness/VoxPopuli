PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS `SourceEntity` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `name` TEXT NOT NULL,
    `logoUrl` TEXT NOT NULL,
    `category` TEXT NOT NULL,
    `feedUrl` TEXT NOT NULL,
    `lastFetched` INTEGER NOT NULL,
    `muted` INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `PostEntity` (
    `id` INTEGER PRIMARY KEY NOT NULL,
    `publishedAt` INTEGER NOT NULL,
    `sourceId` INTEGER NOT NULL,
    `author` TEXT NOT NULL,
    `title` TEXT NOT NULL,
    `description` TEXT NOT NULL,
    `pubDate` TEXT NOT NULL,
    `link` TEXT NOT NULL,
    `comments` TEXT NOT NULL,
    `embedding` BLOB NOT NULL,
    `bookmarked` INTEGER NOT NULL,
    FOREIGN KEY(`sourceId`)
        REFERENCES `SourceEntity`(`id`)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS `index_PostEntity_sourceId_publishedAt` ON `PostEntity` (`sourceId`, `publishedAt`);
CREATE INDEX IF NOT EXISTS `index_PostEntity_bookmarked` ON `PostEntity` (`bookmarked`);

CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT);
INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '17385d8550fb338a96f538dbe80f2218');

------------------------------------------------------------------------------------------------------------

INSERT INTO `SourceEntity` (`name`, `logoUrl`, `category`, `feedUrl`, `lastFetched`, `muted`) VALUES
-- 1. GENERAL
('BBC (Front Page)', 'https://www.bitcni.org.uk/wp-content/uploads/2018/09/BBC-logo.png', 'GENERAL', 'http://newsrss.bbc.co.uk/rss/newsonline_uk_edition/front_page/rss.
xml', '0', '0'),
('CNN', 'https://images.seeklogo.com/logo-png/3/2/cnn-logo-png_seeklogo-32699.png', 'GENERAL', 'http://rss.cnn.com/rss/edition_world.rss', '0', '0'),
('New York Times (Front Page)', 'https://1000logos.net/wp-content/uploads/2017/04/Symbol-New-York-Times.png', 'GENERAL', 'https://rss.nytimes.
com/services/xml/rss/nyt/HomePage.xml', '0', '0'),
('New York Times (World)', 'https://1000logos.net/wp-content/uploads/2017/04/Symbol-New-York-Times.png', 'GENERAL', 'https://rss.nytimes.
com/services/xml/rss/nyt/World.xml', '0', '0'),
('Washington Post', 'https://logos-world.net/wp-content/uploads/2022/12/Washington-Post-Emblem.png', 'GENERAL', 'http://feeds.washingtonpost.com/rss/world', '0', '0'),
('Wall Street Journal', 'https://images.icon-icons.com/2699/PNG/512/wsj_logo_icon_168755.png', 'GENERAL', 'https://www.wsj.com/news/rss-news-and-feeds', '0', '0'),

-- 2. ENTERTAINMENT
('Variety', 'https://download.logo.wine/logo/Variety_(magazine)/Variety_(magazine)-Logo.wine.png', 'ENTERTAINMENT', 'http://feeds.feedburner.com/ign/all', '0', '0'),
('Billboard', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/Billboard_logo.svg/3840px-Billboard_logo.svg.png', 'ENTERTAINMENT', 'https://www.
billboard.com/articles/rss.xml', '0', '0'),
('Pitchfork', 'https://iconape.com/wp-content/png_logo_vector/pitchfork-logo.png', 'ENTERTAINMENT', 'http://pitchfork.com/rss/news', '0', '0'),
('The Onion', 'https://brandslogos.com/wp-content/uploads/images/the-onion-logo-vector.svg', 'ENTERTAINMENT', 'https://www.theonion.com/rss', '0', '0'),

-- 3. GAMING
('Rock, Paper, Shotgun', 'https://upload.wikimedia.org/wikipedia/en/4/49/Rock%2C_Paper%2C_Shotgun.svg', 'GAMING', 'http://feeds.feedburner.com/RockPaperShotgun', '0', '0'),
('Kotaku', 'https://download.logo.wine/logo/Kotaku/Kotaku-Logo.wine.png', 'GAMING', 'https://kotaku.com/rss', '0', '0'),
('Polygon', 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Polygon_logo_%282024%29.svg/3840px-Polygon_logo_%282024%29.svg.png', 'GAMING',
'https://www.polygon.com/feed/news/', '0', '0'),
('IGN', 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/IGN_logo.svg/960px-IGN_logo.svg.png?_=20230202004547', 'GAMING', 'http://feeds.feedburner.
com/ign/all', '0', '0'),

-- 4. TECHNOLOGY
('The Verge', 'https://www.freelogovectors.net/wp-content/uploads/2023/03/the-verge-logo-freelogovectors.net_.png', 'TECHNOLOGY', 'https://www.theverge.
com/rss/index.xml', '0', '0'),
('TechCrunch', 'https://upload.wikimedia.org/wikipedia/commons/7/7f/TechCrunch_Logo_2013.png', 'TECHNOLOGY', 'https://techcrunch.com/feed/', '0', '0'),
('WIRED (Technology)', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/Wired_logo.svg/960px-Wired_logo.svg.png', 'TECHNOLOGY', 'https://www.wired.
com/feed/rss', '0', '0'),
('Ars Technica', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/51/Ars_Technica_logo_%282016%29.svg/500px-Ars_Technica_logo_%282016%29.svg.png',
'TECHNOLOGY', 'https://feeds.arstechnica.com/arstechnica/index', '0', '0'),

-- 5. PROGRAMMING
('Hacker News', 'https://cdn.dribbble.com/userupload/19223502/file/original-135bb6620afae1c591a2f1cc39fc7ce0.png', 'PROGRAMMING', 'https://news.ycombinator.
com/rss', '0', '0'),
('Hacker News (Alternative)', 'https://cdn.dribbble.com/userupload/19223502/file/original-135bb6620afae1c591a2f1cc39fc7ce0.png', 'PROGRAMMING', 'https://hnrss.
org/frontpage', '0', '0'),
('Lobsters', 'https://lobste.rs/assets/logo-transparent-e4f2f8b4.svg', 'PROGRAMMING', 'https://lobste.rs/rss', '0', '0'),
('Github', 'https://images.seeklogo.com/logo-png/30/2/github-logo-png_seeklogo-304612.png', 'PROGRAMMING', 'https://mshibanami.github.
io/GitHubTrendingRSS/daily/all.xml', '0', '0'),
('XKCD', 'https://xkcd.com/s/0b7742.png', 'PROGRAMMING', 'https://xkcd.com/rss.xml', '0', '0'),

-- 6. BUSINESS
('Bloomberg (Markets)', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Bloomberg_logo.svg/3840px-Bloomberg_logo.svg.png', 'BUSINESS',
'https://feeds.bloomberg.com/markets/news.rss', '0', '0'),
('Bloomberg (Wealth)', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Bloomberg_logo.svg/3840px-Bloomberg_logo.svg.png', 'BUSINESS',
'https://feeds.bloomberg.com/wealth/news.rss', '0', '0'),
('Bloomberg (Economics)', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Bloomberg_logo.svg/3840px-Bloomberg_logo.svg.png', 'BUSINESS',
'https://feeds.bloomberg.com/economics/news.rss', '0', '0'),
('New York Times (Business)', 'https://1000logos.net/wp-content/uploads/2017/04/Symbol-New-York-Times.png', 'BUSINESS', 'https://rss.nytimes.
com/services/xml/rss/nyt/Business.xml', '0', '0'),
('Forbes', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Forbes_logo.svg/1280px-Forbes_logo.svg.png', 'BUSINESS', 'https://www.forbes.
com/business/feed/', '0', '0'),

-- 7. SCIENCE
('BBC (Science)', 'https://www.bitcni.org.uk/wp-content/uploads/2018/09/BBC-logo.png', 'SCIENCE', 'http://feeds.bbci.co.uk/news/science_and_environment/rss.xml', '0', '0'),
('New York Times (Science)', 'https://1000logos.net/wp-content/uploads/2017/04/Symbol-New-York-Times.png', 'SCIENCE', 'https://rss.nytimes.
com/services/xml/rss/nyt/Science.xml', '0', '0'),
('WIRED (Science)', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/Wired_logo.svg/960px-Wired_logo.svg.png', 'SCIENCE', 'https://www.wired.
com/feed/category/science/latest/rss', '0', '0'),
('TED', 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/aa/TED_three_letter_logo.svg/330px-TED_three_letter_logo.svg.png', 'SCIENCE', 'https://pa.tedcdn.
com/feeds/talks.rss', '0', '0'),

-- 8. SPORTS
('BBC (Sports)', 'https://www.bitcni.org.uk/wp-content/uploads/2018/09/BBC-logo.png', 'SPORTS', 'http://feeds.bbci.co.uk/sport/rss.xml', '0', '0'),
('Yahoo', 'https://logosmarcas.net/wp-content/uploads/2020/11/Yahoo-Logo.png', 'SPORTS', 'https://sports.yahoo.com/rss/', '0', '0'),
('ESPN', 'https://logosmarcas.net/wp-content/uploads/2020/12/ESPN-Logo-650x366.png', 'SPORTS', 'https://www.espn.com/espn/rss/news', '0', '0'),

-- 9. FASHION
('ELLE', 'https://upload.wikimedia.org/wikipedia/commons/9/9c/ELLE_Magazine_Logo.svg', 'FASHION', 'https://www.elle.com/rss/fashion.xml/', '0', '0'),
('The Guardian', 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/The_Guardian_2018.svg/3840px-The_Guardian_2018.svg.png', 'FASHION', 'https://www.
theguardian.com/fashion/rss', '0', '0'),
('New York Times (Fashion)', 'https://1000logos.net/wp-content/uploads/2017/04/Symbol-New-York-Times.png', 'FASHION', 'https://rss.nytimes.
com/services/xml/rss/nyt/FashionandStyle.xml', '0', '0'),

-- 10. POLITICS
('npr', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/NPR_new_logo.svg/1280px-NPR_new_logo.svg.png', 'POLITICS', 'https://feeds.npr.org/1014/rss.xml', '0', '0'),
('Politico', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/POLITICOLOGO.svg/1280px-POLITICOLOGO.svg.png', 'POLITICS', 'https://www.politico.
com/rss/politicopicks.xml', '0', '0'),
('Bloomberg (Politics)', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Bloomberg_logo.svg/3840px-Bloomberg_logo.svg.png', 'POLITICS', 'https://feeds.
bloomberg.com/politics/news.rss', '0', '0'),
('New York Times (Politics)', 'https://1000logos.net/wp-content/uploads/2017/04/Symbol-New-York-Times.png', 'POLITICS', 'https://www.nytimes.
com/svc/collections/v1/publish/https://www.nytimes.com/section/politics/rss.xml', '0', '0');

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
    `imageUrl` TEXT NOT NULL,
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
('BBC (Front Page)', 'https://upload.wikimedia.org/wikipedia/commons/6/65/BBC_logo_(1997-2021).svg', 'GENERAL', 'https://feeds.bbci.co.uk/news/rss.xml', '0', '0'),
('New York Times (Front Page)', 'https://upload.wikimedia.org/wikipedia/commons/5/58/NewYorkTimes.svg', 'GENERAL', 'https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml', '0', '0'),
('New York Times (World)', 'https://upload.wikimedia.org/wikipedia/commons/5/58/NewYorkTimes.svg', 'GENERAL', 'https://rss.nytimes.com/services/xml/rss/nyt/World.xml', '0', '0'),
('Wall Street Journal (World)', 'https://upload.wikimedia.org/wikipedia/commons/4/4a/WSJ_Logo.svg', 'GENERAL', 'https://feeds.content.dowjones.io/public/rss/RSSWorldNews', '0', '0'),

-- 2. ENTERTAINMENT
('Variety', 'https://upload.wikimedia.org/wikipedia/commons/c/ca/Variety_logo.svg', 'ENTERTAINMENT', 'https://feeds.feedburner.com/ign/all', '0', '0'),
('IndieWire', 'https://upload.wikimedia.org/wikipedia/commons/8/83/IndieWire_logo_2016.png', 'ENTERTAINMENT', 'https://www.indiewire.com/feed', '0', '0'),
('Pitchfork', 'https://upload.wikimedia.org/wikipedia/commons/7/76/Pitchfork_logo.svg', 'ENTERTAINMENT', 'https://pitchfork.com/rss/news', '0', '0'),

-- 3. GAMING
('Rock Paper Shotgun', 'https://upload.wikimedia.org/wikipedia/en/4/49/Rock%2C_Paper%2C_Shotgun.svg', 'GAMING', 'https://feeds.feedburner.com/RockPaperShotgun', '0', '0'),
('Kotaku', 'https://upload.wikimedia.org/wikipedia/commons/2/28/Kotaku_logo.svg', 'GAMING', 'https://kotaku.com/rss', '0', '0'),
('Polygon', 'https://upload.wikimedia.org/wikipedia/commons/b/b4/Polygon_logo_%282024%29.svg', 'GAMING', 'https://www.polygon.com/feed/news/', '0', '0'),

-- 4. TECHNOLOGY
('TechCrunch', 'https://upload.wikimedia.org/wikipedia/commons/b/b9/TechCrunch_logo.svg', 'TECHNOLOGY', 'https://techcrunch.com/feed/', '0', '0'),
('WIRED (Technology)', 'https://upload.wikimedia.org/wikipedia/commons/9/95/Wired_logo.svg', 'TECHNOLOGY', 'https://www.wired.com/feed/rss', '0', '0'),
('Ars Technica', 'https://upload.wikimedia.org/wikipedia/commons/5/51/Ars_Technica_logo_%282016%29.svg', 'TECHNOLOGY', 'https://feeds.arstechnica.com/arstechnica/index', '0', '0'),

-- 5. PROGRAMMING
--('Hacker News', 'https://upload.wikimedia.org/wikipedia/commons/9/95/Font_Awesome_5_brands_hacker-news-square.svg', 'PROGRAMMING', 'https://news.ycombinator.com/rss', '0', '0'),
('Hacker News', 'https://upload.wikimedia.org/wikipedia/commons/9/95/Font_Awesome_5_brands_hacker-news-square.svg', 'PROGRAMMING', 'https://hnrss.org/frontpage', '0', '0'),
('Lobsters', 'https://avatars.githubusercontent.com/u/32438445', 'PROGRAMMING', 'https://lobste.rs/rss', '0', '0'),
('Github', 'https://upload.wikimedia.org/wikipedia/commons/9/91/Octicons-mark-github.svg', 'PROGRAMMING', 'https://mshibanami.github.io/GitHubTrendingRSS/daily/all.xml', '0', '0'),
('XKCD', 'https://xkcd.com/s/0b7742.png', 'PROGRAMMING', 'https://xkcd.com/rss.xml', '0', '0'),

-- 6. BUSINESS
('Bloomberg (Markets)', 'https://upload.wikimedia.org/wikipedia/commons/9/9d/Bloomberg_logo-2556aaa618.svg', 'BUSINESS', 'https://feeds.bloomberg.com/markets/news.rss', '0', '0'),
('Bloomberg (Economics)', 'https://upload.wikimedia.org/wikipedia/commons/9/9d/Bloomberg_logo-2556aaa618.svg', 'BUSINESS', 'https://feeds.bloomberg.com/economics/news.rss', '0', '0'),
('New York Times (Business)', 'https://upload.wikimedia.org/wikipedia/commons/5/58/NewYorkTimes.svg', 'BUSINESS', 'https://rss.nytimes.com/services/xml/rss/nyt/Business.xml', '0', '0'),
('Forbes', 'https://upload.wikimedia.org/wikipedia/commons/d/db/Forbes_logo.svg', 'BUSINESS', 'https://www.forbes.com/business/feed/', '0', '0'),

-- 7. SCIENCE
('BBC (Science)', 'https://upload.wikimedia.org/wikipedia/commons/6/65/BBC_logo_(1997-2021).svg', 'SCIENCE', 'https://feeds.bbci.co.uk/news/science_and_environment/rss.xml', '0', '0'),
('New York Times (Science)', 'https://upload.wikimedia.org/wikipedia/commons/5/58/NewYorkTimes.svg', 'SCIENCE', 'https://rss.nytimes.com/services/xml/rss/nyt/Science.xml', '0', '0'),
('WIRED (Science)', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/Wired_logo.svg/960px-Wired_logo.svg.png', 'SCIENCE', 'https://www.wired.com/feed/category/science/latest/rss', '0', '0'),

-- 8. SPORTS
('BBC (Sports)', 'https://upload.wikimedia.org/wikipedia/commons/6/65/BBC_logo_(1997-2021).svg', 'SPORTS', 'https://feeds.bbci.co.uk/sport/rss.xml', '0', '0'),
('Wall Street Journal (Sports)', 'https://upload.wikimedia.org/wikipedia/commons/4/4a/WSJ_Logo.svg', 'SPORTS', 'https://feeds.content.dowjones.io/public/rss/rsssportsfeed', '0', '0'),
('ESPN', 'https://logosmarcas.net/wp-content/uploads/2020/12/ESPN-Logo-650x366.png', 'SPORTS', 'https://www.espn.com/espn/rss/news', '0', '0'),

-- 9. FASHION
('ELLE', 'https://upload.wikimedia.org/wikipedia/commons/9/9c/ELLE_Magazine_Logo.svg', 'FASHION', 'https://www.elle.com/rss/fashion.xml/', '0', '0'),
('The Guardian', 'https://upload.wikimedia.org/wikipedia/commons/7/75/The_Guardian_2018.svg', 'FASHION', 'https://www.theguardian.com/fashion/rss', '0', '0'),
('New York Times (Fashion)', 'https://upload.wikimedia.org/wikipedia/commons/5/58/NewYorkTimes.svg', 'FASHION', 'https://rss.nytimes.com/services/xml/rss/nyt/FashionandStyle.xml', '0', '0'),

-- 10. POLITICS
('npr', 'https://upload.wikimedia.org/wikipedia/commons/2/2e/National_Public_Radio_logo_%282%29.svg', 'POLITICS', 'https://feeds.npr.org/1014/rss.xml', '0', '0'),
('Wall Street Journal (Politics)', 'https://upload.wikimedia.org/wikipedia/commons/4/4a/WSJ_Logo.svg', 'POLITICS', 'https://feeds.content.dowjones.io/public/rss/socialpoliticsfeed', '0', '0'),
('Bloomberg (Politics)', 'https://upload.wikimedia.org/wikipedia/commons/9/9d/Bloomberg_logo-2556aaa618.svg', 'POLITICS', 'https://feeds.bloomberg.com/politics/news.rss', '0', '0'),
('New York Times (Politics)', 'https://upload.wikimedia.org/wikipedia/commons/5/58/NewYorkTimes.svg', 'POLITICS', 'https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/politics/rss.xml', '0', '0');

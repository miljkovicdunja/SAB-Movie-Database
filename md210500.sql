CREATE DATABASE md210500;
go

use md210500;
go

CREATE TABLE Users(
	ID INT IDENTITY(1,1) primary key,
	Username nvarchar(100) not null unique,
	Rewards INT not null default 0,
);
CREATE TABLE Movies(
	ID INT IDENTITY(1,1) primary key,
	Title NVARCHAR(100) not null,
	Director NVARCHAR(100) not null,
	TrendStatus NVARCHAR(100) null
);
CREATE TABLE Genres (
    Id   INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE MovieGenres (
    MovieID INT NOT NULL,
    GenreID INT NOT NULL,
    CONSTRAINT PK_MovieGenres PRIMARY KEY (MovieId, GenreId),
    CONSTRAINT FK_MovieGenres_Movie FOREIGN KEY (MovieID) REFERENCES Movies(ID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MovieGenres_Genre FOREIGN KEY (GenreID) REFERENCES Genres(ID)
        ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE Tags (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE MovieTags (
    MovieID INT NOT NULL,
    TagID INT NOT NULL,

    CONSTRAINT PK_LinkMovieTags PRIMARY KEY (MovieID, TagID),

    CONSTRAINT FK_LinkMovieTags_Movie FOREIGN KEY (MovieID)
        REFERENCES Movies(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT FK_LinkMovieTags_Tag FOREIGN KEY (TagID)
        REFERENCES Tags(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
CREATE TABLE UserSpecializations (
    UserID INT NOT NULL,
    TagID INT NOT NULL,
    CONSTRAINT PK_UserSpecializations PRIMARY KEY (UserID, TagID),
    CONSTRAINT FK_UserSpecializations_User FOREIGN KEY (UserID)
        REFERENCES Users(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT FK_UserSpecializations_Tag FOREIGN KEY (TagID)
        REFERENCES Tags(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE Watchlist (
    UserID INT NOT NULL,
    MovieID INT NOT NULL,

    CONSTRAINT PK_Watchlist PRIMARY KEY (UserID, MovieID),

    CONSTRAINT FK_Watchlist_User FOREIGN KEY (UserID)
        REFERENCES Users(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT FK_Watchlist_Movie FOREIGN KEY (MovieID)
        REFERENCES Movies(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
CREATE TABLE Ratings (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL,
    MovieID INT NOT NULL,
    Score INT NOT NULL CHECK (Score BETWEEN 1 AND 10),
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    RatingDate DATETIME NOT NULL DEFAULT GETDATE(),
   
   

    CONSTRAINT UQ_Ratings_UserMovie UNIQUE (UserID, MovieID),

    CONSTRAINT FK_Ratings_User FOREIGN KEY (UserID)
        REFERENCES Users(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT FK_Ratings_Movie FOREIGN KEY (MovieID)
        REFERENCES Movies(ID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
GO

--trigeri i procedure

use md210500
go

CREATE OR ALTER TRIGGER TR_BLOCK_EXTREME
ON Ratings
AFTER INSERT, UPDATE
AS
BEGIN
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN MovieGenres mg ON mg.MovieID = i.MovieID
        WHERE i.Score IN (1, 10)
          AND (
                SELECT COUNT(*)
                FROM Ratings r
                JOIN MovieGenres mg2 ON mg2.MovieID = r.MovieID
                WHERE r.UserID = i.UserID
                  AND mg2.GenreID = mg.GenreID
                  AND r.Score IN (1, 10)
                  AND NOT (r.UserID = i.UserID AND r.MovieID = i.MovieID)
              ) > 3
          AND (
                SELECT COUNT(*)
                FROM Ratings r
                JOIN MovieGenres mg2 ON mg2.MovieID = r.MovieID
                WHERE r.UserID = i.UserID
                  AND mg2.GenreID = mg.GenreID
                  AND r.Score IN (6, 7, 8)
                  AND NOT (r.UserID = i.UserID AND r.MovieID = i.MovieID)
              ) < 3
    )THROW 50000, N'GRESKA: previse ekstremnih ocena', 1;
END
GO

CREATE OR ALTER TRIGGER TR_UPDATE_MOVIE_TREND
ON Ratings
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH MovieStats AS (
        SELECT
            m.ID AS MovieID,
            (SELECT COUNT(*) FROM Ratings r WHERE r.MovieID = m.ID) AS TotalCnt,
            (SELECT AVG(CAST(r.Score AS DECIMAL(10,3))) FROM Ratings r WHERE r.MovieID = m.ID) AS TotalAvg,
            (SELECT AVG(CAST(x.Score AS DECIMAL(10,3)))
             FROM (SELECT TOP 5 Score FROM Ratings r2
                   WHERE r2.MovieID = m.ID
                   ORDER BY r2.CreatedAt DESC, r2.ID DESC) x) AS Last5Avg,
            (SELECT COUNT(*) FROM Ratings r3
             WHERE r3.MovieID = m.ID
               AND r3.CreatedAt >= DATEADD(DAY, -30, GETDATE())) AS RecentCnt
        FROM Movies m
    ),
    MaxTrend AS (
        SELECT MAX(RecentCnt) AS MaxRecentCnt FROM MovieStats
    )
    UPDATE m
    SET m.TrendStatus =
        CASE
            WHEN ms.TotalCnt >= 5 AND ms.Last5Avg IS NOT NULL AND ms.Last5Avg >= ms.TotalAvg + 1 THEN 'Rising'
            WHEN ms.TotalCnt >= 5 AND ms.Last5Avg IS NOT NULL AND ms.Last5Avg <= ms.TotalAvg - 1 THEN 'Falling'
            WHEN ms.TotalCnt >= 3 AND ms.TotalAvg >= 8 THEN 'Classic'
            WHEN ms.RecentCnt > 0 AND ms.RecentCnt = mt.MaxRecentCnt THEN 'Trending'
            ELSE NULL
        END
    FROM Movies m
    JOIN MovieStats ms ON ms.MovieID = m.ID
    CROSS JOIN MaxTrend mt;
END
GO


CREATE PROCEDURE SP_REWARD_USER_
    @UserId  INT,
    @MovieId INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @TotalRatings INT;
    SELECT @TotalRatings = COUNT(*) FROM Ratings WHERE UserId = @UserId;
    IF @TotalRatings < 10 RETURN;

    DECLARE @GlobalAvgExcl DECIMAL(10,3);
    SELECT @GlobalAvgExcl = AVG(CAST(Score AS DECIMAL(10,3)))
    FROM Ratings
    WHERE MovieId = @MovieId AND UserId <> @UserId;

    IF @GlobalAvgExcl IS NULL OR @GlobalAvgExcl >= 6 RETURN;

    IF EXISTS (
        SELECT 1
        FROM MovieGenres mg
        WHERE mg.MovieId = @MovieId
          AND (
                SELECT AVG(CAST(r.Score AS DECIMAL(10,3)))
                FROM Ratings r
                JOIN MovieGenres mg2 ON mg2.MovieId = r.MovieId
                WHERE r.UserId = @UserId AND mg2.GenreId = mg.GenreId
              ) >= 8
    )
    BEGIN
        UPDATE Users SET Rewards = Rewards + 1 WHERE Id = @UserId;
    END
END
GO

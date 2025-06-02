CREATE TABLE stations
(
	StationID SERIAL PRIMARY KEY,
	XCoordinate INT NOT NULL,
	YCoordinate INT NOT NULL,
	StationName VARCHAR(30) NOT NULL
);

CREATE TABLE lines
(
	LineID SERIAL PRIMARY KEY,
	LineColor VARCHAR(30) NOT NULL,
	LineName VARCHAR(30) NOT NULL
);

CREATE TABLE connections
(
	ConnectionID SERIAL PRIMARY KEY,
	LineID INT NOT NULL,
	Station1ID INT NOT NULL,
	Station2ID INT NOT NULL,
	FOREIGN KEY (LineID) REFERENCES lines (LineID),
	FOREIGN KEY (Station1ID) REFERENCES stations (StationID),
	FOREIGN KEY (Station2ID) REFERENCES stations (StationID),
	TravelTime INT NOT NULL
);

INSERT INTO stations (StationID, XCoordinate, YCoordinate, StationName) 
VALUES (1, 900, 100, 'Измайловская'), 
		(2, 700, 130, 'Проспект мира'), 
		(3, 800, 220, 'Курская'),
		(4, 800, 410, 'Белорусская'),
		(5, 580, 490, 'Арбат'),
		(6, 315, 410, 'Москва-сити'),
		(7, 290, 190, 'Серпуховская'),
		(8, 510, 90, 'Ховрино'),
		(9, 460, 280, 'Парк победы'),
		(10, 670, 315, 'Бауманская'),
		(11, 120, 550, 'Одинцово'),
		(12, 150, 60, 'Шереметьево'),
		(13, 900, 575, 'Люблино')
ON CONFLICT (StationID) DO UPDATE 
  SET StationID = excluded.StationID, 
      XCoordinate = excluded.XCoordinate,
	  YCoordinate = excluded.YCoordinate,
	  StationName = excluded.StationName;

INSERT INTO lines (LineID, LineColor, LineName) 
VALUES (1, 'Brown', 'Кольцевая'), 
		(2, 'Red', 'Сокольническая'), 
		(3, 'Blue', 'Арбатско-Покровская'),
		(4, 'Green', 'Замоскворецкая')
ON CONFLICT (LineID) DO UPDATE 
  SET LineID = excluded.LineID, 
      LineColor = excluded.LineColor,
	  LineName = excluded.LineName;

INSERT INTO connections (ConnectionID, LineID, Station1ID, Station2ID, TravelTime) 
VALUES (1, 3, 1, 3, 3), 
		(2, 1, 2, 3, 2),
		(3, 1, 3, 4, 4),
		(4, 1, 4, 5, 2),
		(5, 1, 5, 6, 4),
		(6, 1, 6, 7, 3),
		(7, 1, 7, 8, 4),
		(8, 4, 8, 9, 1),
		(9, 1, 2, 8, 4),
		(10, 3, 3, 10, 4),
		(11, 4, 9, 5, 3),
		(12, 3, 6, 10, 9),
		(13, 3, 6, 11, 5),
		(14, 2, 9, 4, 6),
		(15, 2, 9, 7, 4),
		(16, 2, 12, 7, 2),
		(17, 2, 4, 13, 3)
ON CONFLICT (ConnectionID) DO UPDATE 
  SET ConnectionID = excluded.ConnectionID, 
      LineID = excluded.LineID,
	  Station1ID = excluded.Station1ID,
	  Station2ID = excluded.Station2ID;

/*SELECT * FROM stations;

SELECT * FROM lines;

SELECT * FROM connections;  

SELECT S1.XCoordinate, S1.YCoordinate, S2.XCoordinate, S2.YCoordinate, L.LineColor
FROM connections AS C
JOIN stations AS S1 ON C.Station1ID = S1.StationID 
JOIN stations AS S2 ON C.Station2ID = S2.StationID 
JOIN lines AS L ON C.LineID = L.LineID;

SELECT XCoordinate, YCoordinate, StationName FROM stations;

SELECT Station1ID, Station2ID, TravelTime FROM connections;

SELECT COUNT(StationID) FROM stations;

SELECT StationID FROM stations
WHERE StationName = 'Измайловская';

SELECT S1.XCoordinate, S1.YCoordinate, S2.XCoordinate, S2.YCoordinate, L.LineColor
FROM connections AS C
INNER JOIN stations AS S1 ON C.Station1ID = S1.StationID OR C.Station2ID = S1.StationID
INNER JOIN stations AS S2 ON C.Station2ID = S2.StationID OR C.Station1ID = S2.StationID
INNER JOIN lines AS L ON C.LineID = L.LineID
WHERE S1.StationID = 8 AND S2.StationID = 2;*/
                


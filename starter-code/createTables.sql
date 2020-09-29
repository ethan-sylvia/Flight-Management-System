.mode csv
.header ON

CREATE TABLE Carriers (
    cid VARCHAR(7) PRIMARY KEY,
    name VARCHAR(83)
);

CREATE TABLE Months (
    mid INTEGER PRIMARY KEY,
    month VARCHAR(9)
);
CREATE TABLE Weekdays (
    did INTEGER PRIMARY KEY,
    day_of_week VARCHAR(9)
);


CREATE TABLE FLIGHTS (
    fid INTEGER PRIMARY KEY, 
    month_id INTEGER,        -- 1-12
    day_of_month INTEGER,    -- 1-31 
    day_of_week_id INTEGER,  -- 1-7, 1 = Monday, 2 = Tuesday, etc
    carrier_id VARCHAR(7), 
    flight_num INTEGER,
    origin_city VARCHAR(34), 
    origin_state VARCHAR(47), 
    dest_city VARCHAR(34), 
    dest_state VARCHAR(46), 
    departure_delay INTEGER, -- in mins
    taxi_out INTEGER,        -- in mins
    arrival_delay INTEGER,   -- in mins
    canceled INTEGER,        -- 1 means canceled
    actual_time INTEGER,     -- in mins
    distance INTEGER,        -- in miles
    capacity INTEGER, 
    price INTEGER,            -- in $   
    FOREIGN KEY (carrier_id)
        REFERENCES Carriers(cid),
    FOREIGN KEY (month_id)
        REFERENCES Months(mid),
    FOREIGN KEY (day_of_week_id)
        REFERENCES Weekdays(did)          
);

.import flights-small.csv FLIGHTS
.import carriers.csv CARRIERS
.import months.csv MONTHS
.import weekdays.csv WEEKDAYS

CREATE TABLE Reservation (   
    rid int NOT NULL PRIMARY KEY,
	fid1 int references FLIGHTS,
	fid2 int references FLIGHTS DEFAULT (0),
	paid int,
	cost int,
	username varchar(20)
    FOREIGN KEY (username)
        REFERENCES Users(username)
);

CREATE TABLE Users (
	username varchar(20) NOT NULL PRIMARY KEY,
	password varchar(20),
	balance int
);







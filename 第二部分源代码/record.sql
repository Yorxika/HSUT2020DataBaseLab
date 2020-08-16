/*
乘车记录表【记录编号，乘客身份证号，列车流水号，出发站编号，到达站编号，车厢号，席位排号，席位编号，席位状态】
CarrigeID若为空，则表示“无座”；
SeatNo只能取值为’A’、’B’、’C’、’E’、’F’，或为空值；
SStatus只能取值’0’（退票）、’1’（正常）、’2’（乘客没上车）。
*/
DROP TABLE IF EXISTS `TakeTrainRecord`;
CREATE TABLE TakeTrainRecord (
	RID int PRIMARY KEY AUTO_INCREMENT, 
	PCardID char(18), 
	TID int, 
	SStationID int, 
	AStationID int, 
	CarrigeID smallint, 
	SeatRow smallint,
	SeatNo char(1) CHECK (SeatNo IN ('A','B','C','E','F',NULL)),
	SStatus int CHECK (SStatus >= 0 AND SStatus <= 2),
	FOREIGN KEY (PCardID) REFERENCES passenger (PCardID) ON DELETE RESTRICT ON UPDATE RESTRICT,
	FOREIGN KEY (TID) REFERENCES trainpass (TID) ON DELETE RESTRICT ON UPDATE RESTRICT,
	FOREIGN KEY (SStationID) REFERENCES train (SStationID) ON DELETE RESTRICT ON UPDATE RESTRICT,
	FOREIGN KEY (AStationID) REFERENCES train (AStationID) ON DELETE RESTRICT ON UPDATE RESTRICT
)ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

/*增加数据*/

delimiter $$ ;
DROP PROCEDURE IF EXISTS insertTakeTrainTecord;
CREATE PROCEDURE insertTakeTrainTecord()
BEGIN
	DECLARE tmpPCardID char(18);
	DECLARE tmpTID int;
	DECLARE tmpSStationID int;
	DECLARE tmpAStationId int;
	DECLARE tmpCarriage smallint;
	DECLARE tmpSeatRow smallint;
	DECLARE tmpSeatNo CHAR(1);
	DECLARE tmpSStatus int;
	DECLARE cnt INT;
	SET cnt = 0;
	while cnt < 10000 DO
		SET tmpTID = FLOOR(1+RAND()*10000);
		SET tmpCarriage = ROUND(RAND()*18);
		SET tmpSeatRow = ROUND(RAND()*17);
		SET tmpSeatNo = CHAR(ROUND(RAND()*4+65,0));
		IF(tmpSeatNo = 'D') THEN
			SET tmpSeatNo = 'F';
		END IF;
		SET tmpSStatus = ROUND(RAND()*2,0);
		SELECT PCardID INTO tmpPCardID FROM passenger ORDER BY RAND() LIMIT 1;
		SELECT SStationID,AStationID INTO tmpSStationID,tmpAStationId FROM train where (TID = tmpTID) LIMIT 1;
		INSERT INTO TakeTrainRecord (PCardID,TID,SStationID,AStationID,CarrigeID,SeatRow,SeatNo,SStatus) VALUES(tmpPCardID,tmpTID,tmpSStationID,tmpAStationId,tmpCarriage,tmpSeatRow,tmpSeatNo,tmpSStatus);
		SET cnt = cnt+1;
	END WHILE;
END $$;
delimiter;

CALL insertTakeTrainTecord();
DROP TABLE IF EXISTS diff;
CREATE TABLE diff as (SELECT TID FROM TakeTrainRecord GROUP BY TID,CarrigeID,SeatRow,SeatNo HAVING COUNT(*)>1);
DELETE FROM TakeTrainRecord WHERE TID IN (SELECT TID FROM diff);
DROP TABLE diff;

/*
诊断表【诊断编号，病人身份证号，诊断日期，诊断结果，发病日期】
DiagnoseRecord (DID int, PCardID char(18), DDay date, DStatus smallint, FDay date)
其中，主码为DID；DStatus包括：1：新冠确诊；2：新冠疑似；3：排除新冠
*/
DROP TABLE IF EXISTS `DiagnoseRecord`;
CREATE TABLE DiagnoseRecord(
	DID int PRIMARY KEY AUTO_INCREMENT, 
	PCardID char(18), 
	DDay date NOT NULL, 
	DStatus smallint CHECK (DStatus >= 1 AND DStatus <= 3), 
	FDay date,
	FOREIGN KEY (PCardID) REFERENCES passenger (PCardID) ON DELETE RESTRICT ON UPDATE RESTRICT
)ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP PROCEDURE IF EXISTS insertDiagnoseRecored;
CREATE PROCEDURE insertDiagnoseRecored()
BEGIN
	DECLARE tmpPCardID char(18);
	DECLARE tmpDDay date;
	DECLARE tmpDStatus smallint;
	DECLARE tmpFDay date;
	DECLARE cnt int;
	SET cnt = 0;
	WHILE cnt < 1000 DO
		SELECT PCardID INTO tmpPCardID FROM passenger ORDER BY RAND() LIMIT 1;
		SET tmpDDay = date_add('2020-01-14',INTERVAL FLOOR(RAND()*30) DAY);
		SET tmpDStatus = FLOOR(1 + RAND()*3);
		IF(tmpDStatus = 3) THEN
			SET tmpFDay = NULL; 
		ELSE 
			SET tmpFDay = date_sub(tmpDDay,INTERVAL FLOOR(RAND()*14) DAY);
		END IF;
		SET cnt = cnt + 1;
		INSERT INTO DiagnoseRecord(PCardID,DDay,DStatus,Fday) VALUES(tmpPCardID,tmpDDay,tmpDStatus,tmpFDay);
	END WHILE;
END;

CALL insertDiagnoseRecored();
DROP TABLE IF EXISTS diff;
CREATE TABLE diff as (SELECT PCardID FROM DiagnoseRecord GROUP BY PCardID HAVING COUNT(*)>1);
DELETE FROM TakeTrainRecord WHERE TID IN (SELECT TID FROM diff);
DROP TABLE diff;


/*
乘客紧密接触者表【接触日期, 被接触者身份证号，状态，病患身份证号】
TrainContactor (CDate date, CCardID char(18), DStatus smallint, PCardID char(18))
其中，主码为全码。DStatus包括：1：新冠确诊；2：新冠疑似；3：排除新冠
*/
DROP TABLE IF EXISTS `traincontactor`;
CREATE TABLE traincontactor(
	CDate date, 
	CCardID char(18),
	DStatus smallint,
	PCardID char(18),
	PRIMARY KEY(CDate,CCardID,DStatus,PCardID),
	FOREIGN KEY (PCardID) REFERENCES diagnoserecord (PCardID) ON DELETE RESTRICT ON UPDATE RESTRICT,
	FOREIGN KEY (CCardID) REFERENCES passenger (PCardID) ON DELETE RESTRICT ON UPDATE RESTRICT
)ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP PROCEDURE IF EXISTS insertTrainContactor;
CREATE PROCEDURE insertTrainContactor()
BEGIN
	DECLARE tmpDate date;
	DECLARE tmpCCardID char(18);
	DECLARE tmpDStatus smallint;
	DECLARE tmpPCardID char(18);
	DECLARE cnt INT;
	SET cnt = 0;
	WHILE cnt < 1000 DO
	SELECT PCardID INTO tmpPCardID FROM DiagnoseRecord WHERE(DStatus=1) ORDER BY RAND() LIMIT 1;
	SET tmpDate=date_add('2020-01-18', interval FLOOR(RAND()*30) day);
	SELECT PCardID INTO tmpCCardID FROM passenger ORDER BY RAND() LIMIT 1;
	SET tmpDStatus = IF((SELECT DStatus FROM DiagnoseRecord WHERE DiagnoseRecord.PCardID=tmpCCardID),(SELECT DStatus FROM DiagnoseRecord WHERE DiagnoseRecord.PCardID=tmpCCardID),FLOOR(1+RAND()*3));
	SET cnt = cnt + 1;
	INSERT INTO TrainContactor(CDate,CCardID,DStatus,PCardID) VALUES(tmpDate,tmpCCardID,tmpDStatus,tmpPCardID);
	END WHILE;
END;

CALL insertTrainContactor();
DROP TABLE IF EXISTS diff; 
CREATE TABLE diff as (SELECT CCardID FROM TrainContactor GROUP BY CCardID HAVING COUNT(*)>1);
DELETE FROM TrainContactor WHERE CCardID IN (SELECT CCardID FROM diff);
DROP TABLE diff;

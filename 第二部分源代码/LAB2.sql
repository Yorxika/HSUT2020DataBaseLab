/*分别用一条sql语句完成对乘车记录表基本的增、删、改的操作*/
INSERT INTO TakeTrainRecord VALUES(11,'65432319991017375X',630,1599,1621,7,2,'C',2);
UPDATE TakeTrainRecord SET SStatus = 1 where RID = 11;
DELETE FROM TakeTrainRecord WHERE RID = 11;

/*将乘车记录表中的从武汉出发的乘客的乘车记录插入到一个新表WH_TakeTrainRecord中*/
CREATE TABLE WH_TakeTrainRecord AS (SELECT * FROM TakeTrainRecord where SStationID IN (SELECT SID FROM station WHERE CityName = '武汉');

/*数据导入导出*/
SELECT * FROM WH_TakeTrainRecord INTO OUTFILE "D:/1.txt";
set global local_infile = 1;
LOAD DATA LOCAL INFILE "D:/1.txt" INTO TABLE WH_TakeTrainRecord;

/*观察性实验*/
DROP TABLE IF EXISTS ob;
CREATE TABLE ob(
	a int,
	b int,
	c int
);
INSERT INTO ob VALUES (1,2,3);
INSERT INTO ob VALUES (1,2,3);
INSERT INTO ob VALUES (1,2,3);

/*创建一个新冠确诊病人的乘火车记录视图，其中的属性包括：身份证号、姓名、年龄、乘坐列车编号、发车日期、车厢号，席位排号，席位编号。按身份证号升序排序，如果身份证号一致，按发车日期降序排序（注意，如果病人买了票但是没坐车，不计入在内）。*/
CREATE VIEW digTakeRecord AS(
SELECT
	passenger.PCardID,
	passenger.PName,
	passenger.Age,
	taketrainrecord.TID,
	train.STime,
	taketrainrecord.CarrigeID, 
	taketrainrecord.SeatRow,
	taketrainrecord.SeatNo
FROM
	passenger,taketrainrecord,train
WHERE
	passenger.PCardID IN (SELECT PCardID FROM DiagnoseRecord WHERE DStatus = 1) AND taketrainrecord.SStatus = 1 AND taketrainrecord.PCardID = passenger.PCardID AND taketrainrecord.TID = train.TID
ORDER BY passenger.PCardID,STime DESC
);

/*编写一个触发器，用于实现以下完整性控制规则：
1) 当新增一个确诊患者时，若该患者在发病前14天内有乘车记录，则将其同排及前后排乘客自动加入“乘客紧密接触者表”，其中：接触日期为乘车日期。
2) 当一个紧密接触者被确诊为新冠时，从“乘客紧密接触者表”中修改他的状态为“1”。
*/
DROP TRIGGER IF EXISTS DIGTRIGGER;
CREATE TRIGGER DIGTRIGGER BEFORE INSERT ON DiagnoseRecord
FOR EACH ROW
BEGIN
	IF(NEW.DStatus = 1)
	AND(EXISTS(SELECT train.TID FROM TakeTrainRecord,train WHERE taketrainrecord.Tid = train.TID AND new.PCardID = taketrainrecord.PCardID AND train.SDate >= date_sub(NEW.FDay, interval 14 day) AND train.SDate <= NEW.FDay))
	THEN
		BEGIN 
			DECLARE tmpPCardID char(18);
			DECLARE tmpTID int;
			DECLARE tmpSDate date;
			DECLARE tmpCarriageID smallint;
			DECLARE tmpSeatRow smallint;
			
			DECLARE paPCardID char(18);
			DECLARE paTID int;
			DECLARE paCarriageID smallint;
			DECLARE paSeatRow smallint;
			DECLARE paDStatus smallint;
			DECLARE paSStatus int;
			
			-- 定义结束标志
			DECLARE done INT DEFAULT FALSE;
			DECLARE edone INT DEFAULT FALSE;
			
			-- 定义游标为患者14天的乘车记录
			DECLARE DigCursor CURSOR FOR SELECT PCardID,TakeTrainRecord.TID,train.SDate ,CarrigeID,SeatRow FROM TakeTrainRecord,train WHERE NEW.PCardID = TakeTrainRecord.PCardID AND train.TID = TakeTrainRecord.TID AND train.SDate >= date_sub(NEW.FDay, interval 14 day) AND train.SDate <= NEW.FDay;
			
			DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;-- 结束标识
		
			-- 打开游标
			OPEN DigCursor;
				loop1: LOOP -- 循环游标开始
					FETCH DigCursor INTO tmpPCardID,tmpTID,tmpSDate,tmpCarriageID,tmpSeatRow;
					IF done THEN LEAVE loop1;
					END IF;
					BEGIN
						-- 定义乘客游标为同乘坐一辆车的
						DECLARE paCursor CURSOR FOR SELECT PCardID, CarrigeID, SeatRow, SStatus FROM TakeTrainRecord WHERE TID = tmpTID;
						
						DECLARE CONTINUE HANDLER FOR NOT FOUND SET edone = 1; # 结束标识
						
						-- 打开游标
						OPEN paCursor;
							inner_loop: Loop
								FETCH paCursor INTO paPCardID,paCarriageID,paSeatRow,paSStatus;
								IF edone THEN LEAVE inner_loop;
								ELSE
									BEGIN
										IF(EXISTS(SELECT * FROM DiagnoseRecord WHERE PCardID = paPCardID))
										THEN SET paDStatus  = 1;
										ELSE SET paDStatus = 2;
										END IF;
										
										IF (paPCardID <> tmpPCardID AND paCarriageID = tmpCarriageID AND ABS(tmpSeatRow - paSeatRow) <= 1 AND paSStatus = 1)
										THEN INSERT INTO TrainContactor VALUES(tmpSDate,paPCardID,paDStatus,tmpPCardID);
										END IF;
									END;
								END IF;
							END LOOP;
						CLOSE paCursor;  -- 关闭内层游标
					SET edone = FALSE; -- 内循环复位 以便再次循环
					END;
				END LOOP;  -- 结束循环
			CLOSE DigCursor;  -- 关闭游标
		END;
	END IF;
END;
		
DROP TRIGGER IF EXISTS CONTACTTRIGGER;
CREATE TRIGGER CONTACTTRIGGER AFTER UPDATE ON DiagnoseRecord FOR EACH ROW 
BEGIN
	IF(NEW.DStatus = 1)
	THEN
		UPDATE TrainContactor SET DStatus = 1 WHERE(TrainContactor.CCardID = NEW.PCardID);
	END IF;
END;

/*测试数据*/
INSERT INTO taketrainrecord (PCardID,TID,SStationID,AStationID,CarriageID,SeatRow,SeatNo,SStatus)VALUES ('110101200004054254',19617,1556,404,2,13,'A',1);
INSERT INTO Diagnoserecord(PCardID,DDay,DStatus,FDay) VALUES ('429021194203080538','2020-01-23',1,'2020-01-22');

/*查询确诊者“张三”的在发病前14天内的乘车记录*/
SELECT TakeTrainRecord.*,passenger.PName FROM TakeTrainRecord,train,DiagnoseRecord,passenger WHERE TakeTrainRecord.TID = train.TID AND DiagnoseRecord.PCardID=TakeTrainRecord.PCardID AND passenger.PCardID = DiagnoseRecord.PCardID AND passenger.PName= '张三' AND DiagnoseRecord.DStatus = 1 AND train.SDate <= DiagnoseRecord.FDay AND train.SDate >= DATE_SUB(DiagnoseRecord.FDay,INTERVAL 14 DAY);

/*查询所有从城市“武汉”出发的乘客乘列车所到达的城市名； */
SELECT DISTINCT station.CityName FROM station,TakeTrainRecord WHERE station.SID=TakeTrainRecord.AStationID AND TakeTrainRecord.SStationID IN(SELECT station.SID FROM station WHERE station.CityName='武汉');
		
/*计算每位新冠患者从发病到确诊的时间间隔（天数）及患者身份信息，并将结果按照发病时间天数的降序排列；*/
SELECT DATEDIFF(DiagnoseRecord.DDay,DiagnoseRecord.FDay) AS DayInterval ,passenger.* FROM passenger,DiagnoseRecord WHERE passenger.PCardID = DiagnoseRecord.PCardID AND DiagnoseRecord.DStatus= 1 ORDER BY DayInterval DESC;

/*查询“2020-01-22”从“武汉”发出的所有列车；*/
SELECT train.* FROM train WHERE train.SDate='2020-01-22'  AND train.SStationID IN (SELECT DISTINCT SID FROM station WHERE station.CityName= '武汉');

/*查询“2020-01-22”途经“武汉”的所有列车；*/
SELECT DISTINCT train.* FROM train,trainpass WHERE train.SDate='2020-01-22' AND train.TID = trainpass.TID AND trainpass.SID IN (SELECT DISTINCT SID FROM station WHERE station.CityName= '武汉');

/*查询“2020-01-22”从武汉离开的所有乘客的身份证号、所到达的城市、到达日期；*/
SELECT passenger.PCardID,station.CityName,train.ATime FROM passenger,TakeTrainRecord,station,train WHERE passenger.PCardID = TakeTrainRecord.PCardID AND station.SID= TakeTrainRecord.AStationID AND train.TID = TakeTrainRecord.TID AND TakeTrainRecord.TID IN(SELECT DISTINCT train.TID FROM train WHERE train.SDate='2020-01-22'  AND train.SStationID IN (SELECT DISTINCT SID FROM station WHERE station.CityName= '武汉'));

/*统计“2020-01-22” 从武汉离开的所有乘客所到达的城市及达到各个城市的武汉人员数*/
SELECT station.CityName,count(CityName) AS numbers FROM passenger,TakeTrainRecord,station,train WHERE passenger.PCardID = TakeTrainRecord.PCardID AND station.SID = TakeTrainRecord.AStationID AND train.TID = TakeTrainRecord.TID AND TakeTrainRecord.TID IN(SELECT DISTINCT train.TID FROM train WHERE train.SDate='2020-01-22' AND train.SStationID IN (SELECT DISTINCT SID FROM station WHERE station.CityName= '武汉')) GROUP BY(CityName);

/*查询2020年1月到达武汉的所有人员*/
SELECT DISTINCT passenger.* FROM passenger,TakeTrainRecord,station,train WHERE passenger.PCardID=TakeTrainRecord.PCardID AND TakeTrainRecord.AStationID IN (SELECT DISTINCT SID FROM station WHERE station.CityName= '武汉') AND TakeTrainRecord.TID = train.TID AND train.ATime >= '2020-01-01' AND train.ATime < '2020-02-01';

/*查询2020年1月乘车途径武汉的外地人员（身份证非“420”开头）；*/
SELECT DISTINCT passenger.* FROM passenger,train,trainpass,TakeTrainRecord WHERE passenger.PCardID=TakeTrainRecord.PCardID AND LEFT(passenger.PCardID,3) != '420' AND trainpass.TID = train.TID AND trainpass.SID IN (SELECT DISTINCT SID FROM station WHERE station.CityName= '武汉')  AND TakeTrainRecord.TID = train.TID AND train.STime >='2020-01-01' AND train.STime < '2020-02-01';

/*统计“2020-01-22”乘坐过‘G007’号列车的新冠患者在火车上的密切接触乘客人数（每位新冠患者的同车厢人员都算同车密切接触）。*/
SELECT count(DISTINCT PCardID) AS counts FROM TakeTrainRecord WHERE (TakeTrainRecord.TID,TakeTrainRecord.CarrigeID) IN(SELECT TakeTrainRecord.TID,TakeTrainRecord.CarrigeID FROM passenger,TakeTrainRecord,train,DiagnoseRecord WHERE TakeTrainRecord.TID = train.TID AND train.ATime = '2020-01-22' AND train.TName = 'G007' AND passenger.PCardID = TakeTrainRecord.PCardID AND DiagnoseRecord.PCardID = passenger.PCardID AND DiagnoseRecord.DStatus = 1);

/*查询一趟列车的一节车厢中有3人及以上乘客被确认患上新冠的列车名、出发日期，车厢号； */
SELECT train.TName,train.SDate,TakeTrainRecord.CarrigeID FROM train,TakeTrainRecord,DiagnoseRecord WHERE train.TID = TakeTrainRecord.TID AND TakeTrainRecord.PCardID = DiagnoseRecord.PCardID AND DiagnoseRecord.DStatus = 1 GROUP BY train.TName,train.SDate,TakeTrainRecord.CarrigeID HAVING count(*)>=3; 

/*查询没有感染任何周边乘客的新冠乘客的身份证号、姓名、乘车日期；*/
SELECT passenger.PCardID,passenger.PName,train.SDate FROM train,TakeTrainRecord,DiagnoseRecord,passenger WHERE train.TID = taketrainrecord.TID AND passenger.PCardID = TakeTrainRecord.PCardID AND TakeTrainRecord.PCardID = DiagnoseRecord.PCardID AND DiagnoseRecord.DStatus = 1 AND (train.TName,train.SDate,TakeTrainRecord.CarrigeID) IN (SELECT train.TName,train.SDate,TakeTrainRecord.CarrigeID FROM train,TakeTrainRecord,DiagnoseRecord WHERE train.TID = TakeTrainRecord.TID AND TakeTrainRecord.PCardID = DiagnoseRecord.PCardID AND DiagnoseRecord.DStatus = 1 GROUP BY train.TName,train.SDate,TakeTrainRecord.CarrigeID HAVING count(*)=1) GROUP BY PCardID ORDER BY SDate DESC;
	
/*查询到达 “北京”、或“上海”，或“广州”（即终点站）的列车名，要求where子句中除了连接条件只能有一个条件表达式*/
SELECT DISTINCT train.TName FROM train WHERE train.AStationID IN (SELECT station.SID FROM station WHERE station.CityName = '北京' OR station.CityName = '上海' OR station.CityName = '广州');

/*查询“2020-01-22”从“武汉站”出发，然后当天换乘另一趟车的乘客身份证号和首乘车次号，结果按照首乘车次号降序排列，同车次则按照乘客身份证号升序排列；*/

SELECT train.TID,passenger.PCardID FROM train,passenger,TakeTrainRecord WHERE passenger.PCardID = TakeTrainRecord.PCardID AND TakeTrainRecord.TID = train.TID AND train.TID IN(SELECT train.TID FROM train WHERE train.SDate='2020-01-22' AND train.SStationID IN (SELECT DISTINCT SID FROM station WHERE station.SName= '武汉')) AND(( TakeTrainRecord.AStationID,passenger.PCardID) IN (SELECT TakeTrainRecord.SStationID,passenger.PCardID FROM train,passenger,TakeTrainRecord WHERE passenger.PCardID = TakeTrainRecord.PCardID AND TakeTrainRecord.TID = train.TID AND train.TID IN (SELECT train.TID FROM train WHERE train.SDate='2020-01-22' ))) ORDER BY train.TID DESC,PCardID;

/*查询所有新冠患者的身份证号，姓名及其2020年以来所乘坐过的列车名、发车日期，要求即使该患者未乘坐过任何列车也要列出来；*/
SELECT tmp.PCardID,PName,Tname,SDate FROM (SELECT passenger.PCardID,PName FROM passenger,DiagnoseRecord WHERE passenger.PCardID = DiagnoseRecord.PCardID AND DiagnoseRecord.DStatus = 1) tmp LEFT JOIN (SELECT TName,SDate,PCardID FROM TakeTrainRecord,train WHERE TakeTrainRecord.TID = train.TID AND Train.SDate >= '2020-01-01' ) temp ON tmp.PCardID = temp.PCardID ORDER BY tmp.PCardID,temp.SDate DESC;

/*查询所有发病日期相同而且确诊日期相同的病患统计信息，包括：发病日期、确诊日期和患者人数，结果按照发病日期降序排列的前提下再按照确诊日期降序排列。*/
SELECT FDay,DDay,count(*) AS total FROM DiagnoseRecord WHERE DStatus = 1 GROUP BY FDay,DDay ORDER BY FDay DESC,DDay DESC;
CREATE DEFINER=`fantel`@`%` PROCEDURE `spNewOption`(
	IN Latitude DECIMAL(20, 13),
	IN Longitude DECIMAL(20, 13),
	IN UserID VARCHAR(20),
	IN DateTagged VARCHAR(20),
	IN DateUntagged VARCHAR(20),
    IN Bearing DECIMAL(10, 7),
    IN Tilt DECIMAL(10, 7),
    IN Zoom DECIMAL(10, 7)
)
BEGIN	
	IF (DateUntagged = '') THEN
		INSERT INTO `fantel`.`FantelOptions` (`Latitude`, `Longitude`, `UserID`, `DateTagged`, `Bearing`, `Tilt`, `Zoom`) 
		VALUES (Latitude, Longitude, UserID, STR_TO_DATE(DateTagged, '%Y-%m-%d %H:%i:%s'), Bearing, Tilt, Zoom);
	ELSE
		INSERT INTO `fantel`.`FantelOptions` (`Latitude`, `Longitude`, `UserID`, `DateTagged`, `DateUntagged`, `Bearing`, `Tilt`, `Zoom`)  
		VALUES (Latitude, Longitude, UserID, STR_TO_DATE(DateTagged, '%Y-%m-%d %H:%i:%s'), STR_TO_DATE(DateUntagged, '%Y-%m-%d %H:%i:%s'), Bearing, Tilt, Zoom);
	END IF;
    
END
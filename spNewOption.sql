DELIMITER $$
CREATE DEFINER=`fantel`@`%` PROCEDURE `spNewOption`(
	IN Latitude DECIMAL(20, 13),
	IN Longitude DECIMAL(20, 13),
	IN UserID VARCHAR(20),
	IN DateTagged VARCHAR(20),
	IN DateUntagged VARCHAR(20)
)
BEGIN	
	IF (DateUntagged = '') THEN
		INSERT INTO `fantel`.`FantelOptions` (`Latitude`, `Longitude`, `UserID`, `DateTagged`) 
		VALUES (Latitude, Longitude, UserID, STR_TO_DATE(DateTagged, '%Y-%m-%d %H:%i:%s'));
	ELSE
		INSERT INTO `fantel`.`FantelOptions` (`Latitude`, `Longitude`, `UserID`, `DateTagged`, `DateUntagged`) 
		VALUES (Latitude, Longitude, UserID, STR_TO_DATE(DateTagged, '%Y-%m-%d %H:%i:%s'), STR_TO_DATE(DateUntagged, '%Y-%m-%d %H:%i:%s'));
	END IF;
    
END$$
DELIMITER ;

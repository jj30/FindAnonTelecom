DELIMITER $$
CREATE DEFINER=`fantel`@`%` PROCEDURE `spNewOption`(
	IN UserID VARCHAR(20),
	IN Latitude DECIMAL(20, 13),
	IN Longitude DECIMAL(20, 13)
)
BEGIN	
	INSERT INTO `fantel`.`FantelOptions` (`Latitude`, `Longitude`, `UserID`, `DateTagged`) 
    VALUES (Latitude, Longitude, UserID, NOW());
END$$
DELIMITER ;

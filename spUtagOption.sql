DELIMITER $$
CREATE DEFINER=`fantel`@`%` PROCEDURE `spUtagOption`(
	IN UntagGlobalID VARCHAR(20)	
)
BEGIN	
	UPDATE `fantel`.`FantelOptions` 
    SET DateUntagged = NOW()
    WHERE GlobalID = UntagGlobalID;
END$$
DELIMITER ;

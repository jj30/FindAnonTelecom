CREATE DEFINER=`fantel`@`%` PROCEDURE `spDistance`(
	IN onelat DECIMAL(20, 13),
	IN onelong DECIMAL(20, 13)
)
BEGIN
	-- https://www.scribd.com/presentation/2569355/Geo-Distance-Search-with-MySQL
	DECLARE PI DECIMAL(20, 13);
	SET PI = 3.14159265359;
	SELECT *, 
		3959 * 2 * 
        ASIN(
			SQRT(
				POWER(
					SIN((onelat - ABS(FantelOptions.Latitude)) * (PI / 180) / 2), 
                    2)
                + COS(onelat * (PI / 180)) 
                * COS(ABS(FantelOptions.Latitude) * (PI / 180)) 
                * POWER(SIN((onelong - FantelOptions.Longitude) * (PI / 180) / 2), 2)
                )) as distance
	FROM FantelOptions;
    -- HAVING distance < 50; 
END

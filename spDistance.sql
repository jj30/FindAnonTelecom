CREATE DEFINER=`fantel`@`%` PROCEDURE `spDistance`(
	IN drawing INTEGER,
	IN onelat DECIMAL(20, 13),
	IN onelong DECIMAL(20, 13)
)
BEGIN
	-- https://www.scribd.com/presentation/2569355/Geo-Distance-Search-with-MySQL
	DECLARE PI DECIMAL(20, 13);
	SET PI = 3.14159265359;
	SELECT *, 
		3956 * 2 * 
        ASIN(
			SQRT(
				POWER(
					SIN((onelat - ABS(FantelOptions.Latitude)) * (PI / 180) / 2), 
                    2)
                + COS(onelat * (PI / 180)) 
                * COS(ABS(FantelOptions.Latitude) * (PI / 180)) 
                * POWER(SIN((onelong - FantelOptions.Longitude) * (PI / 180) / 2), 2)
                )) as Distance
	FROM FantelOptions
    WHERE (DateUntagged IS NULL) OR (drawing = 0); -- HAVING distance < 50; 
END

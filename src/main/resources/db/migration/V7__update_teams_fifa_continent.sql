UPDATE teams SET fifa_continent = 
    CASE 
        WHEN "division_id" >= 1 AND "division_id" <= 8 THEN 'Europe'
        WHEN "division_id" = 13 THEN 'SouthAmerica'
        WHEN "division_id" = 14 THEN 'Oceania'
        WHEN "division_id" >= 15 AND "division_id" <= 19 THEN 'Asia'
        WHEN "division_id" >= 9 AND "division_id" <= 12 THEN 'NorthAmerica'
        WHEN "division_id" >= 20 AND "division_id" <= 24 THEN 'Africa'
    END;

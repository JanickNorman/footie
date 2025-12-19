-- Create teams table for World Cup draw simulation
CREATE TABLE teams (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(6) NOT NULL UNIQUE,
    zone VARCHAR(50),
    division VARCHAR(50),
    division_id INT,
    fifa_continent VARCHAR(50),
    team_type VARCHAR(10),
    flag_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INSERT INTO teams ("name", zone_id, zone, division, team_type, code, flag_url) VALUES

-- Create index on pot for faster queries
CREATE INDEX idx_teams_code ON teams(code);

-- Create index on continent for faster queries
CREATE INDEX idx_teams_fifa_continent ON teams(fifa_continent);

-- Create index on continent for faster queries
CREATE INDEX idx_teams_zone ON teams(zone);
CREATE INDEX idx_teams_division ON teams(division);

-- Create teams table for World Cup draw simulation
CREATE TABLE teams (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(3) NOT NULL UNIQUE,
    continent VARCHAR(50) NOT NULL,
    division VARCHAR(50),
    flag_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on pot for faster queries
CREATE INDEX idx_teams_code ON teams(code);

-- Create index on continent for faster queries
CREATE INDEX idx_teams_continent ON teams(continent);

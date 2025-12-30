# Quick Start Guide - Match Events with Kafka

## Prerequisites
- Docker Desktop installed and running
- Java 25 (or Java 17+)
- Node.js 16+ (for frontend)
- Maven

## Step-by-Step Setup

### 1. Start Kafka (Required)

```bash
# Navigate to project root
cd d:\User\Projects\12FootballJava\footie

# Start Kafka with Docker Compose
docker-compose up -d

# Verify services are running
docker-compose ps

# Expected output:
# NAME        IMAGE                              STATUS
# kafka       confluentinc/cp-kafka:7.5.0        Up
# kafka-ui    provectuslabs/kafka-ui:latest      Up
# zookeeper   confluentinc/cp-zookeeper:7.5.0    Up
```

### 2. Start Backend (Spring Boot)

```bash
# In a new terminal, navigate to project root
cd d:\User\Projects\12FootballJava\footie

# Build and run
./mvnw clean spring-boot:run

# Or on Windows
mvnw.cmd clean spring-boot:run

# Wait for "Started FootieApplication" message
```

### 3. Start Frontend (React)

```bash
# In a new terminal
cd d:\User\Projects\12FootballJava\footie\frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev

# Frontend will be available at http://localhost:3000
```

### 4. Test the Feature

#### Option A: Using the Web UI

1. Open browser: http://localhost:3000/match-simulator
2. Enter team names (e.g., "Barcelona" vs "Real Madrid")
3. Click "Start Match"
4. Watch live events stream in real-time!

#### Option B: Using cURL

```bash
# Start a match and get streaming events
curl -N -X POST http://localhost:8080/api/match/start-and-simulate \
  -H "Content-Type: application/json" \
  -d '{"homeTeam": "Barcelona", "awayTeam": "Real Madrid", "intervalMillis": "500"}'

# You'll see events streaming like:
# data:{"matchId":"...","minute":0,"eventType":"KICKOFF",...}
# data:{"matchId":"...","minute":5,"eventType":"POSSESSION",...}
# data:{"matchId":"...","minute":23,"eventType":"GOAL",...}
```

### 5. Monitor Kafka (Optional)

Open Kafka UI in browser: http://localhost:8090

- View topics: You should see `match-events` topic
- Browse messages: Click on topic to see all events
- Monitor consumers: Check `match-events-group` consumer group

## API Endpoints Reference

### Start a match
```bash
POST http://localhost:8080/api/match/start
Body: {"homeTeam": "Barcelona", "awayTeam": "Real Madrid"}
```

### Simulate real-time (slow)
```bash
GET http://localhost:8080/api/match/{matchId}/simulate-realtime?intervalMillis=1000
```

### Simulate fast (all events quickly)
```bash
GET http://localhost:8080/api/match/{matchId}/simulate-fast
```

### Get match state
```bash
GET http://localhost:8080/api/match/{matchId}/state
```

### Get all events for a match
```bash
GET http://localhost:8080/api/match/{matchId}/events
```

## Troubleshooting

### Kafka won't start
```bash
# Check if ports are in use
netstat -ano | findstr :9092
netstat -ano | findstr :2181

# Stop and restart
docker-compose down
docker-compose up -d
```

### Backend connection errors
```bash
# Verify Kafka is running
docker-compose ps

# Check backend logs
./mvnw spring-boot:run

# Look for: "Kafka is not available"
```

### Frontend not showing events
- Check browser console for errors
- Verify backend is running on port 8080
- Check CORS settings in MatchEventController

### No events in Kafka UI
- Wait a few seconds for topics to be created
- Try starting a match first
- Refresh the Kafka UI page

## Stopping Services

```bash
# Stop Kafka
docker-compose down

# Stop backend (Ctrl+C in terminal)

# Stop frontend (Ctrl+C in terminal)
```

## Architecture Overview

```
Frontend (React)
    ↓ HTTP POST /api/match/start
Spring Boot Backend
    ↓ Produces event
Kafka Topic (match-events)
    ↓ Consumes event
Consumer Service
    ↓ Logs & Stores
Match Events Store
    ↓ HTTP GET /api/match/events
Frontend (Display)
```

## What to Expect

When you start a match, you'll see:

1. **Kickoff** (minute 0)
2. **Random events** throughout the match:
   - Goals (2% chance per minute)
   - Shots (6% chance)
   - Fouls (4% chance)
   - Corners (3% chance)
   - Yellow cards (1% chance)
3. **Possession updates** every 5 minutes
4. **Half-time** at minute 45
5. **Second half** from minute 46
6. **Full-time** at minute 90

All events are:
- ✅ Published to Kafka
- ✅ Consumed by the consumer service
- ✅ Logged in backend console
- ✅ Stored in memory
- ✅ Streamed to frontend in real-time

## Next Steps

- Check the full documentation: [KAFKA_MATCH_EVENTS.md](KAFKA_MATCH_EVENTS.md)
- Explore the Kafka UI: http://localhost:8090
- Customize event probabilities in `MatchSimulationService.java`
- Add more event types in `EventType.java`
- Integrate with your existing tournament/match data

Enjoy your real-time football match simulator! ⚽

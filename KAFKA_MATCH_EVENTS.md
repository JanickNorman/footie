# Match Events Kafka Integration

## Overview
This feature implements minute-by-minute match event tracking using Apache Kafka. The system simulates football matches and streams events (goals, fouls, cards, etc.) in real-time through Kafka topics.

## Architecture

### Components

1. **Event Model**
   - `MatchEvent`: Core event data (minute, type, player, score, etc.)
   - `EventType`: Enumeration of event types (GOAL, FOUL, YELLOW_CARD, etc.)
   - `MatchState`: Current state of a match
   - `MatchStatus`: Match phase enumeration

2. **Kafka Producer**
   - `MatchEventProducer`: Sends match events to Kafka topics
   - Publishes events to `match-events` topic

3. **Kafka Consumer**
   - `MatchEventConsumer`: Listens to match events from Kafka
   - Stores events in memory for retrieval
   - Logs significant events (goals, cards, etc.)

4. **Match Simulation**
   - `MatchSimulationService`: Simulates matches minute-by-minute
   - Generates random events based on probabilities
   - Supports both real-time and fast simulation modes

5. **REST API**
   - `MatchEventController`: Exposes endpoints for match operations

## Setup

### 1. Start Kafka using Docker

```bash
# Start Kafka and Zookeeper
docker-compose up -d

# Verify services are running
docker-compose ps

# View logs
docker-compose logs -f kafka
```

This will start:
- Zookeeper on port 2181
- Kafka on port 9092
- Kafka UI on port 8090 (http://localhost:8090)

### 2. Build and Run the Application

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

## API Endpoints

### Start a Match
```bash
POST http://localhost:8080/api/match/start
Content-Type: application/json

{
  "homeTeam": "Barcelona",
  "awayTeam": "Real Madrid"
}
```

Response:
```json
{
  "matchId": "uuid-here",
  "homeTeam": "Barcelona",
  "awayTeam": "Real Madrid",
  "homeScore": 0,
  "awayScore": 0,
  "currentMinute": 0,
  "status": "FIRST_HALF",
  "events": []
}
```

### Simulate Match (Real-time)
```bash
# Streams events minute-by-minute (1 second = 1 minute)
GET http://localhost:8080/api/match/{matchId}/simulate-realtime?intervalMillis=1000
```

### Simulate Match (Fast)
```bash
# Generates all 90 minutes of events quickly
GET http://localhost:8080/api/match/{matchId}/simulate-fast
```

### Start and Simulate in One Call
```bash
POST http://localhost:8080/api/match/start-and-simulate
Content-Type: application/json

{
  "homeTeam": "Barcelona",
  "awayTeam": "Real Madrid",
  "intervalMillis": "500"
}
```

### Get Match State
```bash
GET http://localhost:8080/api/match/{matchId}/state
```

### Get All Active Matches
```bash
GET http://localhost:8080/api/match/active
```

### Get Match Events
```bash
GET http://localhost:8080/api/match/{matchId}/events
```

### Get All Match Events
```bash
GET http://localhost:8080/api/match/events/all
```

### Clear Match Events
```bash
DELETE http://localhost:8080/api/match/{matchId}/events
```

## Usage Examples

### Using cURL

1. **Start a match:**
```bash
curl -X POST http://localhost:8080/api/match/start \
  -H "Content-Type: application/json" \
  -d '{"homeTeam": "Barcelona", "awayTeam": "Real Madrid"}'
```

2. **Start and simulate (Server-Sent Events):**
```bash
curl -N http://localhost:8080/api/match/start-and-simulate \
  -H "Content-Type: application/json" \
  -d '{"homeTeam": "Barcelona", "awayTeam": "Real Madrid", "intervalMillis": "500"}'
```

### Using JavaScript (Frontend)

```javascript
// Start a match
async function startMatch() {
  const response = await fetch('http://localhost:8080/api/match/start', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      homeTeam: 'Barcelona',
      awayTeam: 'Real Madrid'
    })
  });
  return await response.json();
}

// Listen to real-time events using EventSource (SSE)
function simulateMatch(matchId) {
  const eventSource = new EventSource(
    `http://localhost:8080/api/match/${matchId}/simulate-realtime?intervalMillis=1000`
  );

  eventSource.onmessage = (event) => {
    const matchEvent = JSON.parse(event.data);
    console.log(`Minute ${matchEvent.minute}: ${matchEvent.description}`);
    
    if (matchEvent.eventType === 'GOAL') {
      console.log('⚽ GOAL!', matchEvent);
    }
    
    if (matchEvent.eventType === 'FULL_TIME') {
      console.log('Match finished!');
      eventSource.close();
    }
  };

  eventSource.onerror = (error) => {
    console.error('EventSource error:', error);
    eventSource.close();
  };
}

// Start and simulate
async function startAndSimulate() {
  const match = await startMatch();
  simulateMatch(match.matchId);
}
```

## Event Types

| Event Type | Description | Probability |
|------------|-------------|-------------|
| KICKOFF | Match starts | Fixed (minute 0) |
| GOAL | A goal is scored | 2% per minute |
| SHOT_ON_TARGET | Shot towards goal | 3% per minute |
| SHOT_OFF_TARGET | Shot off target | 3% per minute |
| FOUL | Foul committed | 4% per minute |
| CORNER | Corner kick | 3% per minute |
| YELLOW_CARD | Yellow card shown | 1% per minute |
| RED_CARD | Red card shown | ~0.1% per minute |
| POSSESSION | Possession update | Every 5 minutes |
| HALF_TIME | End of first half | Fixed (minute 45) |
| FULL_TIME | End of match | Fixed (minute 90) |

## Monitoring

### Kafka UI
Access Kafka UI at http://localhost:8090 to:
- View topics and messages
- Monitor consumer groups
- Browse messages in `match-events` topic

### Application Logs
The consumer logs all significant events:
```
⚽ GOAL! John Silva scored at minute 23 for Barcelona. Score: 1 - 0
🟨 Yellow card for Michael Garcia (Real Madrid) at minute 45
⏸️ Half-time: Barcelona 1 - 0 Real Madrid
⚽ GOAL! Pedro Lopez scored at minute 67 for Real Madrid. Score: 1 - 1
⏹️ Full-time: Barcelona 1 - 1 Real Madrid
```

## Configuration

Edit `application.properties` to customize:

```properties
# Kafka broker address
spring.kafka.bootstrap-servers=localhost:9092

# Consumer group ID
spring.kafka.consumer.group-id=match-events-group

# Topic name
kafka.topic.match-events=match-events
```

## Stopping Services

```bash
# Stop Kafka and Zookeeper
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Troubleshooting

### Kafka not starting
- Check if ports 9092 or 2181 are already in use
- Verify Docker is running
- Check logs: `docker-compose logs kafka`

### Events not being consumed
- Verify Kafka is running: `docker-compose ps`
- Check consumer group: `spring.kafka.consumer.group-id`
- Verify topic exists in Kafka UI

### Application connection errors
- Ensure `spring.kafka.bootstrap-servers` matches your Kafka address
- Check network connectivity to Kafka broker

## Future Enhancements

- [ ] Persist events to database
- [ ] Real-time dashboard with WebSocket
- [ ] Multiple match formats (league, knockout, etc.)
- [ ] More realistic event probabilities based on team stats
- [ ] Replay functionality
- [ ] Event-driven notifications
- [ ] Integration with existing draw simulator

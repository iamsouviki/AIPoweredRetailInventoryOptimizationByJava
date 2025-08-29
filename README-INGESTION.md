
# Ingestion & ML Step 1–2 Updates

- Removed external Google Calendar API usage for events.
- Added `src/main/resources/data/kolkata_events.events.json` as the event source.
- New components:
  - `EventJsonLoader` – reads the local JSON and maps to `EventData`.
  - `EventIngestionService` – loads events at startup (`@PostConstruct`) and exposes helpers.
  - `EventController` – `GET /events` to view, `POST /events/ingest` to reload.
  - `EventImpactService` – simple event-aware preprocessor that boosts sale quantities on event days by event type.
- `EventData` model extended with `storeId`, `eventType`, `eventName`, `location` fields.
- `LstmModelService` patched to call `eventImpactService.applyEventBoost(sales)` before training.

> This keeps Step 1 fully offline/local and plugs event signal into Step 2 without changing network input shape.

## How to run

1. Ensure MongoDB is running and `spring.data.mongodb.uri` points to your instance.
2. Build and run the Spring Boot app.
3. Visit `GET http://localhost:8080/events` to verify events loaded.
4. Trigger training via your existing `TrainingController` endpoints.


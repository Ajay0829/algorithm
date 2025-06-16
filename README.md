# Market Data Aggregator

A Spring Boot application for fetching and analyzing market data using the Polygon.io API.


## Technologies

- Java
- Spring Boot
- Maven
- Polygon.io API

## Getting Started

### Prerequisites

- Java 17+
- Maven
- Polygon.io API key

### Setup

1. Clone the repository:
    ```
    git clone https://github.com/yourusername/your-repo.git
    cd your-repo
    ```

2. Add your Polygon.io API key to `application.properties`:
    ```
    polygon.api.key=YOUR_API_KEY
    ```

3. Build and run the application:
    ```
    mvn spring-boot:run
    ```

## Project Structure

- `src/main/java/com/market/MarketApplication.java` - Main Spring Boot application
- `src/main/java/com/market/external/polygon/dto/` - Data transfer objects for Polygon API
- `src/main/java/com/market/external/polygon/service/PolygonService.java` - Service for fetching data from Polygon.io
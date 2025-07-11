A RESTFul web service that allows users to create polls, vote, and see live updates in real-time. Secured with JWT and powered by WebSocket for live results.

- Create polls with multiple options

- Vote (one vote per user per poll)

- View poll results (total votes per option)

- Set poll expiry date & time

- Prevent voting after expiry

- Update poll if no votes exist

- JWT-based authentication

- Live poll result updates via WebSocket

- RESTful API with Swagger docs

- Layered architecture (DTO, service, repo)

- Unit tests (service layer)


## Tech Stack

	- Backend (Spring Boot)
	
	- Java 21

	- Spring Boot 3.5.3

	- Spring Security (JWT)

	- Spring WebSocket (STOMP + SockJS)

	- Spring Data JPA

	- H2 (in-memory) or PostgreSQL/MySQL

	- Swagger/OpenAPI (Springdoc)


## Project Structure


	backend/
	├── controller/
	├── service/
	├── dto/
	├── model/
	├── repository/
	├── config/
	├── security/
	└── exception/


## Setup Instructions

	- Clone the repo and navigate into backend/
	
	git clone https://github.com/your-username/polling-app.git
	cd polling-app/backend

	- Build and run
	
	./mvnw spring-boot:run


	- Swagger UI:
	
	http://localhost:8080/swagger-ui.html
	
	

## Authentication Flow (JWT)


	- Register via POST /api/auth/register

	- Login via POST /api/auth/login

	- Receive a token → store in localStorage

	- Use token in Authorization: Bearer <jwt> header for all requests
	
	- Token is also sent in WebSocket headers during connect
	
	
	
##  WebSocket for Live Poll Results


	- Connect to /ws using SockJS

	- STOMP client subscribes to /topic/poll/{pollId}

	- Backend pushes updates whenever a vote is cast

	- Authenticated users only
	
	
## API Endpoints

	| Method | Endpoint             | Description                   |
	| ------ | -------------------- | ----------------------------- |
	| POST   | /api/auth/register   | Register a new user           |
	| POST   | /api/auth/login      | Get JWT token                 |
	| POST   | /api/poll           | Create a new poll             |
	| PUT    | /api/poll/{id}      | Update poll (if no votes yet) |
	| POST   | /api/poll/{id}/vote | Cast a vote                   |
	| GET    | /api/poll/{id}      | Get poll with results         |


## Test

	./mvnw test


## Assumptions

	- One vote per user per poll (enforced at DB layer)

	- Updating a poll is only allowed before any votes

	- WebSocket endpoint is open to any authenticated user



## Contributing

Feel free to fork and improve the project. Pull requests welcome!


## License
This project is open-sourced under the MIT License.

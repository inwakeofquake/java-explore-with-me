# java-explore-with-me
Template repository for ExploreWithMe project.

Project description
The project enables users to organize and participate in various leisure activities.

The API consists of three paths:
1) /admin for moderation,
2) /private for authorized users, and
3) /public for all users.

ExploreWithMe allows users to publish events, send participation requests,
review event compilations prepared by moderators, and search for events, including geographical search.

TECH STACK
ExploreWithMe adopts microservices architecture, comprising a main service and a statistics service
that collects statistics on event views by users. Communication between services is done via HTTP using StatClient.

Both services follow a RESTful design and are built using Spring Boot and Maven.
Each service stores data in a separate PostgreSQL database.

Interaction with the database is facilitated through the ORM framework Hibernate.
QueryDSL is used to consolidate multiple requests.

The entire project is containerized using Docker.

System requirements
JVM installation
Creation of a PostgreSQL database using any PostgreSQL client (e.g., pgAdmin 4)
Docker installation (for building and running containers).

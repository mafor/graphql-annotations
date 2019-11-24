[![codecov](https://codecov.io/gh/mafor/graphql-annotations/branch/master/graph/badge.svg?token=GDPwHlsKi1)](https://codecov.io/gh/mafor/graphql-annotations)
[![Build status](https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:(id:TestDrive_GraphqlPlayground_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TestDrive_GraphqlPlayground_Build)

# GraphQL annotations

GraphQL Java + SpringBoot + WebFlux evaluation. 
Prove of concept of a declarative, annotations based configuration (see [GraphQLHandler](/examples/library/src/main/kotlin/com/chaosonic/graphql/library/GraphQLHandler.kt))
## Starting the application
To start the application localy, run in the project's root folder:

```shell script
./gradlew bootRun
```
To start it on Docker:
```shell script
./gradlew clean bootJar
docker build -t graphql-library examples/library
docker run -d --rm -p 8080:8080 graphql-library
```
The application exposes single GraphQL endpoint on http://localhost:8080/graphql 
## Sample queries
GraphQL schema: [schema.graphqls](examples/library/src/main/resources/graphql/schema.graphqls)
#### list books
GraphQL:
```graphql
query listBooks {
    books {
        id,
        title,
        authors {
            id,
            name
        }
    }
}
```
Curl:
```shell script
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'accept: application/json' \
  --header 'content-type: application/json' \
  --data '{"query":"query listBooks {books {id, title, authors {id, name}}}"}'
```
#### list authors
GraphQL:
```graphql
query listAuthors {
  authors {
    id,
    name,
    books {
      id,
      title
    }
  }
}
```
Curl:
```shell script
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'accept: application/json' \
  --header 'content-type: application/json' \
  --data '{"query":"query listAuthors {authors {id, name, books {id, title}}}"}'
```
#### add a book
GraphQL:
```graphql
mutation createBook {
    addBook(authorIds: ["1"], title: "New book") {
        id
    }
}
```
Curl:
```shell script
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'accept: application/json' \
  --header 'content-type: application/json' \
  --data '{"query":"mutation createBook {addBook(authorIds: [\"1\"], title: \"New book\") {id}}"}'
```
#### remove a book
GraphQL:
```graphql
mutation removeBook {
	removeBook(id: "1")
}
```
Curl:
```shell script
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'accept: application/json' \
  --header 'content-type: application/json' \
  --data '{"query":"mutation removeBook {removeBook(id: \"1\")}"}'
```
#### subscribe to notifications
GraphQL:
```graphql
subscription events {
	events
}
```
Curl:
```shell script
curl -N --request POST \
  --url http://localhost:8080/graphql \
  --header 'accept: text/event-stream' \
  --header 'content-type: application/json' \
  --data '{"query":"subscription events {events}"}'
```
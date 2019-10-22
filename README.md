# GraphQL playground

GraphQL Java + SpringBoot + WebFlux evaluation. 
Prove of concept of a declarative, annotations based configuration (see [GraphQLHandler](/src/main/java/com/chaosonic/graphql/GraphQLHandler.kt))
## Starting the application
To start the application localy, run in the project's root folder:

```shell script
./gradlew bootRun
```
To start it on Docker:
```shell script
./gradlew clean bootJar
docker build -t graphql-playground .
docker run -d --rm -p 8080:8080 graphql-playground
```
The application exposes single GraphQL endpoint on http://localhost:8080/graphql 
## Sample queries
GraphQL schema: [src/main/resources/graphql/schema.graphqls](src/main/resources/graphql/schema.graphqls)
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
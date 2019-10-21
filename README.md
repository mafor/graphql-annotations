# GraphQL playground

Simple GraphQL Java + SpringBoot + WebFlux evaluation. Classic Library app.

## Starting the application

In the project's root folder:

```shell script
./gradlew bootRun
```
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
#!/usr/bin/env bash
cd ../../../../..
./gradlew :app:downloadApolloSchema --endpoint='https://apigateway.dev.limor.ie/graphql/' --schema='app/src/main/graphql/com/limor/app/schema.json'

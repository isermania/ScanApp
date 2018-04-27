FROM openjdk:8

# authors
LABEL LU Refugee Team

# import app content
COPY /Code/ /app

# set working directory
WORKDIR /app/app/build/outputs/apk/debug 

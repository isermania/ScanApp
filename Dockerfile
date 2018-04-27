FROM openjdk:8

# authors
LABEL LU Refugee Team

# set working directory
WORKDIR /app

# import app content
COPY /Code/ /app

RUN cd app/build/outputs/apk/debug
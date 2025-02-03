# FROM postgres:14.1-alpine

# ENV POSTGRES_DB=db \
#    POSTGRES_USER=usr \
#    POSTGRES_PASSWORD=pwd


FROM openjdk:latest
COPY Main.class /Main.class 
WORKDIR /
RUN java Main

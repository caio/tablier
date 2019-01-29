FROM node:latest as build

ENV DEBIAN_FRONTEND noninteractive
ENV LC_ALL C.UTF-8
ENV LANG C.UTF-8

RUN apt-get update -qq && apt-get install -y build-essential
RUN npm install -g gulp

WORKDIR /
ADD package.json /

RUN npm install

WORKDIR /app

EXPOSE 1313
EXPOSE 3001

CMD ["bash"]

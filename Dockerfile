FROM node:latest

RUN apt-get update -qq && apt-get install -y build-essential
RUN apt-get install -y ruby ruby-dev
RUN gem install sass
RUN npm install gulp -g

RUN mkdir /base
WORKDIR /base
ADD app/package.json /base/package.json

RUN npm install

EXPOSE 1313
EXPOSE 3001

WORKDIR /base/app
CMD ["bash", "-c", "/usr/local/bin/gulp serve"]

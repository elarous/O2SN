FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/o2sn.jar /o2sn/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/o2sn/app.jar"]

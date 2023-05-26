FROM openjdk:11
COPY target/uberjar/helcb.jar /helcb/app.jar
COPY prod-config.edn /helcb/config.edn
COPY resources/html/index.html /resources/html/index.html
ADD resources/public/* /resources/
EXPOSE 3000
CMD ["java", "-jar", "-Dconf=/helcb/config.edn", "/helcb/app.jar"]
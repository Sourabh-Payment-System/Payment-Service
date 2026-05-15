FROM dockerfile/java:oracle-java8

VOLUME /tmp
ADD Payment-System.jar /opt/Payment-System/
EXPOSE 8080
WORKDIR /opt/Payment-System/
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-Xms512m", "-Xmx1g", "-jar", "Payment-System.jar"]

FROM java:8
ADD build/libs/* dockerapp.jar
CMD ["java","-jar","dockerapp.jar"]
FROM eclipse-temurin:11-jdk

WORKDIR /app

# Copy source and library files
COPY src ./src
COPY lib ./lib

# Create bin directory for compiled classes
RUN mkdir bin

# Compile the application
RUN find src -name "*.java" > sources.txt
RUN javac -d bin -sourcepath src -cp "lib/mysql-connector-j-8.0.33.jar" @sources.txt

# The server runs on port 8000
EXPOSE 8000

# Command to run the server
CMD ["java", "-cp", "bin:lib/mysql-connector-j-8.0.33.jar", "com.whiteboard.server.WhiteboardServer"]

# Use a base image with Maven and JDK installed
FROM java:8u40-b09-jdk

# Set the working directory inside the container
WORKDIR /app
# Copy the compiled Java application JAR file into the container
COPY target/backend-0.0.1-SNAPSHOT.jar /app/backend-0.0.1-SNAPSHOT.jar

# Command to run the Java application when the container starts
CMD ["java", "-jar", "backend-0.0.1-SNAPSHOT.jar"]

#docker build -t latest .

# # Copy the Maven project descriptor
# COPY pom.xml .
#
# # Fetch dependencies
# RUN mvn -B dependency:go-offline
#
# # Copy the source code
# COPY src ./src
#
# # Build the application
# RUN mvn -B package
#
# # Use a base image with AWS CLI installed
# FROM amazon/aws-cli:latest
#
# # Set the working directory inside the container
# WORKDIR /app
#
# # Copy the JAR file from the build stage
# COPY --from=build /app/target/your-application.jar .

# Set AWS configuration (replace these values with your AWS credentials and ECR details)
# ARG AWS_ACCESS_KEY_ID
# ARG AWS_SECRET_ACCESS_KEY
# ARG AWS_REGION
# ARG ECR_REPO_URL
#
# RUN aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID && \
#     aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY && \
#     aws configure set region $AWS_REGION
#
# # Authenticate Docker to your ECR registry
# RUN aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPO_URL
#
# # Tag the image with the ECR repository URI
# RUN docker tag your-application:latest $ECR_REPO_URL/your-application:latest
#
# # Push the Docker image to ECR
# RUN docker push $ECR_REPO_URL/your-application:latest

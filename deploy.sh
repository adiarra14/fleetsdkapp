#!/bin/bash

# Build the Docker image
docker build -t sdkmaxvision1:latest .

# Tag the image for your registry (replace with your registry details)
docker tag sdkmaxvision1:latest sdkmaxvision1:latest

# Login to your registry (you'll need to provide credentials)
# docker login https://82.112.255.144:9443

# Push the image
# docker push sdkmaxvision1:latest

echo "Image built and pushed successfully!"
echo "Now you can deploy the stack in Portainer at https://82.112.255.144:9443"
echo "Use the docker-compose.yml file to create the stack in Portainer UI" 
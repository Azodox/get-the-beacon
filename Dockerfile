FROM marctv/minecraft-papermc-server:latest

COPY ./build/libs ./plugins
ENV MEMORYSIZE=2G
ENV TZ=Europe/Paris
EXPOSE 25565
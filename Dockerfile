FROM marctv/minecraft-papermc-server:latest

COPY ./worlds/gtb ./world
COPY ./build/libs ./plugins
ENV MEMORYSIZE=2G
ENV TZ=Europe/Paris
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"
EXPOSE 25565
EXPOSE 5005
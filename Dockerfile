FROM openjdk:13-alpine
# to use:
# docker build -t validator/validator .
# docker run -it --rm \
#    -e CONNECTION_TIMEOUT_SECONDS=15 \
#    -e SOCKET_TIMEOUT_SECONDS=15 \
#    -p 8888:8888 \
#    validator/validator
LABEL name="vnu"
LABEL version="dev"
LABEL maintainer="Michael[tm] Smith <mike@w3.org>"
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar .
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar.asc .
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar.sha1 .
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar.sha1.asc .
RUN apk --no-cache add gnupg
RUN gpg --quiet --keyserver ha.pool.sks-keyservers.net --recv-keys 87D17477BC3A4B95 || \
    gpg --quiet --keyserver pgp.mit.edu --recv-keys 87D17477BC3A4B95 || \
    gpg --quiet --keyserver ipv4.pool.sks-keyservers.net --recv-keys 87D17477BC3A4B95 || \
    gpg --quiet --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 87D17477BC3A4B95 || \
    gpg --quiet --keyserver keyserver.ubuntu.com --recv-keys 87D17477BC3A4B95 || \
    gpg --quiet --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 87D17477BC3A4B95 || \
    gpg --quiet --keyserver keyserver.pgp.com --recv-keys 87D17477BC3A4B95
RUN gpg --quiet --verify --trust-model always vnu.jar.asc vnu.jar
RUN gpg --quiet --verify --trust-model always vnu.jar.sha1.asc vnu.jar.sha1
RUN echo "$(cat vnu.jar.sha1)  vnu.jar" | sha1sum -c -
ENV CONNECTION_TIMEOUT_SECONDS 5
ENV SOCKET_TIMEOUT_SECONDS 5
EXPOSE 8888
CMD CONNECTION_TIMEOUT=$((CONNECTION_TIMEOUT_SECONDS * 1000)); \
    SOCKET_TIMEOUT=$((SOCKET_TIMEOUT_SECONDS * 1000)); \
    java \
    -Dnu.validator.servlet.connection-timeout=$CONNECTION_TIMEOUT \
    -Dnu.validator.servlet.socket-timeout=$SOCKET_TIMEOUT \
    -cp vnu.jar nu.validator.servlet.Main 8888

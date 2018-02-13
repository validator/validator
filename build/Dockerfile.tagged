FROM java:8u111-jre-alpine
# to use:
# docker build \
#     --build-arg tag=17.11.1 \
#     -t validator/validator:17.11.1 \
#     -f Dockerfile.tagged .
# docker push validator/validator
ARG tag
ENV TAG=$tag
LABEL name="vnu"
LABEL version=$tag
LABEL maintainer="Michael[tm] Smith <mike@w3.org>"
ENV RELEASE_DOWNLOAD_URL_BASE https://sideshowbarker.net/releases/jar
RUN echo "${RELEASE_DOWNLOAD_URL_BASE}/vnu.jar_$TAG.zip ."
ADD ${RELEASE_DOWNLOAD_URL_BASE}/vnu.jar_${TAG}.zip .
ADD ${RELEASE_DOWNLOAD_URL_BASE}/vnu.jar_${TAG}.zip.asc .
ADD ${RELEASE_DOWNLOAD_URL_BASE}/vnu.jar_${TAG}.zip.sha1 .
ADD ${RELEASE_DOWNLOAD_URL_BASE}/vnu.jar_${TAG}.zip.sha1.asc .
RUN apk --no-cache add gnupg=2.1.12-r0
RUN gpg --quiet --recv-keys 87D17477BC3A4B95
RUN gpg --quiet --verify --trust-model always vnu.jar_${TAG}.zip.asc vnu.jar_${TAG}.zip
RUN gpg --quiet --verify --trust-model always vnu.jar_${TAG}.zip.sha1.asc vnu.jar_${TAG}.zip.sha1
RUN echo "$(cat vnu.jar_$TAG.zip.sha1)  vnu.jar_$TAG.zip" | sha1sum -c -
RUN unzip -o vnu.jar_${TAG}.zip
ENV CONNECTION_TIMEOUT_SECONDS 5
ENV SOCKET_TIMEOUT_SECONDS 5
EXPOSE 8888
CMD CONNECTION_TIMEOUT=$((CONNECTION_TIMEOUT_SECONDS * 1000)); \
    SOCKET_TIMEOUT=$((SOCKET_TIMEOUT_SECONDS * 1000)); \
    java \
    -Dnu.validator.servlet.connection-timeout=$CONNECTION_TIMEOUT \
    -Dnu.validator.servlet.socket-timeout=$SOCKET_TIMEOUT \
    -cp vnu.jar nu.validator.servlet.Main 8888

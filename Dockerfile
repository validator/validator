FROM debian:stable-slim
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
ADD https://github.com/validator/validator/releases/download/linux/vnu.linux.zip .
ADD https://github.com/validator/validator/releases/download/linux/vnu.linux.zip.asc .
ADD https://github.com/validator/validator/releases/download/linux/vnu.linux.zip.sha1 .
ADD https://github.com/validator/validator/releases/download/linux/vnu.linux.zip.sha1.asc .
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install --no-install-recommends -y \
       gnupg=2.1.18-8~deb9u4 \
       dirmngr=2.1.18-8~deb9u4 \
       unzip=6.0-21+deb9u1 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir ~/.gnupg \
    && chmod 700 ~/.gnupg \
    && echo "disable-ipv6" >> ~/.gnupg/dirmngr.conf \
    && gpg --quiet --keyserver ha.pool.sks-keyservers.net --recv-keys 100025026C976B92 || \
       gpg --quiet --keyserver pgp.mit.edu --recv-keys 100025026C976B92 || \
       gpg --quiet --keyserver ipv4.pool.sks-keyservers.net --recv-keys 100025026C976B92 || \
       gpg --quiet --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 100025026C976B92 || \
       gpg --quiet --keyserver keyserver.ubuntu.com --recv-keys 100025026C976B92 || \
       gpg --quiet --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 100025026C976B92 || \
       gpg --quiet --keyserver keyserver.pgp.com --recv-keys 100025026C976B92 \
    && gpg --quiet --verify --trust-model always vnu.linux.zip.asc vnu.linux.zip \
    && gpg --quiet --verify --trust-model always vnu.linux.zip.sha1.asc vnu.linux.zip.sha1 \
    && echo "$(cat vnu.linux.zip.sha1)  vnu.linux.zip" | sha1sum -c - \
    && unzip ./vnu.linux.zip \
    && rm ./vnu.linux.zip* \
    && apt-get purge -y --auto-remove gnupg dirmngr unzip
ENV LANG C.UTF-8
ENV CONNECTION_TIMEOUT_SECONDS 5
ENV SOCKET_TIMEOUT_SECONDS 5
ENV BIND_ADDRESS 0.0.0.0
ENV PATH=/vnu-runtime-image/bin:$PATH
EXPOSE 8888
# hadolint ignore=DL3025
CMD CONNECTION_TIMEOUT=$((CONNECTION_TIMEOUT_SECONDS * 1000)); \
    SOCKET_TIMEOUT=$((SOCKET_TIMEOUT_SECONDS * 1000)); \
    ./vnu-runtime-image/bin/java \
    -Dnu.validator.servlet.connection-timeout=$CONNECTION_TIMEOUT \
    -Dnu.validator.servlet.socket-timeout=$SOCKET_TIMEOUT \
    -Dnu.validator.servlet.bind-address=$BIND_ADDRESS \
    -m vnu/nu.validator.servlet.Main 8888

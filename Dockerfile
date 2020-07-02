FROM debian:stable-slim AS builder
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
       gnupg=2.2.12-1+deb10u1 \
       dirmngr=2.2.12-1+deb10u1 \
       unzip=6.0-23+deb10u1 \
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
ENV JAVA_TOOL_OPTIONS ""
ENV CONNECTION_TIMEOUT_SECONDS 5
ENV SOCKET_TIMEOUT_SECONDS 5
ENV BIND_ADDRESS 0.0.0.0
ENV PATH=/vnu-runtime-image/bin:$PATH
EXPOSE 8888
# hadolint ignore=DL3006
FROM gcr.io/distroless/base
COPY --from=builder /vnu-runtime-image /vnu-runtime-image
COPY --from=builder /lib/x86_64-linux-gnu/libz.so.1 /lib/x86_64-linux-gnu/libz.so.1
CMD ["./vnu-runtime-image/bin/java", "-m", "vnu/nu.validator.servlet.Main", "8888"]

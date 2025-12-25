FROM debian:stable-slim AS builder
# to use:
# docker build -t ghcr.io/validator/validator .
# docker buildx build --platform linux/amd64,linux/arm64 -t ghcr.io/validator/validator .
# docker run -it --rm \
#    -e CONNECTION_TIMEOUT_SECONDS=15 \
#    -e SOCKET_TIMEOUT_SECONDS=15 \
#    -p 8888:8888 \
#    ghcr.io/validator/validator
LABEL name="vnu"
LABEL version="dev"
LABEL maintainer="Michael[tm] Smith <mike@w3.org>"
ARG TARGETPLATFORM
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install --no-install-recommends -y \
        ca-certificates \
        curl \
    && curl -fL https://github.com/validator/validator/releases/download/latest/vnu.jar -o /vnu.jar && \
    apt-get purge -y --auto-remove curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
# hadolint ignore=DL3006
FROM debian:stable-slim
RUN apt-get update && apt-get install --no-install-recommends -y \
       wget \
       default-jre-headless \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /vnu.jar /vnu.jar
ENV LANG=C.UTF-8
ENV JAVA_TOOL_OPTIONS=""
ENV CONNECTION_TIMEOUT_SECONDS=5
ENV SOCKET_TIMEOUT_SECONDS=5
ENV BIND_ADDRESS=0.0.0.0
EXPOSE 8888
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8888/ || exit 1
CMD ["sh", "-c", "java -cp /vnu.jar -Dnu.validator.servlet.bind-address=${BIND_ADDRESS} -Dnu.validator.servlet.connection-timeout=${CONNECTION_TIMEOUT_SECONDS} -Dnu.validator.servlet.socket-timeout=${SOCKET_TIMEOUT_SECONDS} nu.validator.servlet.Main 8888"]

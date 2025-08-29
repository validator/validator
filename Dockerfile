FROM debian:stable-slim AS builder
# to use:
# docker build -t ghcr.io/validator/validator .
# docker run -it --rm \
#    -e CONNECTION_TIMEOUT_SECONDS=15 \
#    -e SOCKET_TIMEOUT_SECONDS=15 \
#    -p 8888:8888 \
#    ghcr.io/validator/validator
LABEL name="vnu"
LABEL version="dev"
LABEL maintainer="Michael[tm] Smith <mike@w3.org>"
ADD https://github.com/validator/validator/releases/download/latest/vnu.linux.zip .
ADD https://github.com/validator/validator/releases/download/latest/vnu.linux.zip.sha1 .
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN apt-get update && apt-get install --no-install-recommends -y \
       unzip=6.0-29 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && echo "$(cat vnu.linux.zip.sha1)  vnu.linux.zip" | sha1sum -c - \
    && unzip ./vnu.linux.zip \
    && rm ./vnu.linux.zip* \
    && apt-get purge -y --auto-remove unzip
# hadolint ignore=DL3006
FROM debian:stable-slim
COPY --from=builder /vnu-runtime-image /vnu-runtime-image
ENV LANG C.UTF-8
ENV JAVA_TOOL_OPTIONS ""
ENV CONNECTION_TIMEOUT_SECONDS 5
ENV SOCKET_TIMEOUT_SECONDS 5
ENV BIND_ADDRESS 0.0.0.0
ENV PATH=/vnu-runtime-image/bin:$PATH
EXPOSE 8888
CMD ["./vnu-runtime-image/bin/java", "-m", "vnu/nu.validator.servlet.Main", "8888"]

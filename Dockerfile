FROM java:8u111-jre-alpine
LABEL name="vnu"
LABEL version="dev"
LABEL maintainer="Michael[tm] Smith <mike@w3.org>"
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar .
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar.asc .
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar.sha1 .
ADD https://sideshowbarker.net/nightlies/jar/vnu.jar.sha1.asc .
RUN apk --no-cache add gnupg=2.1.12-r0
RUN gpg --quiet --recv-keys 87D17477BC3A4B95
RUN gpg --quiet --verify --trust-model always vnu.jar.asc vnu.jar
RUN gpg --quiet --verify --trust-model always vnu.jar.sha1.asc vnu.jar.sha1
RUN echo "$(cat vnu.jar.sha1)  vnu.jar" | sha1sum -c -
EXPOSE 8888
CMD ["java", "-cp", "vnu.jar", "nu.validator.servlet.Main", "8888"]

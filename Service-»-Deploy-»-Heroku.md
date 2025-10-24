The checker can be deployed to Heroku like this:

1. Install the [heroku-deploy
   plugin](https://github.com/heroku/heroku-deploy), which allows you to
   deploy WAR files to Heroku:

   ```bash
   heroku plugins:install https://github.com/heroku/heroku-deploy
   ```

2. Create a new Heroku instance like this:

   ```bash
   heroku create my-vnu-instance
   ```

3. To [minimize memory issues in Java
   applications](https://devcenter.heroku.com/articles/java-memory-issues),
   configure the instance like this :

   ```bash
   heroku config:set \
       JAVA_OPTS="-Xss1024k -XX:+UseCompressedOops" \
       JAVA_TOOL_OPTIONS="-Xmx384m -Xss1024k" \
       --app=my-vnu-instance
   ```

4. Download the [latest WAR release of the
   checker](https://github.com/validator/validator/releases/tag/war):

   ```curl
   curl -O -L https://github.com/validator/validator/releases/download/war/vnu.war
   ```

5. Deploy the WAR file to Heroku:

   ```bash
   heroku deploy:war --war vnu.war --app my-vnu-instance
   ```

6. Done! Your checker instance is ready for you to open in your browser:

   ```bash
   heroku open --app my-vnu-instance
   ```

## Gotchas

* A standard Heroku instance (a "dyno") has 512 Mb of RAM, which might be
  not enough for a production server. Consider upgrading to a bigger
  instance if you're going to use this in production.

* Heroku dynos are shut down after 1 hour of inactivity. They will wake up
  when a new request comes in, but this sometimes can cause a server crash.
  If you want to prevent this by monitoring your instances with Pingdom or
  a similar tool.

* If your Heroku instance ever crashes, you can restart it like that:

  ```bash
  heroku restart --app my-vnu-instance
  ```

* You can check the server logs with:

  ```bash
  heroku logs --tail --app my-vnu-instance
  ```

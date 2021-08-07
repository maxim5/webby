<h1 align="center">
  <br>
  <img src="https://raw.githubusercontent.com/maxim5/webby/readme/doc/spider-lucas.png" alt="Webby" width="600">
  <br>
  Webby
  <br>
</h1>

<h4 align="center">Web Server for Humans</h4>

<p align="center">
  <a href="#key-features">Key Features</a> •
  <a href="#how-to-use">How To Use</a> •
  <a href="#license">License</a> •
  <a href="#credits">Credits</a>
</p>

[comment]: <> (![screenshot]&#40;https://raw.githubusercontent.com/amitmerchant1990/electron-markdownify/master/app/img/markdownify.gif&#41;)

## Key Features

### Reduced Java boilerplate

```java
@GET(url = "/hello/{userId}")
@View(template = "hello_user.ftl")
public Map<String, Object> hello(int userId) {
    return Map.of(
        "user", userManager.getUserById(userId)
    );
}
```

We believe the web-developers should focus on the application and not web/HTTP routine as much as possible.
The snippet above is a working example of a web handler that will accept the incoming HTTP request 
with already extracted URL variables, inject dependencies if necessary, render the template and send the HTTP response.

### Low overhead 

Webby applies a large variety of optimizations to provide the best latency and GC-friendliness: pre-compute, caching,
byte buffer pooling, char buffer reuse and many more.

### Library not a framework

```java
public class Pojo {
    @GET(url="/blog_post/{id}/{slug}")
    public @Json List getBlogPost(int postId, String slug) {
        return List.of(...);
    }
}
```

Webby does not require any code structure, base class inheritance, database, ORM and templating system.
A developer has the freedom to apply the library in any way they want.

## How To Use

```groovy
// settings.gradle
sourceControl {
    gitRepository('https://github.com/maxim5/webby.git') {
        producesModule("io.webby:webby")
    }
}
```

```groovy
// build.gradle
implementation ('io.webby:webby:0.1.0-SNAPSHOT')
```

Webby is in active development, hence the best way to add a Gradle dependency is via `gitRepository` directive.


To start the web server locally:

```java
public class Main {
    public static void main(String[] args) throws Exception {
        AppSettings settings = new AppSettings();
        settings.setWebPath("src/main/resources/web/");

        Webby.nettyBootstrap(settings).runLocally(8080);
    }
}
```

## License

[Apache 2.0](/maxim5/webby/blob/master/LICENSE)


## Credits

Many thanks to [Lucas the Spider](https://www.youtube.com/channel/UCNqRS1gSJFMNPVwye1gyI_g) for the amazing character
which inspired our logo.

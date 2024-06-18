package io.webby.demo.templates;

import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import io.spbx.webby.url.annotate.GET;
import io.spbx.webby.url.annotate.Render;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.url.annotate.View;
import views.HelloRock;

import java.io.InputStream;
import java.util.Map;

@Serve(render = Render.ROCKER)
public class RockerExample {
    @GET(url = "/templates/rocker/hello")
    @View(template = "views/HelloRock.rocker.html")
    public HelloRock hello_bound_template() {
        return HelloRock.template("World");
    }

    @GET(url = "/templates/rocker/hello/model")
    @View(template = "views/HelloRock.rocker.html")
    public Map<String, Object> hello_model() {
        return Map.of("message", "Model");
    }

    @GET(url = "/templates/manual/rocker/hello")
    public Object manual_hello() {
        return HelloRock.template("World").render();  // Default: ArrayOfByteArraysOutput
    }

    @GET(url = "/templates/manual/rocker/hello/string")
    public String manual_hello_string() {
        RockerOutput<?> output = HelloRock.template("String").render();
        // Charset: UTF-8, byte length: 14
        return output.toString();
    }

    @GET(url = "/templates/manual/rocker/hello/stream")
    public InputStream manual_hello_stream() {
        ArrayOfByteArraysOutput output = Rocker.template("views/HelloRock.rocker.html")
                .bind("message", "Stream")
                .render(ArrayOfByteArraysOutput::new);
        // output.getArrays(): Hello, Stream, !
        return output.asInputStream();
    }

    @GET(url = "/templates/manual/rocker/hello/bytes")
    public byte[] manual_hello_bytes() {
        return HelloRock.template("Bytes")
                .render(ArrayOfByteArraysOutput::new)
                .toByteArray();
    }
}

package io.webby.templates;

import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Render;
import io.webby.url.annotate.Serve;
import views.HelloRock;

import java.io.InputStream;

@Serve(render = Render.ROCKER)
public class Rockerz {
    @GET(url = "/templates/rocker/hello")
    public Object hello() {
        // Default: ArrayOfByteArraysOutput
        return HelloRock.template("World").render();
    }

    @GET(url = "/templates/rocker/hello/string")
    public String hello_string() {
        RockerOutput<?> output = HelloRock.template("String").render();
        // Charset: UTF-8, byte length: 14
        return output.toString();
    }

    @GET(url = "/templates/rocker/hello/stream")
    public InputStream hello_stream() {
        ArrayOfByteArraysOutput output = Rocker.template("views/HelloRock.rocker.html")
                .bind("message", "Stream")
                .render(ArrayOfByteArraysOutput::new);
        // output.getArrays(): Hello, Stream, !
        return output.asInputStream();
    }

    @GET(url = "/templates/rocker/hello/bytes")
    public byte[] hello_bytes() {
        return HelloRock.template("Bytes")
                .render(ArrayOfByteArraysOutput::new)
                .toByteArray();
    }
}

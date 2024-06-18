package io.spbx.webby.demo.websockets.lowlevel;

import com.google.inject.Inject;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.spbx.webby.url.annotate.Serve;
import io.spbx.webby.netty.ws.sender.Sender;
import org.jetbrains.annotations.NotNull;

import static io.spbx.webby.netty.ws.sender.Sender.text;

@Serve(url = "/ws/ll/send", websocket = true)
public class LLSending {
    @Inject private Sender sender;

    public void onText(@NotNull TextWebSocketFrame frame) {
        sender.sendFlush(text("Ack %s".formatted(frame.text())));
    }

    public void countDownImmediately(int num) {
        for (int i = num; i > 0; i--) {
            sender.send(text(String.valueOf(i)));
        }
        sender.flush();
    }

    public void countDownViaListener(int num) {
        if (num <= 0) {
            sender.flush();
        } else {
            sender.send(text(String.valueOf(num)))
                    .addListener(__ -> countDownViaListener(num - 1));
        }
    }
}

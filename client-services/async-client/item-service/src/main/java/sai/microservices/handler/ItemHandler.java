package sai.microservices.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sai.microservices.vo.Item;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Slf4j
@Component
public class ItemHandler {

    private Mono getItems(ServerRequest serverRequest) {
        return ServerResponse
                .ok()
//                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(
                        Flux.range(1, new Random().nextInt(10))
                                .map(itemId -> Item.builder()
                                        .itemId("Item-" + itemId.toString())
                                        .orderId(serverRequest.pathVariable("orderId"))
                                        .qty(new Random().nextInt(10))
                                        .price(new Random().nextFloat())
                                        .build()
                                ), Item.class);
    }

    public static void main(String[] args) {
        Flux.interval(Duration.ofSeconds(1)).take(10).map(aLong -> {
            System.out.println(aLong);
            return aLong;
        }).subscribe(System.out::println)
        ;
    }

    @Bean
    public RouterFunction itemRouter(ItemHandler handler) {
        return RouterFunctions.route(GET("item/{orderId}"), handler::getItems);
    }

}

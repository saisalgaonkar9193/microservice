package sai.microservices.handlers;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import sai.microservices.vo.Item;
import sai.microservices.vo.Order;

import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Slf4j
@Component
public class OrderHandler {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private WebClient.Builder webclientBuilder;


    private Mono getOrders(ServerRequest request) {
        Publisher orders = Flux.range(1, 5)
                .mergeWith(Flux.range(12, 5))
//                .delayElements(Duration.ofSeconds(1))

//                .elapsed()
//                .parallel(3)
//                .runOn(Schedulers.elastic())
//                .map(this::getOrder)


                .flatMap(orderId -> Mono.just(orderId)
                                .map(order -> getOrder(orderId))
                                .elapsed()
                                .map(objects -> Integer.parseInt(objects.getT2().getOrderId()) % 3 == 0 ? null : objects)
                                .onErrorContinue((throwable, o) -> log.error("Error Continue : {}", o))
                                .subscribeOn(Schedulers.elastic())

//                                .log()
                        , 3
                )
                .log()
//                .doOnNext(objects -> log.info("On Next : {} : {}", objects.getT2().getOrderId(), objects.getT1()))
//                .groups()
                .doOnComplete(this::itsDone)

                ;
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(orders, Order.class);
    }

    private void itsDone() {
        log.info("Work is been done!!!!!");
    }

    private Order getOrder(Integer orderId) {
        log.info("Order Id : " + orderId);
        try {
            Thread.sleep((orderId % 3) * 1000);
//            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Item> body = webclientBuilder
                .baseUrl("http://item-service")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("item/1").build())
                .exchange()
                .block()
                .toEntity(new ParameterizedTypeReference<List<Item>>() {
                })
                .block()
                .getBody();


        return Order.builder().orderId(orderId.toString())
                .items(body)
                .finalPrice(Math.random() * 100).build();

    }

    @Bean
    public RouterFunction orderRouter(OrderHandler handler) {
        return RouterFunctions.route(GET("/orders"), handler::getOrders);
    }

    private static boolean b = true;

    public static void main(String[] args) {
//        Flux.range(1,3)
//                .mergeWith(Flux.range(7,8 ))
//                .mergeWith(Flux.range(7,8 ))
//                .mergeWith(Flux.range(7,8 ))
//                .mergeWith(null)

        Flux.just(1, 2, 3, 4)
                .mergeWith(Flux.range(9, 3))
//                .delayElements(Duration.ofSeconds(1))
                .map(integer -> integer < 2 ? null : integer)
                .doOnComplete(() -> log.info("Complete Called"))
//                .onErrorResume(throwable -> {
//                    log.info("Error Resume Called");
//                    return Flux.just(100);
//                })
                .doOnError(throwable -> log.error("Error called"))
                .onErrorContinue((throwable, data) -> log.error("Error Continue Called : " + data))
                .doFinally((signalType) -> {
                    log.info("Finally Called");
                    b = false;
                })
                .log()
//                .delayElements(Duration.ofSeconds(2))
                .subscribe(System.out::println);

        while (b) {
            log.info("Sleeping");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Main End");
    }
}

package sai.microservices.vo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@Builder
public class Order {

    private String orderId;

    private List<Item> items;

    private Double mrpPrice;

    private Double finalPrice;

    private Float discount;

}

package sai.microservices.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Item implements Serializable {

    private String orderId;

    private String itemId;

    private Integer qty;

    private Float price;

}

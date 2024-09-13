package com.example.webflux_example.publisher;

import com.example.webflux_example.model.ItemModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class ItemUpdatesPublisher {

    private final Sinks.Many<ItemModel> itemModelUpdatesSink;

    public ItemUpdatesPublisher() {
        this.itemModelUpdatesSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public void publish(ItemModel itemModel) {
        itemModelUpdatesSink.tryEmitNext(itemModel);
    }

    public Sinks.Many<ItemModel> getUpdatesSink() {
        return itemModelUpdatesSink;
    }
}

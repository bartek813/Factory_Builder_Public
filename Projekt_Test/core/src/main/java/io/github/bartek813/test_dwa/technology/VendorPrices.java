package io.github.bartek813.test_dwa.technology;

import com.badlogic.gdx.utils.ObjectMap;

public class VendorPrices {
    private final ObjectMap<String, Integer> prices =  new ObjectMap<>();

    public VendorPrices(){
        prices.put("iron_ore",1);
        prices.put("iron_ingot",3);
        prices.put("iron_plate",5);
    }

    public int getPrice(String itemId) {
        Integer price = prices.get(itemId);
        return price == null ? 0 : price;
    }
}

package com.tarkus.merchantmonitor.helper;

import com.tarkus.merchantmonitor.Item;
import com.tarkus.merchantmonitor.Merc;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kein on 12/02/14.
 */
public class MerchantUpdaterTest {
    @Test
    public void testProceed_new_items_sold() throws Exception {

        List<Item> old_items = generate_items(),new_items = new ArrayList<Item>();
        Item item;

        item = new Item(); item.setId(578); item.setName("Strawberry"); item.setPrice(2000); item.setCount(90);
        new_items.add(item);

        MerchantUpdater updater = new MerchantUpdater(new Merc());
        List<Item> result = updater.proceed_new_items(new_items, old_items);

        org.junit.Assert.assertFalse("failure - items is not new or merchant offline", updater.isNew() && updater.isOffline());
        org.junit.Assert.assertEquals("failure - profit not correct", (Object) result.get(0).getProfit(), 20000);
    }

    @Test
    public void testProceed_new_items_reset() throws Exception {

        List<Item> old_items = generate_items(),new_items = new ArrayList<Item>();
        Item item;

        item = new Item(); item.setId(578); item.setName("Strawberry"); item.setPrice(2000); item.setCount(101);
        new_items.add(item);

        MerchantUpdater updater = new MerchantUpdater(new Merc());
        updater.proceed_new_items(new_items, old_items);

        org.junit.Assert.assertTrue("failure - merchants is not new", updater.isNew());

        updater = new MerchantUpdater(new Merc());
        new_items = new ArrayList<Item>();
        item = new Item(); item.setId(578); item.setName("Strawberry"); item.setPrice(2003); item.setCount(100);
        new_items.add(item);
        updater.proceed_new_items(new_items, old_items);
        org.junit.Assert.assertTrue("failure - merchants is not new", updater.isNew());
    }

    private List<Item> generate_items(){
        List<Item> old_items = new ArrayList<Item>();
        Item item;
        item = new Item(); item.setId(578); item.setName("Strawberry"); item.setPrice(2000); item.setCount(100);
        item.setNow_count(100);
        old_items.add(item);
        return old_items;
    }
}

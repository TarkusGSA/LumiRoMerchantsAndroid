package com.tarkus.merchantmonitor.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tarkus.merchantmonitor.Item;
import com.tarkus.merchantmonitor.MainActivity;
import com.tarkus.merchantmonitor.Merc;
import com.tarkus.merchantmonitor.R;

import java.util.ArrayList;
import java.util.Formatter;

/**
 * Created by kein on 15/02/14.
 */

public class MerchantArrayAdapter extends BaseExpandableListAdapter  {
    private final Context context;
    private ArrayList<Merc> mercs;

    private static int bgColor= Color.WHITE;

    public MerchantArrayAdapter(Context context, ArrayList mercs) {
        this.context = context;
        this.mercs = mercs;
    }

    public void add(Merc m)
    {
        mercs.add(m);
    }

    public void clear()
    {
        mercs.clear();
    }

    public Context getContext()
    {
        return this.context;
    }

    @Override
    public int getGroupCount() {
        return mercs.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mercs.get(groupPosition).getItemsCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mercs.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mercs.get(groupPosition).getItems().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.merchant_row, parent, false);
            holder = new ViewHolder();
            holder.label = (TextView) rowView.findViewById(R.id.label);
            holder.count = (TextView) rowView.findViewById(R.id.count);
            holder.profit = (TextView) rowView.findViewById(R.id.profit);
            holder.image_state = (ImageView) rowView.findViewById(R.id.image_state);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Merc merc = (Merc)mercs.get(groupPosition);
        holder.label.setText( merc.getName() );
        Integer items_count = merc.getItemsCount(); String item_count_label = "";
        if(0 < items_count){
            item_count_label = String.valueOf( items_count );
            holder.image_state.setBackgroundColor(getContext().getResources().getColor(R.color.online));
            holder.count.setText( item_count_label );
            Integer profit = merc.getProfit();
            if( profit > 0){
                // Формирую доход
                StringBuilder profitString = new StringBuilder();
                Formatter fm = new Formatter(profitString);
                fm.format("%,d",profit);

                holder.profit.setText("+"+profitString.toString());
                holder.image_state.setBackgroundColor(getContext().getResources().getColor(R.color.profit));
            } else {
                holder.profit.setText("");
            }
        } else {
            holder.count.setText("");
            holder.profit.setText("");
            holder.image_state.setBackgroundColor(getContext().getResources().getColor(R.color.offline));
        }

        return rowView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderItem holder;
        View rowView = convertView;
        Item item = (Item)mercs.get(groupPosition).getItems().get(childPosition);

//        if(null == convertView){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_row, parent, false);
            holder = new ViewHolderItem();
            holder.label = (TextView) rowView.findViewById(R.id.label);
            holder.count = (TextView) rowView.findViewById(R.id.count);
            holder.cost = (TextView) rowView.findViewById(R.id.cost);
            holder.profit = (TextView) rowView.findViewById(R.id.profit);
            holder.image = (ImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(holder);
/*        } else {
            holder = (ViewHolderItem) rowView.getTag();
        }*/

        // Показываю название товара
        if (item.getRefain()>0)
        {
            holder.label.setText(item.getName()+" (+"+item.getRefain().toString()+") "+item.getAttr());
        }
        else
        {
            holder.label.setText(item.getName()+" "+item.getAttr());
        }

        // Формирую цену за 1 штуку
        StringBuilder costString = new StringBuilder();
        Formatter fm = new Formatter(costString);
        fm.format("%,d",item.getPrice());
        // Присваиваю полученное значение цены в TextView
        holder.cost.setText(costString.toString());

        // Показываю количество
        if(item.getNow_count() > 0){
            if (item.getCount()-item.getNow_count()>0) {
                holder.count.setText(String.valueOf(item.getNow_count()) + "/" + String.valueOf(item.getCount() - item.getNow_count()));
            }
            else {
                holder.count.setText(String.valueOf(item.getNow_count()));
            }
        } else {
            holder.count.setText("");
        }

        // Показываю прибыль
        if( (item.getCount() - item.getNow_count()) != 0){
            Integer profit = (item.getCount() - item.getNow_count()) * item.getPrice();
            costString.setLength(0);
            fm.format("%,d",profit);
            holder.profit.setText("+" + costString.toString());
        } else {
            holder.profit.setText("");
        }

        // Обновляю картинку товара
        int image_resource_id = this.getContext().getResources().getIdentifier("item_" + String.valueOf(item.getId()), "drawable", "com.tarkus.merchantmonitor");
        if(0 == image_resource_id){
            image_resource_id = R.drawable.item_5819;
        }
        holder.image.setImageResource(image_resource_id);

        // Если данный пункт вынесен на виджет то подсвечиваем его
        if (MainActivity.onWidgetItem!=null && item.getId().equals(MainActivity.onWidgetItem.getId()))
        {
            rowView.setBackgroundColor(Color.rgb(250,230,255));
        }
        else
        {
//            rowView. .setBackground(parent.getBackground());
        }

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class ViewHolder {
        protected TextView label;
        protected TextView count;
        protected TextView profit;
        protected ImageView image_state;
    }

    static class ViewHolderItem {
        protected TextView label;
        protected TextView count;
        protected TextView cost;
        protected TextView profit;
        protected ImageView image;
    }
}

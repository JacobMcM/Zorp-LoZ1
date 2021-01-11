import java.util.ArrayList;

public class Inventory {
    //collection of item objects
    private ArrayList<Item> items;

    public Inventory() {
        items = new ArrayList<Item>();
    }

    public boolean addItem(Item item){
        return items.add(item);
    }

    

    //returns the item based on the name given
    //if the item is not in the inventory returns null
    public Item removeItem(String name){
        for (int i = 0; i < items.size(); i++){
            if (name.equals(items.get(i).getName())) {
                return items.remove(i);
            }
        }

        return null;
    }

    //iterates through "items" and adds every "Item" name to a string
    //then returns this finnished string
    public String toString(){
        String msg = "";

        for (Item i : items){// for each "Item" in "items"
            msg += i.getName() + "\n";
        }

        return msg;
    }
} 

import java.util.ArrayList;

public class Entity {

    //entity is a interactable enemy, man(person), or movable block and can be placed in a a room

    private String type;//type of entity: skeleton, bats, slime, brute, dragon, old man
    private int hp;//remaining health of an enemy
    private String description;
    private String validEntitys[] = { "null", "man", "bats", "skeleton", "slime", "block", "brute", "Dragon"};
    //used to confirm if a name being inputted into each entity is valid, used mostly for testing purposes

    public Entity (){//defult entity initalization
        type = "Man";
        hp = 1;
    }

    public Entity (String type, int hp, String description){
        if (isEntity(type)){//checks incoming entity "type" is valid, used for testing 
            this.type = type;
        }else{
            System.out.println("there was a problem as an entity is not valid");//printed if a problem exists in setting up entity "types", was used to test
        }
        this.hp = hp;
        this.description = description;
    }

    public int getHp(){//return hp
        return this.hp;
    }

    public String getType(){//return type
        return this.type;
    }

    public void setType(String type){//set the type, used to set a entity as "null"
        this.type = type;
    }

    public boolean doDamage (int damage){//removes hp from the entity   
        hp -= damage;

        if (hp <= 0){//return true if the enemy is "dead" (aka less then 0 hp)
            return true;
        }
        return false;//return false if the entity is still alive
    }

    public boolean isEntity(String aString) {//used for testing purposes and called when a new entity is put into a "room"
        for (int i = 0; i < validEntitys.length; i++) {
          if (validEntitys[i].equals(aString)){//uses borrowed code from Inventory
            return true;
          }
        }
        return false;
    }
    
}

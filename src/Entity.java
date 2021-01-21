import java.util.ArrayList;

public class Entity {

    //** CHANGES ***// Entity reqires major reconstruction to be more like inventory ***/
    //maybe an interior class like "monster" or "person" idc that hold the individual values of each entity
    // aka. like items aka. holding things like: "old man" "bats" "skeleton_gaurd" "brute", yadda yadda.

    private String type;//type of entity: skeleton, bats, slime, brute, dragon, old man
    private int hp;//remaining health of an enemy
    private String description;
    private String validEntitys[] = { "null", "man", "bats", "skeleton", "slime", "block", "brute", "Dragon"};

    public Entity (){
        type = "Man";
        hp = 1;
    }

    public Entity (String type, int hp, String description){
        if (isEntity(type)){
            this.type = type;
        }else{
            System.out.println("there was a problem as an entity is not valid");///***TESTER */
        }
        this.hp = hp;
        this.description = description;
        //***CHANGE ***/ would be better if had "setters and getters" like inventory
    }

    public int getHp(){
        return this.hp;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type){
        this.type = type;
    }

    public boolean doDamage (int damage){        
        hp -= damage;

        if (hp <= 0){
            return true;
        }
        return false;
    }

    public boolean isEntity(String aString) {
        for (int i = 0; i < validEntitys.length; i++) {
          if (validEntitys[i].equals(aString)){
            return true;
          }
        }
        // if we get here, the entity was wrong?
        return false;
    }
    
}

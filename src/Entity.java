public class Entity {

    //** CHANGES ***// Entity reqires major reconstruction to be more like inventory ***/
    //maybe an interior class like "monster" or "person" idc that hold the individual values of each entity
    // aka. like items aka. holding things like: "old man" "bats" "skeleton_gaurd" "brute", yadda yadda.

    private String type;//type of entity: skeleton, bats, slime, brute, dragon, old man
    private int hp;//remaining health of an enemy
    private String description;

    public Entity (){
        type = "Man";
        hp = 1;
    }

    public Entity (String type, int hp, String description){
        this.type = type;
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

    public boolean doDamage (int damage){
        if (this.hp >= damage){
            this.hp -= damage;
            return true;
        }
        return false;
    }
    
}

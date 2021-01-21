import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class Game - the main class of the "Zork" game.
 *
 * Author: Michael Kolling Version: 1.1 Date: March 2000
 * 
 * This class is the main class of the "Zork" application. Zork is a very
 * simple, text based adventure game. Users can walk around some scenery. That's
 * all. It should really be extended to make it more interesting!
 * 
 * To play this game, create an instance of this class and call the "play"
 * routine.
 * 
 * This main class creates and initialises all the others: it creates all rooms,
 * creates the parser and starts the game. It also evaluates the commands that
 * the parser returns.
 */
public class Game {
  private Parser parser;
  private Room currentRoom;
  private Inventory inventory;
  private double health;
  private int weight;
  private boolean combat;
  private int eventCounter1;
  private int eventCounter2;
  // This is a MASTER object that contains all of the rooms and is easily
  // accessible.
  // The key will be the name of the room -> no spaces (Use all caps and
  // underscore -> Great Room would have a key of GREAT_ROOM
  // In a hashmap keys are case sensitive.
  // masterRoomMap.get("GREAT_ROOM") will return the Room Object that is the Great
  // Room (assuming you have one).
  private HashMap<String, Room> masterRoomMap;

  private void initRooms(String fileName) throws Exception {
    masterRoomMap = new HashMap<String, Room>();
    Scanner roomScanner;
    try {
      HashMap<String, HashMap<String, String>> exits = new HashMap<String, HashMap<String, String>>();
      roomScanner = new Scanner(new File(fileName));
      while (roomScanner.hasNext()) {
        Room room = new Room();
        // Read the Name
        String roomName = roomScanner.nextLine();
        room.setRoomName(roomName.split(":")[1].trim());
        // Read the Description
        String roomDescription = roomScanner.nextLine();
        room.setDescription(roomDescription.split(":")[1].replaceAll("<br>", "\n").trim());

        //**changes***/ making rooms.dat also store a possible item per room, and a possible entity per room

        //**changes */ must include more detail to roominventory and room entity

        
        // read item, and generates an int weight from a string, string name and string description from a string array in rooms.dat
        String roomInventory = roomScanner.nextLine();
        String[] item = roomInventory.split(":")[1].split("-");
        room.setInventory(Integer.parseInt(item[0]), item[1], item[2].replaceAll("<br>", "\n").trim());
        // Read the Entity
        String roomEntity = roomScanner.nextLine();
        String[] entity = roomEntity.split(":")[1].split("-");
        room.setEntity(entity[1], Integer.parseInt(entity[0]), entity[2].replaceAll("<br>", "\n").trim());
        
        // Read the Exits

        String roomExits = roomScanner.nextLine();
        // An array of strings in the format E-RoomName
        String[] rooms = roomExits.split(":")[1].split(",");
        HashMap<String, String> temp = new HashMap<String, String>();
        for (String s : rooms) {
          temp.put(s.split("-")[0].trim(), s.split("-")[1]);
        }

        exits.put(roomName.substring(10).trim().toUpperCase().replaceAll(" ", "_"), temp);

        // This puts the room we created (Without the exits in the masterMap)
        masterRoomMap.put(roomName.toUpperCase().substring(10).trim().replaceAll(" ", "_"), room);

        // Now we better set the exits.
      }

      for (String key : masterRoomMap.keySet()) {
        Room roomTemp = masterRoomMap.get(key);
        HashMap<String, String> tempExits = exits.get(key);
        for (String s : tempExits.keySet()) {
          // s = direction
          // value is the room.

          String roomName2 = tempExits.get(s.trim());
          Room exitRoom = masterRoomMap.get(roomName2.toUpperCase().replaceAll(" ", "_"));
          roomTemp.setExit(s.trim().charAt(0), exitRoom);
        }
      }

      roomScanner.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the game and initialise its internal map.
   */
  public Game() {
    try {
      initRooms("data/rooms.dat");
      currentRoom = masterRoomMap.get("FOREST");
      inventory = new Inventory();

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    parser = new Parser();
  }

  /**
   * Main play routine. Loops until end of play.
   */
  public void play() {
    printWelcome();
    // Enter the main command loop. Here we repeatedly read commands and
    // execute them until the game is over.

    boolean finished = false;
    combat = false;
    eventCounter1 = 0;//counts how many times you've talked to either old man
    eventCounter2 = 0;//""
    health = 3.0;
    combat = false;
    weight = 0;
    
    while (!finished) {
      
      System.out.println("");
      if (currentRoom.getEntity().getType().equals("null") || currentRoom.getEntity().getType().equals("man") || currentRoom.getEntity().getType().equals("block")){
        combat = false;
      }
      if (combat){
        System.out.println("you are currently in combat with " + currentRoom.getEntity().getType());
        System.out.println("it has " + currentRoom.getEntity().getHp() + " hearts left" );
      }
      System.out.println("hearts: " + health);
      System.out.println("inventory slots: " + weight + "/4 used");
      System.out.println("");
      Command command = parser.getCommand();
      System.out.println("");

      finished = processCommand(command, eventCounter1, eventCounter2);
    }
    System.out.println("Thank you for playing.  Good bye.");
  }

  /**
   * Print out the opening message for the player.
   */
  private void printWelcome() {
    System.out.println();
    System.out.println("Welcome to The Legend Of Zelda-Text adventure Game");
    System.out.println("This is a shortened Re-creation of the first ever Zelda game in a text adventure medium.");
    System.out.println("Your quest is to reclaim the magical Triforce, defeat the evil pig-king Ganon and save the princess Zelda. Good Luck");
    System.out.println("Type 'help' if you need help.");
    System.out.println();
    System.out.println(currentRoom.longDescription());
  }

  /**
   * Given a command, process (that is: execute) the command. If this command ends
   * the game, true is returned, otherwise false is returned.
   */
  private boolean processCommand(Command command, int count1, int count2) {// "on"
    if (command.isUnknown()) {
      System.out.println("I don't know what you mean...");
      return false;
    }
    String commandWord = command.getCommandWord();
    if (commandWord.equals("help"))
      printHelp();
    else if (commandWord.equals("go"))
      goRoom(command);
    else if (commandWord.equals("quit")) {
      if (command.hasSecondWord())
        System.out.println("Quit what?");
      else
        return true; // signal that we want to quit
    }else if (commandWord.equals("take")){
      if (!command.hasSecondWord()){
        System.out.println("take what? I don't know what you mean... ");
      }else if(currentRoom.getInventory().isItem(command.getSecondWord())){
        takeItem(command.getSecondWord());
      }else{
        System.out.println("error found item wrong");///***change***///tester code
      }
    }else if (commandWord.equals("drop")){
      if (!command.hasSecondWord()){
        System.out.println("drop what? I don't know what you mean... ");
      }else if(inventory.isItem(command.getSecondWord())){
        dropItem(command.getSecondWord());
      }else{
        System.out.println("error found item wrong");///***change***///tester code
      }
    }else if (commandWord.equals("inventory")){
      System.out.println("you are carrying the following: " + inventory);
    }else if (commandWord.equals("talk")){
      if (command.hasSecondWord()){
        System.out.println("talk what?");
      }else{
        talk(count1, count2);
      }
    }else if (commandWord.equals("use")){
      use(command);
    }else if (commandWord.equals("push")){
      if (command.hasSecondWord()){
        System.out.println("push what?");
      }else{
        push();
      }
    }else if (commandWord.equals("duck")){
      if (command.hasSecondWord()){
        System.out.println("duck huh? *quack quack*");
      }else{
        duck();
      }
    }else if (commandWord.equals("roll")){
      if (command.hasSecondWord()){
        System.out.println("roll huh?");
      }else{
        roll();
      }
    }else if (commandWord.equals("squish")){
      if (!command.hasSecondWord()){
        System.out.println("squish who?");
      }else{
        squish(command);
      }
    }else if (commandWord.equals("block")){
      if (!command.hasSecondWord()){
        System.out.println("block who?");
      }else{
        block(command);
      }
    }else if (commandWord.equals("scream")){
      if (command.hasSecondWord()){
        System.out.println("scream huh? are you screaming internally?");
      }else{
        scream();
      }
    }else if (commandWord.equals("cry")){
      if (command.hasSecondWord()){
        System.out.println("cry huh? are you crying internally?");
      }else{
        cry();
      }
    }else if (commandWord.equals("suicide")){
      if (command.hasSecondWord()){
        System.out.println("sui- what?!? what are you taling about?");
      }else{
        suicide();
        return true;
      }
    }
    
    //win/lose conditions:

    if (currentRoom.getRoomName().equals("Village")){
      System.out.println("deciding that being an adventurer is not for you, you return home");
      System.out.println("you live alone for the rest of your life, with no one recognising the great hero you could have been");
      return true;
    }else if (health <= 0.0){
      System.out.println("the light fades from your eyes as you leave this world");
      System.out.println("you never reached your goal, and you die with that as your final thought");
      return true;
    }else if (inventory.isItem("Triforce")){
      System.out.println("the mystical triangle glows in front of you");
      System.out.println("you have made it through the perilous quest and have obtained the first peice of the Triforce");
      System.out.println("You have taken your first step to becoming a hero, but the journey will be long and hard");
      System.out.println("looking back once more at the triangular object you feel prepared for what comes");
      System.out.println("");
      System.out.println("thank you for playing my ICT ISP: the ledgend of Zelda text-adventure.");
      return true;
    }
  
  
    return false;
    
  }

  // implementations of user commands:
  /**
   * Print out some help information. Here we print some stupid, cryptic message
   * and a list of the command words.
   */
  private void printHelp() {
    System.out.println("the world arround is oppresive and confusing,");
    System.out.println("Your feeble, insignificant, puny mind yells at the face of nowhere for answers.");
    System.out.println();
    System.out.println("Your command words are:");
    parser.showCommands();
    System.out.println("you're currently in: " + currentRoom.getRoomName());
    System.out.println("you are currently in combat with a " + currentRoom.getEntity().getType() + " in this room");
    System.out.println("you can see a " + currentRoom.getInventory() + " in the room");
  }

  /**
   * Try to go to one direction. If there is an exit, enter the new room,
   * otherwise print an error message.
   */
  private void goRoom(Command command) {
    if (!command.hasSecondWord()) {
      // if there is no second word, we don't know where to go...
      System.out.println("Go where?");
      return;
    }
    String direction = command.getSecondWord();
    // Try to leave current room.
    if (combat){
      System.out.println("You cannot leave this room, you are currently engauged in combat");
    }else{
      Room nextRoom = currentRoom.nextRoom(direction);
      if (nextRoom == null)
        System.out.println("There is no door!");
      else {
        currentRoom = nextRoom;
        System.out.println(currentRoom.longDescription());
        if (!currentRoom.getEntity().equals("null") || !currentRoom.getEntity().equals("man") || !currentRoom.getEntity().equals("block")){
          combat = true;
        }
      }
    }
  }

  private void takeItem(String itemName) {
    Inventory temp = currentRoom.getInventory();

    Item item = temp.removeItem(itemName);
    int itemWeight = item.getWeight();

    if (item != null){
      if (weight + itemWeight < 4){
        if (inventory.addItem(item)) {
          System.out.println("You have taken the " + itemName);
          weight += itemWeight;
          if (itemName.equals("heart")){
            System.out.println("it grants you two additonal hearts");
            health += 2.0;
          }
        }else{
          System.out.println("you were unable to take the " + itemName);
        }
      }else{
        System.out.println("the weight of the a " + item.getName() + " is " + itemWeight);
        System.out.println("you do not have enough inventory space for a " + item.getName());
      }
    }else{
      System.out.println("there is no item called: " + itemName + " in this room");
    }
  }

  private void dropItem(String itemName) {
    Item item = inventory.removeItem(itemName);
    int itemWeight = item.getWeight();
    
    if (item != null){
      if (currentRoom.getInventory().addItem(item)) {
        System.out.println("You have dropped the " + itemName);
        weight -= itemWeight;
        if (itemName.equals("heart")){
          System.out.println("you lose your two additonal hearts");
          health -= 2.0;
        }
      }else{
        System.out.println("you were unable to drop the " + itemName);
      }
    }else{
      System.out.println("there is no item called: " + itemName + " in your inventory");
    }
  }

  //***changes needed**/in room I have code written which will acctually make entity = null instead of having its name = "null", this will break...
  //alot of these commands, dont forget
  private void talk(int count1, int count2){//finnished
    if (currentRoom.getRoomName().equals("Cave")){//***IMPORTANT***//have to change if you are renaming locations
      System.out.println("You speak to the old man in the room:");
      if (count1 == 0){
        System.out.println("\"oh you want me to say the thing? its... um... perilous to go by yourself... uh... Bring this with you...\"");
        eventCounter1 = 1;
      }else if(count1 == 1){
        System.out.println("\"What? oh stop loking at me like that...\"");
        eventCounter1 = 2;
      }else if(count1 == 2){
        System.out.println("\"Just take the sword and leave.\"");
        eventCounter1 = 3;
      }else{
        System.out.println("It looks as if the old man is ignoring you");
      }
    }else if (currentRoom.getRoomName().equals(" level A3")){//***IMPORTANT***//have to change if you are renaming locations
      System.out.println("You speak to the old man in the room:");
      if (count2 == 0){
        System.out.println("\"EASTMOST PENNINSULA IS THE SECRET!\"");
        eventCounter2 = 1;
      }else if(count2 == 1){
        System.out.println("\"dont ask me what it means, I have no idea...\"");
        eventCounter2 = 2;
      }else if(count2 == 2){
        System.out.println("\"I have no more knowledge, bugger off lad.\"");
        eventCounter2 = 3;
      }else{
        System.out.println("It looks as if the old man is ignoring you");
      }
    }else if(currentRoom.getEntity().getType().equals("null") || currentRoom.getEntity().getType().equals("block")){
      System.out.println("there is nothing arround to talk to...");
      System.out.println("you talk to youself, such as a crazed lunatic would, then return back to your quest");
    }else{
      System.out.println("you attempt to comunicate with the monster but it doesnt seem to understand...");
      System.out.println("you are now vulnerable to an attack");
      takeDamage(1);//***change later */
    }
  }

  private void use(Command command){//95% done need unlock (may remove)
    if (inventory.isItem(command.getSecondWord())){//if somthing breaks, check how inventory is stored/created
      if (command.getSecondWord().equals("sword") || command.getSecondWord().equals("bow") || command.getSecondWord().equals("Freeze")){
        attack(command.getSecondWord());
      }else if (command.getSecondWord().equals("heart")){
        System.out.println("heart is an active item, and therefore cannot be \"used\"");
        System.out.println("drop it to remove its effects");
      }else if (command.getSecondWord().equals("key")){
        //when doors are unlockable implemt a function here---
      }else{///***change *///it wont work after changing null-named into just null
        System.out.println("you cant \"use\" nothing");
      }
    }else{
      System.out.println("that item is not in your inventory");
    }
  }

  private void attack(String attackItem){//finnished but might be 80% cus of glitchs/exploits
    if (!currentRoom.getEntity().getType().equals("null") && !currentRoom.getEntity().getType().equals("block") && !currentRoom.getEntity().getType().equals("man")){
      System.out.println("You are using a " + attackItem + " to fight the " + currentRoom.getEntity().getType());

      int damage = 0;
      if (attackItem.equals("sword")){
        damage = 1;
        System.out.println("the mighty steel of your blade cuts into the " + currentRoom.getEntity().getType());

      }else if(attackItem.equals("bow")){
        damage = 2;
        System.out.println("you nock a arrow and watch as it flys gracfully into the " + currentRoom.getEntity().getType());
      }else if(attackItem.equals("Freeze")){
        damage = 3;
        System.out.println("a touch of cold flows through your hands as you unleash a tiny blizzard of magic onto the " + currentRoom.getEntity().getType());
      }
      System.out.println("you do " + damage + " heart(s) of damage to the " + currentRoom.getEntity().getType());
      
      boolean isDead = currentRoom.getEntity().doDamage(damage);
      if(isDead){
        System.out.println("you have vanquished the " + currentRoom.getEntity().getType());
        currentRoom.getEntity().setType("null");
        combat = false;
      }else{      
        if (attackItem.equals("Freeze")){
          System.out.println("the enemy seems unable to move from that ice spell");
        }else{
          System.out.println("you attack the " + currentRoom.getEntity().getType() + " but it doesn't go down easy");
          System.out.println("you are now vulnerable to an attack");
          takeDamage(1);
        }
      }      
    }else if (currentRoom.getEntity().getType().equals("man")){
      if (attackItem.equals("sword")){
        System.out.println("the mighty steel of your blade cuts into the " + currentRoom.getEntity().getType());
      }else if(attackItem.equals("bow")){
        System.out.println("you nock a arrow and watch as it flys gracfully into the " + currentRoom.getEntity().getType());
      }else if(attackItem.equals("Freeze")){
        System.out.println("a touch of cold flows through your hands as you unleash a tiny blizzard of magic onto the " + currentRoom.getEntity().getType());
      }
      System.out.println("as your " + attackItem + " hits the old man, it is deflected away");
      System.out.println("the Old man laughs menacingly \"you have just made a mistake you WILL regret!\"");
      System.out.println("the old man removes the cloak he was wearing, revealing his 12-pack abs");
      System.out.println("in a blink on an eye he is standing over you, grabs you by the neck and slams you to the floor");
      System.out.println("the last thing you'd ever see is his intimidating face standing over you");
      health = 0.0;
      
    }else{
      System.out.println("There is nothing in this room to fill your bloodlust");
      System.out.println("realizing your never-ending thirst for blood isn't found here, then return back to your quest");
    }
  }

  private void push(){//95% finnished, reqires locks code    
    if (currentRoom.getEntity().getType().equals("block")){
      System.out.println("you push the crumbling block out of the way");
      System.out.println("as you do the block slots into a hile in the ground and becomes part of the floor");
      //***needs adding****// this actions would open certian "blocked" doors
      currentRoom.getEntity().setType("null");
    }else if(currentRoom.getEntity().getType().equals("man")){
      System.out.println("you force your body weight onto the old man");
      System.out.println("but surprisingly he doesnt budge at all, like if he was made of stone");
      System.out.println("he looks down at your attempt, not with anger but with mild bemusement, saying nothing");
      System.out.println("after several unsuccessful soves, you return to your quest");
    }else if(currentRoom.getEntity().getType().equals("null")){
      System.out.println("you force a strong push towards one of the walls of the small room");
      System.out.println("wait... was the room always this small?");
      System.out.println("is...is it just me or... is this room getting smaller");
      System.out.println("...");
      System.out.println("\"HELP! HELP!!! THE WALLS ARE CLOSING IN!!!!\"");
      System.out.println("\"OH NO OH PLEASE NOOOOOOOOOOOOOOOOOOOOOO!!!\"");
      System.out.println("suddenly you realize that, no, the walls are not closing in, you're just starved for attention.");
      System.out.println("you return to your quest");
    }else{
      System.out.println("You press your weight against the " + currentRoom.getEntity().getType());
      System.out.println("you realize that all you've done is made it angry");
      System.out.println("you are now vulnerable to an attack");
      takeDamage(1);
    }    
  }

  private void duck(){//finished
    if (currentRoom.getEntity().getType().equals("bats")){
      System.out.println("you drop to the floor quickly as the bat swarm flys overhead exiting the temple");
      System.out.println("there are no more bats in this room");
      currentRoom.getEntity().setType("null");
      combat = false;
    }else if (currentRoom.getEntity().getType().equals("null") || currentRoom.getEntity().getType().equals("block")){
      System.out.println("like a coward you drop to your knees at the first face of challenge");
      System.out.println("after roleplaying a true yellow-belly for a minute or two, you return to your quest");
    }else if (currentRoom.getEntity().getType().equals("man")){
      System.out.println("you drop to your knees right in front of the old man");
      System.out.println("\"you all right lad? did something scare you, hmm?\" the old man laughs to himself");
      System.out.println("after suffering through his humiliation, you return to your quest");
    }else{
      System.out.println("You quickly drop at the sight of the monster");
      System.out.println("despite this, the monster can still see you, and still wants to hurt you");
      System.out.println("you are now vulnerable to an attack");     
      takeDamage(1);      
    }
  }

  private void roll(){//finnished
    if (currentRoom.getEntity().getType().equals("man")){
      System.out.println("you preform a tactical roll in front of the old man");
      System.out.println("hes not impressed");
      System.out.println("with that disapointing let-down, you return to your quest");
    }else if (currentRoom.getEntity().getType().equals("null") || currentRoom.getEntity().getType().equals("block")){
      System.out.println("you starting rolling arround the room, picking up speed");
      System.out.println("once, twice, three-times, your picking up speed while rolling in a circle in the room");
      System.out.println("as you get faster the spinning image of the room becomes blured and you start seeing fantastical images with strange propertys");
      System.out.println("suddely you hit a wall, and after having a visons beyond this mortal plane, you throw up from the dizziness");
      System.out.println("despite having a supernatural life-changing vision, you return to your quest");
    }else if (currentRoom.getEntity().getType().equals("dragon")){
      System.out.println("you roll quickly to atempt to avoid a vicious stream of fire");
      takeDamage(4);
    }else if (currentRoom.getEntity().getType().equals("brute")){
      System.out.println("you roll quickly to atempt to avoid a bitter cold freeze spell");
      System.out.println("the brute is so surprized you avoided his freeze spell that he isn't atempting a counter attack");
    }else if (currentRoom.getEntity().getType().equals("skeleton")){
      System.out.println("you roll quickly to atempt to avoid the sharp end of the skeleton's blade");
      takeDamage(2);
    }else{
      System.out.println("you roll arround the room to avoid an attack");
      System.out.println("but despite this, the " + currentRoom.getEntity().getType() + " follows you with their eyes");
      System.out.println("you are now vulnerable to an attack");
      if (combat == true){
        takeDamage(1);
      }
    }
  }

  private void squish(Command command){//finnished
    if (currentRoom.getEntity().isEntity(command.getSecondWord())){
      if (currentRoom.getEntity().getType().equals(command.getSecondWord())){
        if (command.getSecondWord().equals("slime")){
          System.out.println("you bring the weight of your foot upon the gooey monster");
          System.out.println("the living slime is swiftly defeated under the mighty heel of your boot");
          System.out.println("that boot will need a good clean though, eugh!");
          currentRoom.getEntity().setType("null");
          combat = false;
        }else if(command.getSecondWord().equals("man")){
          System.out.println("you step on the foot of the older gentlemen");
          System.out.println("he winces at the action and stares at you with annoyance");
          System.out.println("\"REMOVE. YOUR. FOOT. FROM. MINE.");
          System.out.println("In shear fear of retaliation, you QUICKLY return to your quest");
        }else if(command.getSecondWord().equals("null") || command.getSecondWord().equals("block")){
          System.out.println("you step on nothing, imagining standing overtop a grand victory");
          System.out.println("so get on with it loser, stop imagining");
          System.out.println("you return to your qeust");
        }else{
          System.out.println("you attempt to step on the " + command.getSecondWord());
          System.out.println("you realize that all you've done is made it angry");
          System.out.println("you are now vulnerable to an attack");
          takeDamage(1);
        }
      }else{
        System.out.println("you can't push a " + command.getCommandWord() + " becuase there arn't any in this room");
      }
    }else{
      System.out.println("I dont know what you're talking about? what is a " + command.getSecondWord() + " ?");
    }
  }

  private void block(Command command){//finnished
    if (currentRoom.getEntity().isEntity(command.getSecondWord())){
      if (currentRoom.getEntity().getType().equals(command.getSecondWord())){
        if (this.inventory.isItem("sword")){
          if (command.getSecondWord().equals("skeleton")){
            System.out.println("you pull up your sword in a blocking stance against the undead blade-user");
            System.out.println("the skeleton swings, and (to break the 4rth wall a bit) there's a 50% chance he'll hit!");
            takeDamage(2);
          }else if(command.getSecondWord().equals("man")){
            System.out.println("you pull out your sword and enter a defence stance against the old man");
            System.out.println("he chuckles: \"if I wanted to attack you, that puny stance wouldn't stop me\"");
            System.out.println("while considering what he truely means, you return to your quest");
          }else if(command.getSecondWord().equals("null") || command.getSecondWord().equals("block")){
            System.out.println("you pull out your sword to defend against... nothing");
            System.out.println("do you have a reason? who KNOWS! WHO CARES!!!");
            System.out.println("I'm only curious becuase I have no idea what in the word your doing");
            System.out.println("put that stupid thing away so you can return to your qeust");
          }else if(command.getSecondWord().equals("dragon") || command.getSecondWord().equals("brute")){
            System.out.println("you pull out you sword to defend against a flow of magic");
            System.out.println("the sword cuts through the magic but its still heading towards you");
            System.out.println("the magic is deflected stright into your arms and legs, this isn't an anime");
            takeDamage(1);            
          }else{
            System.out.println("you attempt to block against the " + command.getSecondWord());
            System.out.println("but the " + command.getSecondWord() + " can get around your weak attempt");
            System.out.println("you are now vulnerable to an attack");            
            takeDamage(1);
          }
        }else{
          System.out.println("you do not have a sword to block with");
        }        
      }else{
        System.out.println("you can't block against a " + command.getCommandWord() + " becuase there arn't any in this room");
      }
    }else{
      System.out.println("I dont know what you're talking about? what is a " + command.getSecondWord() + " ?");
    }
  }

  private void scream(){
    if (currentRoom.getEntity().getType().equals("man")){
      System.out.println("you realease an unholy sound in front of the old man");
      System.out.println("he covers his ears in pain \"what are you DOING!\"");
      System.out.println("realizing you've upset him, you return to your quest");
    }else if (currentRoom.getEntity().getType().equals("null") || currentRoom.getEntity().getType().equals("block")){
      System.out.println("you scream into room");
      System.out.println("the yell echos throughout the temple");
      System.out.println("as alone as you feel, you suddely feel less as multiple versions of your own voice are nearby you");
      System.out.println("having made this weird relization, you return to your quest");
    }else{
      System.out.println("you scream into the face of the " + currentRoom.getEntity().getType());
      System.out.println("it seems you have affected the " + currentRoom.getEntity().getType() +" and will have a 50/50 chance of hitting you after");
      System.out.println("you are now vulnerable to an attack");
      takeDamage(2);
    }
  }

  private void cry(){
    if (currentRoom.getEntity().getType().equals("man")){
      System.out.println("you curl up into a ball in front of the old man");
      System.out.println("\"stop acting like a baby and get back to your mission\"");
      System.out.println("\"...\"");
      System.out.println("\"listen kid, if you need someone to talk to, you can open up to me\"");
      System.out.println("after opening up to the old man about your insecurities, you return to your quest");
    }else if (currentRoom.getEntity().getType().equals("null") || currentRoom.getEntity().getType().equals("block")){
      System.out.println("you start bawling on the floor of the temple");
      System.out.println("you miss your village, your friends, your home.. your mom");
      System.out.println("after you pull yourself together becuase of the importance of the mission...");
      System.out.println("you return to your quest");
    }else{
      System.out.println("you start crying in front the " + currentRoom.getEntity().getType());
      System.out.println("for a split second it second it seems like the " + currentRoom.getEntity().getType() + " has empathy for you");
      System.out.println("but then it returns to attacking you");
      System.out.println("you are now vulnerable to an attack");
      takeDamage(1);
    }
  }

  private void suicide(){//finnished
    System.out.println("Suicide and depression are serious issues and should be discussed and dealt with the appropriate attention");
    System.out.println("if you are considering suicide, seek profesional help. you do not have to go through it alone");
    System.out.println("Suddenly link's friend from the village enters nearby, and talks to link");
    System.out.println("link returns home and recives the proper medical attention and lives a long happy life. the end.");
    //game will end after this ^
  }

  private void takeDamage(int percent){//requires tweeking
    int rand = (int)(Math.random()* percent) + 1;

    if(rand == 1){
      if (currentRoom.getEntity().getType().equals("skeleton")){
        System.out.println("the boney menace strikes you with his sword for a full heart of damage");
        health -= 1.0;
      }else if (currentRoom.getEntity().getType().equals("slime")){
        System.out.println("the gelatinous being strikes you with the force of its body for a half heart of damage");
        health -= 0.5; 
      }else if (currentRoom.getEntity().getType().equals("bats")){
        System.out.println("the large swarm strikes you with their serveral claws for a half heart of damage");
        health -= 0.5;
      }else if (currentRoom.getEntity().getType().equals("brute")){
        System.out.println("the large brute freezes you with fear the strikes you with their meaty fist for a full heart of damage");
        health -= 1.0;
      }else if (currentRoom.getEntity().getType().equals("dragon")){
        System.out.println("the towering lizard spits out a ball of fire which stikes you for a heart and a half of damage");
        health -= 1.5;
      }
    }else{
      System.out.println("luck is on your side as the " + currentRoom.getEntity().getType() + " misses its attack");
    }
  }
}

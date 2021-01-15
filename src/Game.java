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
  private int health;
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

        /*
        // read item, and generates an int weight from a string, string name and string description from a string array in rooms.dat
        String roomInventory = roomScanner.nextLine();
        String[] item = roomInventory.split(":")[1].split("-"); 
        room.setInventory(Integer.parseInt(item[0]), item[1], item[2].replaceAll("<br>", "\n").trim());
        // Read the Description
        String roomEntity = roomScanner.nextLine();
        String[] entity = roomInventory.split(":")[1].split("-");
        room.setEntity(entity[1], Integer.parseInt(entity[0]), entity[2].replaceAll("<br>", "\n").trim());
        */
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
      health = 3;

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
    health = 3;
    while (!finished) {
      Command command = parser.getCommand();
      finished = processCommand(command);
      if (health <= 0){
        finished = false;
      }
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
  private boolean processCommand(Command command) {// "on"
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
      }else{
        takeItem(command.getSecondWord());
      }
    }else if (commandWord.equals("drop")){
      if (!command.hasSecondWord()){
        System.out.println("drop what? I don't know what you mean... ");
      }else{
        dropItem(command.getSecondWord());
      }
    }else if (commandWord.equals("inventory")){
      System.out.println("you are carrying the following: " + inventory);
    }else if (commandWord.equals("talk")){
      talk(command);
    }else if (commandWord.equals("use")){
      use(command);
    }else if (commandWord.equals("push")){
      if (!command.hasSecondWord()){
        System.out.println("push what?");
      }else{
        push(command);
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
    }else if (commandWord.equals("parry")){
      if (!command.hasSecondWord()){
        System.out.println("parry who?");
      }else{
        parry(command);
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
    Room nextRoom = currentRoom.nextRoom(direction);
    if (nextRoom == null)
      System.out.println("There is no door!");
    else {
      currentRoom = nextRoom;
      System.out.println(currentRoom.longDescription());
    }
  }

  private void takeItem(String itemName) {
    Inventory temp = currentRoom.getInventory();

    Item item = temp.removeItem(itemName);

    if (item != null){
      if (inventory.addItem(item)) {
        System.out.println("You have taken the" + itemName);
      }else{
        System.out.println("you were unable to take the " + itemName);
      }
    }else{
      System.out.println("there is no item called: " + itemName + " in this room");
    }
  }

  private void dropItem(String itemName) {
    Item item = inventory.removeItem(itemName);

    if (item != null){
      if (currentRoom.getInventory().addItem(item)) {
        System.out.println("You have dropped the" + itemName);
      }else{
        System.out.println("you were unable to drop the " + itemName);
      }
    }else{
      System.out.println("there is no item called: " + itemName + " in your inventory");
    }
  }

  private void talk(Command command){

  }

  private void use(Command command){

  }

  private void push(Command command){

  }

  private void duck(){

  }

  private void roll(){

  }

  private void parry(Command command){

  }

  private void squish(Command command){

  }

  private void block(Command command){

  }

  private void scream(){

  }

  private void cry(){

  }

  private void suicide(){
    System.out.println("Suicide and depression are serious issues and should be discussed and dealt with the appropriate attention");
    System.out.println("if you are considering suicide, seek profesional help. you do not have to go through it alone");
    System.out.println("Suddenly link's friend from the village enters nearby, and talks to link");
    System.out.println("link returns home and recives the proper medical attention and lives a long happy life. the end.");
    //game will end after this ^
  }
}

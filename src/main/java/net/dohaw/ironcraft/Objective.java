package net.dohaw.ironcraft;

import org.apache.commons.lang.StringUtils;

public enum Objective {

    MOVE("Hello! Welcome to Minecraft. In this tutorial, you will be taught how to obtain an iron pickaxe. To get started, use WASD on your keyboard to start moving. You can also use your mouse to look around"),
    PLACE_A_TORCH("When you get out on your own, it may get a little dark. Use the numbers on your keyboard to switch to your torch and right-click the ground to put a torch down"),
    COLLECT_WOOD("Good job! Now you'll need some wood. Put your crosshair (The thing in the middle of your screen) on to the wood and hold left click to fully mine it. \n \nDon't be afraid to break some leaves to get to the wood!\n \nCollect &e4 logs&7 to move on to the next objective"),
    OPEN_RECIPE_MENU("The Recipe Menu is your best friend. Press \"E\" on your keyboard and click on the green book in your inventory. The recipe menu is a very powerful tool. You can click on the items you see on the left, and the game will sort of show you how to craft them. "),
    MAKE_PLANKS("Now that you have wood, you can make some planks! To do this, press \"E\" on your keyboard to open up your inventory. Once you have your inventory open, use your mouse to click the logs you have in your inventory. The logs will then be in your cursor.\n \nNext, put the logs in any of the 4 slots to the right of your character. If you did this correctly, you should then see wooden planks to the right of the arrow. Click the planks and put them in your inventory.\n \nIf you ever wish to exit the inventory, you can press the escape key on your keyboard! &a&lYou can use the recipe book if you like to help you out!"),
    MAKE_STICKS("Now that you have planks, you can make sticks. To make sticks, you're going to have to press \"E\" on your keyboard once more.\n \nPick up the wooden planks in your inventory so that they end up in your cursor. Once you've done that, bring it over to the 4 slots that you used in the last objective. To make a stick you need to make a column with the wooden planks.\n \nTo do this, right click the top left slot to place one of your wooden planks down and then right-click the bottom left slot. If you did this correctly, you should see sticks as a result!"),
    MAKE_CRAFTING_TABLE("You're so close to making your first tool! You just need one more thing: A crafting table. To make a crafting table, you will need 4 wooden planks. You may need to make more wooden planks if you don't have 4 already. To refresh your mind, you place logs in the little crafting menu we have been using for the last 2 steps. Once you have enough planks, you're going to need to refer back to the crafting menu again. To make a crafting table, put wooden planks in all 4 slots of the inventory crafting menu."),
    PLACE_CRAFTING_TABLE("Now that you have a crafting table, you'll need to place it down. To get the crafting table in your hand, make sure it's in the very bottom row of your inventory. \n \nThis row is called your hotbar. Once it's in your hotbar, press the number keys on your keyboard to switch to that item. You can then right click any block on the ground or wall to place the crafting table down."),
    MAKE_WOODEN_PICKAXE("To use the crafting table, simply put your crosshair on it and right-click it.\n \n To make a wooden pickaxe, you will need 2 sticks and 3 wooden planks. Enter the crafting table and put a stick in the bottom-middle slot and the middle slot. You then place a wooden plank in each slot in the top row. If you did this correctly, you should have created a wooden pickaxe!"),
    COLLECT_STONE("Now it's time to start mining, sort of. With your newly made pickaxe in hand, start mining the ground. You may have to dig past some grass before you get to the stone. You will move to the next objective when you have &e15 cobblestone&7 in your inventory."),
    MAKE_STONE_PICKAXE("You're going to have to use your crafting table again. To make a stone pickaxe, you will need 2 sticks and 3 cobblestone. To make a stone pickaxe, place 1 stick in the bottom-middle slot of the crafting table, and another stick in the middle slot of the crafting table. You will then place 1 cobblestone in each slot of the top row. It's very similar to crafting a wooden pickaxe."),
    COLLECT_IRON("It's time to get busy! Go to the place with the stone, and keep mining until you find an ore with some orange-ish color to it. Get &e3&7 of these ores to move on to the next objective."),
    MAKE_FURNACE("In order to turn these ores into something that can make an iron pickaxe, we need to make a furnace. Use your crafting table, and put cobblestone in the outer slots of the crafting table.\n \nYou should be placing 1 cobblestone in every single slot, except the middle one."),
    SMELT_IRON("Place the furnace down and right-click it to use it. You'll see that there are 2 slots to the left of the arrow. The bottom slot is the furnace fuel. The top slot is the thing you want to cook/smelt.\n \nThere are a lot of things you can use to fuel a furnace in Minecraft. You can use coal, logs, wood, etc. For now, place some of your wood in the fuel slot. After that, put the iron ore in the top slot and it'll start smelting the iron."),
    MAKE_IRON_PICKAXE("It's time to use the crafting table again. Open the crafting table, and put a stick in the bottom-middle and middle slots. After that, put an iron ingot in each slot of the top row.");

    private final String helperMessage;

    Objective(String helperMessage) {
        this.helperMessage = helperMessage;
    }

    public String getHelperMessage() {
        return helperMessage;
    }

    public String toProperName() {
        return StringUtils.capitalize(name().replace("_", " "));
    }

}
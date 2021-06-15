package net.dohaw.diamondcraft.menu;

import org.bukkit.Material;

import java.util.Map;

public class Recipe {

    private Material result;
    private Map<Integer, Material> ingredients;

    private boolean isCraftedInTable;

    public Recipe(Material result, Map<Integer, Material> ingredients, boolean isCraftedInTable){
        this.result = result;
        this.ingredients = ingredients;
        this.isCraftedInTable = isCraftedInTable;
    }

    public Material getResult() {
        return result;
    }

    public Map<Integer, Material> getIngredients() {
        return ingredients;
    }

    public boolean isCraftedInTable() {
        return isCraftedInTable;
    }

}

package net.dohaw.diamondcraft.menu;

import org.bukkit.Material;

import java.util.Map;

public class Recipe {

    private final Material result;
    private final Map<Integer, Material> ingredients;

    private final boolean isCraftedInTable;

    public Recipe(Material result, Map<Integer, Material> ingredients, boolean isCraftedInTable) {
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

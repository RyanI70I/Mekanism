package mekanism.common.recipe.lookup.cache.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

/**
 * Base input cache that implements the backend handling and lookup of a single basic key based input.
 */
public abstract class BaseInputCache<KEY, INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe>
      implements IInputCache<INPUT, INGREDIENT, RECIPE> {

    /**
     * Map of keys representing inputs to a set of the recipes that contain said input. This allows for quick contains checking by checking if a key exists, as well as
     * quicker recipe lookup.
     */
    private final Map<KEY, Set<RECIPE>> inputCache = new HashMap<>();

    @Override
    public void clear() {
        inputCache.clear();
    }

    @Override
    public boolean contains(INPUT input) {
        return inputCache.containsKey(createKey(input));
    }

    @Override
    public boolean contains(INPUT input, Predicate<RECIPE> matchCriteria) {
        Set<RECIPE> recipes = inputCache.get(createKey(input));
        if (recipes != null) {
            for (RECIPE recipe : recipes) {
                if (matchCriteria.test(recipe)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public RECIPE findFirstRecipe(INPUT input, Predicate<RECIPE> matchCriteria) {
        return findFirstRecipe(inputCache.get(createKey(input)), matchCriteria);
    }

    /**
     * Helper to filter a potentially null collection of recipes by a given predicate.
     */
    @Nullable
    protected RECIPE findFirstRecipe(@Nullable Collection<RECIPE> recipes, Predicate<RECIPE> matchCriteria) {
        if (recipes != null) {
            for (RECIPE recipe : recipes) {
                if (matchCriteria.test(recipe)) {
                    return recipe;
                }
            }
        }
        return null;
    }

    /**
     * Creates a key for the given input for use in querying our input cache.
     *
     * @param input Input to convert into a key.
     *
     * @return Key representing the given input.
     */
    protected abstract KEY createKey(INPUT input);

    /**
     * Adds a given recipe to the input cache using the corresponding key.
     *
     * @param input  Key representing the input.
     * @param recipe Recipe to add.
     */
    protected void addInputCache(KEY input, RECIPE recipe) {
        inputCache.computeIfAbsent(input, i -> new HashSet<>()).add(recipe);
    }

    /**
     * Adds a given recipe to the input cache using the corresponding key.
     *
     * @param input  Key representing the input.
     * @param recipe Recipe to add.
     */
    protected void addInputCache(Holder<KEY> input, RECIPE recipe) {
        addInputCache(input.value(), recipe);
    }
}
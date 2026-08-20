package com.masterquentus.projectpandora.recipe;

import com.masterquentus.projectpandora.item.ModItems;
import com.masterquentus.projectpandora.item.custom.SovereignSpearItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class SovereignSpearUpgradeRecipe extends CustomRecipe {

    public SovereignSpearUpgradeRecipe() {
        super();
        System.out.println("SOVEREIGN SPEAR RECIPE CREATED");
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasSpear = false;
        boolean hasSoul = false;
        boolean hasContract = false;
        int count = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!stack.isEmpty()) {
                count++;

                if (stack.getItem() instanceof SovereignSpearItem && !hasSpear) {
                    hasSpear = true;
                }
                else if (stack.is(ModItems.PANDORA_SOUL.get()) && !hasSoul) {
                    hasSoul = true;
                }
                else if (stack.is(ModItems.PANDORA_CONTRACT.get()) && !hasContract) {
                    hasContract = true;
                }
            }
        }

        return hasSpear && hasSoul && hasContract && count == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        System.out.println("===== SOVEREIGN SPEAR ASSEMBLE CALLED =====");

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.getItem() instanceof SovereignSpearItem) {
                ItemStack result = stack.copy();

                SovereignSpearItem.applyUpgrade(result);

                System.out.println("UPGRADED RESULT CREATED");
                System.out.println(result.get(DataComponents.CUSTOM_DATA));

                return result;
            }
        }

        System.out.println("NO SOVEREIGN SPEAR FOUND");
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(List.of(
                Ingredient.of(ModItems.PANDORA_CONTRACT.get()),
                Ingredient.of(ModItems.PANDORA_SOUL.get()),
                Ingredient.of(ModItems.SOVEREIGN_SPEAR.get())
        ));
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.SOVEREIGN_SPEAR_UPGRADE_SERIALIZER.get();
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
package jard.alchym;

import jard.alchym.api.ingredient.SolubleIngredient;
import jard.alchym.api.recipe.TransmutationRecipe;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.shape.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;

/***
 *  AlchymReference
 *  Central repository for all constant values used by the mod. Stores block and item identifiers, material
 *  types, entities, and such.
 *
 *  Created by jard at 9:15 PM on December 20, 2018.
 ***/
public class AlchymReference {
    public static final String MODID = "alchym";

    public static final ItemGroup ALCHYM_GROUP = FabricItemGroupBuilder.build (
            new Identifier (MODID, "main"),
            () -> new ItemStack (Alchym.getPhilosophersStone ())
    );

    // Any non-material block should have its definitions placed here.
    public enum Blocks {
        NITERROCK,

        COPPER_CRUCIBLE,
        CHYMICAL_ALEMBIC,

        SUNSTONE_BRICK,

        LEAD_ORE;

        public String getName () {
            return name ().toLowerCase ();
        }
    }

    public enum BlockEntities {
        CHYMICAL_CONTAINER;

        public String getName () {
            return name ().toLowerCase ();
        }
    }

    // Any non-material item should have its definitions placed here.
    public enum Items {
        ALCHYMIC_REFERENCE,

        CHYMICAL_TUBING,
        CHYMICAL_FLASK,

        REVOLVER,
        SPEEDLOADER,

        PHILOSOPHERS_STONE;

        public String getName () {
            return name ().toLowerCase ();
        }
    }

    public enum Entities {
        REVOLVER_BULLET;

        public String getName () { return name ().toLowerCase(); }
    }

    public static class WorldGen {
        public enum Features {
            NITER_DEPOSIT,
            LEAD_ORES;

            public String getName () {
                return name ().toLowerCase ();
            }
        }
    }

    public static final Item.Settings DEFAULT_ITEM_SETTINGS = new Item.Settings ()
            .group (AlchymReference.ALCHYM_GROUP);

    public static final Item.Settings SMALL_GLASSWARE_SETTINGS = new Item.Settings ()
            .group (AlchymReference.ALCHYM_GROUP).maxCount (100);
    public static final Item.Settings LARGE_GLASSWARE_SETTINGS = new Item.Settings ()
            .group (AlchymReference.ALCHYM_GROUP).maxCount (10);

    public static final Item.Settings TOOL_SETTINGS = new Item.Settings ().maxCount(1).maxDamage(0).rarity (Rarity.UNCOMMON)
            .group (AlchymReference.ALCHYM_GROUP);
    public static final Item.Settings PHILOSOPHERS_STONE_SETTINGS = new Item.Settings ().group (AlchymReference.ALCHYM_GROUP)
            .rarity (Rarity.EPIC).maxCount (1);
    public static final Item.Settings PHILOSOPHERS_STONE_SETTINGS$1 = new Item.Settings ().rarity (Rarity.EPIC)
            .maxCount (1);

    public static byte ITEM_STACK_LIMIT = 100;

    public enum Reagents {
        UNKNOWN,
        NITER,
        PHILOSOPHERS_STONE
    }

    public enum Sounds {
        REVOLVER_FIRE (new Identifier (MODID, "item.revolver.fire")),
        HITSOUND_1 (new Identifier (MODID, "misc.hitsound.1")),
        HITSOUND_2 (new Identifier (MODID, "misc.hitsound.2")),
        HITSOUND_3 (new Identifier (MODID, "misc.hitsound.3")),
        HITSOUND_4 (new Identifier (MODID, "misc.hitsound.4")),

        TRANSMUTE_DRY (new Identifier (MODID, "transmute.dry")),

        TRANSMUTE_FUMES (new Identifier (MODID, "transmute.fumes"));

        public final Identifier location;

        Sounds (Identifier loc) {
            location = loc;
        }

        public String getRegistryId () {
            return location.getPath ();
        }
    }

    public enum Particles {
        FIRE_ROCKET_TRAIL;

        public String getName () {
            return name ().toLowerCase ();
        }
    }

    public interface IMaterial {
        String getName ();
    }

    public enum Materials implements IMaterial {
        // Glass
        ALCHYMIC_GLASS (Forms.CRYSTAL, Forms.POWDER),

        // Reagent powders
        NITER (Forms.CRYSTAL, Forms.REAGENT_POWDER),
        PROJECTION_POWDER (Forms.REAGENT_POWDER),

        // Metals
        ALCHYMIC_GOLD (Forms.BLOCK, Forms.INGOT, Forms.NUGGET, Forms.POWDER),
        ALCHYMIC_SILVER (Forms.BLOCK, Forms.INGOT, Forms.NUGGET, Forms.POWDER),
        ALCHYMIC_STEEL (Forms.BLOCK, Forms.INGOT, Forms.NUGGET, Forms.POWDER),
        COPPER (Forms.NUGGET, Forms.POWDER),
        GOLD (Forms.POWDER),
        IRON (Forms.POWDER),
        LEAD (Forms.BLOCK, Forms.INGOT, Forms.NUGGET, Forms.POWDER),
        MERCURY (Forms.LIQUID),

        // Chymicals
        VITRIOL (Forms.CRYSTAL, Forms.POWDER, Forms.LIQUID),
        ASHEN_WASTE (Forms.POWDER);

        public enum Forms {
            /* BLOCK:                   A block of the material.
             *
             * INGOT:                   An ingot of the material.
             *
             * NUGGET:                  A nugget of the material, being 1/9th of a regular ingot.
             *
             * POWDER:                  A powdered form of the material.
             *
             * REAGENT_POWDER:          A powdered form of the material, except that it also overrides
             *                          MaterialItem#isTransmutationReagent to return true.
             *                            * Note: POWDER and REAGENT_POWDER are mutually exclusive, and this is enforced.
             *                              An exception will be raised if a material contains both of these.
             *
             * CRYSTAL:                 A crystalline form of the material.
             *
             * LIQUID:                  A liquid form of the material.
             */
            BLOCK (CorrespondingItem.BLOCK, 1000, 9),
            INGOT (CorrespondingItem.ITEM, 360, 9),
            NUGGET (CorrespondingItem.ITEM, 40, 9),
            POWDER (CorrespondingItem.ITEM, 360, 1),
            REAGENT_POWDER (CorrespondingItem.ITEM, 360, 1),
            CRYSTAL (CorrespondingItem.ITEM, 500, 1),
            LIQUID (CorrespondingItem.LIQUID, -1, 1);

            Forms (CorrespondingItem correspondingItem, long volume, int conversionFactor) {
                this.correspondingItem = correspondingItem;
                this.volume = volume;
                this.conversionFactor = conversionFactor;
            }

            private final CorrespondingItem correspondingItem;

            public final long volume;
            public final int conversionFactor;

            private enum CorrespondingItem {
                BLOCK, ITEM, LIQUID
            }

            public String getName () {
                return name ().toLowerCase ().replace ("reagent_", "");
            }

            public boolean isBlock () {
                return correspondingItem == CorrespondingItem.BLOCK;
            }

            public boolean isItem () {
                return correspondingItem == CorrespondingItem.ITEM;
            }

            public boolean isLiquid () {
                return correspondingItem == CorrespondingItem.LIQUID;
            }
        }

        public final java.util.List<Forms> forms;

        Materials (Forms... formsArgs) {
            if (formsArgs == null)
                forms = null;
            else
                forms = Collections.unmodifiableList (new ArrayList<> (Arrays.asList (formsArgs)));

            if (forms != null && forms.contains (Forms.POWDER) && forms.contains (Forms.REAGENT_POWDER))
                throw new RuntimeException ("The material '" + getName () + "' is in an illegal state: " +
                        "\"contains both a POWDER and REAGENT_POWDER form\"!");
        }

        public String getName () {
            return name ().toLowerCase ()
                    .replace ("_powder", ""); // Remove redundant powder suffix
        }
    }

    // Used by build.gradle to automatically generate block/stair/slab models, but otherwise are not used in the
    // block initialization module.
    public enum BuildingBlocks {
        SUNSTONE_BRICK;

        public String getName () { return name ().toLowerCase (); }
    } //$

    public enum AdditionalMaterials implements IMaterial {
        WATER (Fluids.WATER),
        SAND (net.minecraft.block.Blocks.SAND),
        VITRIOL (Alchym.content ().fluids.getMaterial (Materials.VITRIOL)),
        MERCURY (Alchym.content ().fluids.getMaterial (Materials.MERCURY));

        private Object outer;
        private static final Map <Object, AdditionalMaterials> existingSpeciesMaterials = new HashMap <> ();

        AdditionalMaterials (Object outer) {
            this.outer = outer;
        }

        public String getName () { return name ().toLowerCase (); }

        public static void initExistingSpecies () {
            if (! existingSpeciesMaterials.isEmpty ())
                return;

            for (AdditionalMaterials m : values ())
                existingSpeciesMaterials.put (m.outer, m);
        }

        public static AdditionalMaterials getExistingSpeciesMaterial (Object species) {
            return existingSpeciesMaterials.getOrDefault (species, null);
        }
    }

    public enum PhilosophersStoneCharges {
        LESSER  (0, 16 * 16 * 16),
        NORMAL  (LESSER.max, LESSER.max + 79 * 79 * 79),
        GREATER (NORMAL.max, NORMAL.max + 80 * 80 * 80);

        public final long min, max;

        PhilosophersStoneCharges (long min, long max) {
            this.min = min;
            this.max = max;
        }
    }

    // TODO: Move this to configuration file
    public static final double DRY_TRANSMUTATION_RADIUS = 4.00;

    public enum ChymicalContainers {
        COPPER_CRUCIBLE (1000 * 100, Block.createCuboidShape (1.0, 0.0, 1.0, 15.0, 14.5, 15.0),
                true,
                TransmutationRecipe.TransmutationType.CALCINATION,
                TransmutationRecipe.TransmutationType.SOLVATION,
                TransmutationRecipe.TransmutationType.COAGULATION),
        CHYMICAL_ALEMBIC (100, Block.createCuboidShape (4.0, 2.0, 4.0, 12.0, 16.0, 12.0),
                false,
                TransmutationRecipe.TransmutationType.DISTILLATION),
        EMPTY (0, Block.createCuboidShape (0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                false);

        public final long capacity;
        public final VoxelShape boundingBox;
        public final Set <TransmutationRecipe.TransmutationType> supportedOps;
        public final boolean canAcceptItems;

        ChymicalContainers (long capacity, VoxelShape boundingBox, boolean canAcceptItems,
                         TransmutationRecipe.TransmutationType ... types) {
            this.capacity = capacity;
            this.boundingBox = boundingBox;
            this.supportedOps = new HashSet<> (Arrays.asList (types));
            this.canAcceptItems = canAcceptItems;
        }
    }

    public enum FluidSolubilities {
        WATER (
                Fluids.WATER, 1.00f,
                Pair.of (Materials.NITER, (int) Materials.Forms.POWDER.volume * 8),
                Pair.of (Materials.VITRIOL, (int) Materials.Forms.POWDER.volume * 16),
                Pair.of (AdditionalMaterials.VITRIOL, -1)),
        LAVA (
                Fluids.LAVA, 3.10f),
        VITRIOL (
                Alchym.content ().fluids.getMaterial (Materials.VITRIOL), 1.75f
        ),
        MERCURY (
                Alchym.content ().fluids.getMaterial (Materials.MERCURY), 13.55f
        )
        ;

        public final Fluid fluid;
        public final float density;
        private final Map<IMaterial, Integer> solubilities;

        @SafeVarargs
        FluidSolubilities (Fluid fluid, float density, Pair <IMaterial, Integer> ... solubilitiesArgs) {
            this.fluid = fluid;
            this.density = density;

            if (solubilitiesArgs == null)
                solubilities = null;
            else {
                HashMap<IMaterial, Integer> toMap = new HashMap<> ();
                for (Pair <IMaterial, Integer> entry : solubilitiesArgs) {
                    toMap.put (entry.getKey (), entry.getValue ());
                }
                solubilities = Collections.unmodifiableMap (toMap);
            }
        }

        int getSolubility (IMaterial material) {
            return solubilities.getOrDefault (material, 0);
        }

        public static int getSolubility (Fluid fluid, SolubleIngredient solute) {
            if (solute.getMaterial () == null)
                return 0;

            for (FluidSolubilities solubility : FluidSolubilities.values ()) {
                if (solubility.fluid.equals (fluid))
                    return solubility.getSolubility (solute.getMaterial ());
            }

            // No entry was found that matches these. If it turns out that solute is a fluid and the input fluid is an SolubleIngredient,
            // we can then scan for the reverse scenario, with the caveat that we must normalize the resulting solubility so that
            // it describes the volume of fluid that can dissolve in solute.
            if (fluid instanceof SolubleIngredient && ((SolubleIngredient) fluid).getMaterial () != null && solute instanceof Fluid) {
                long val = 0;
                for (FluidSolubilities solubility : FluidSolubilities.values ()) {
                    if (solubility.fluid.equals (fluid))
                        val = solubility.getSolubility (((SolubleIngredient) fluid).getMaterial ());
                }

                if (val == -1)
                    return -1;
                else {
                    // val mB of solute / 1000 mB of solvent
                    // -> 1000 mB of solvent / val mB of solute
                    // -> 1000/val mB of solvent / 1 mB of solute
                    // -> 1000000/val mB of solvent / 1000 mB of solute

                    return (int) (1000000.f / (float) val);
                }
            }

            return 0;
        }
    }

    public enum Packets {
        OPEN_GUIDEBOOK  (new Identifier (MODID, "open_guidebook"), PacketPath.S2C),
        SYNC_GUIDEBOOK  (new Identifier (MODID, "sync_guidebook"), PacketPath.C2S),
        SERVER_REPLAY (new Identifier (MODID, "revolver_server_replay"), PacketPath.C2S),
        CLIENT_REPLAY (new Identifier (MODID, "revolver_client_replay"), PacketPath.S2C);

        enum PacketPath {
            C2S,
            S2C
        }

        public final Identifier id;
        final PacketPath path;

        Packets (Identifier id, PacketPath path) {
            this.id = id;
            this.path = path;
        }

        public boolean isClientbound () {
            return path == PacketPath.S2C;
        }

        public boolean isServerbound () {
            return path == PacketPath.C2S;
        }
    }


    public static class PageInfo {
        private static final String NON_SYNTAX_CHARACTERS = "[^\\\\{}]+";
        private static final Identifier TITLE_FONT = new Identifier (MODID, "page_title");

        public enum BookSide {
            LEFT,
            RIGHT
        }

        public enum ContentTextStyles {
            TITLE    ("\\\\title\\{(" + NON_SYNTAX_CHARACTERS + ")\\}$",    Style.EMPTY.withFont (TITLE_FONT).withColor (TextColor.fromRgb (0xff600c2e)), true),
            SUBTITLE ("\\\\subtitle\\{(" + NON_SYNTAX_CHARACTERS + ")\\}$", Style.EMPTY.withBold (true).withColor (TextColor.fromRgb (0xff600c2e)), true),
            HEADING  ("\\\\heading\\{(" + NON_SYNTAX_CHARACTERS + ")\\}$",   Style.EMPTY.withBold (true).withColor (TextColor.fromRgb (0xff600c2f)), false),
            EMPHASIS ("\\\\emphasis\\{(" + NON_SYNTAX_CHARACTERS + ")\\}",  Style.EMPTY.withItalic (true).withColor (TextColor.fromRgb (0xff230005)), false),
            BODY     (NON_SYNTAX_CHARACTERS, Style.EMPTY.withColor (TextColor.fromRgb (0xff230005)), false);

            public final Pattern pattern;
            public final Style style;
            public final boolean omitNewline;

            ContentTextStyles (String regex, Style style, boolean omit) {
                this.pattern = Pattern.compile ("^" + regex);
                this.style = style;
                this.omitNewline = omit;
            }

            public boolean matches (String content) {
                return pattern.matcher (content).matches ();
            }
        }

        public static final int PAGE_WIDTH = 110;
        public static final int PAGE_HEIGHT = 164;
    }

    public enum RevolverAction {
        SPLASH,
        DIRECT,
        BULLET
    }
}

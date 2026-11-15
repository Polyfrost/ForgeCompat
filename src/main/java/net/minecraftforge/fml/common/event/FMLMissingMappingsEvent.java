package net.minecraftforge.fml.common.event;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.resource.Identifier;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class FMLMissingMappingsEvent extends FMLEvent {
    private final ListMultimap<String, MissingMapping> missing;
    private ModContainer active;

    public FMLMissingMappingsEvent(ListMultimap<String, MissingMapping> missing) {
        this.missing = missing;
    }

    public void applyModContainer(ModContainer container) {
        active = container;
    }

    public List<MissingMapping> get() {
        return active == null ? ImmutableList.of() : ImmutableList.copyOf(missing.get(active.getModId()));
    }

    public List<MissingMapping> getAll() {
        return ImmutableList.copyOf(missing.values());
    }

    public enum Action {
        DEFAULT, IGNORE, WARN, FAIL, REMAP, BLOCKONLY
    }

    public static class MissingMapping {
        public final GameRegistry.Type type;
        public final String name;
        public final int id;
        public final Identifier resourceLocation;
        private Action action = Action.DEFAULT;
        private Object target;

        public MissingMapping(GameRegistry.Type type, Identifier resourceLocation, int id) {
            this.type = type;
            this.resourceLocation = resourceLocation;
            this.name = resourceLocation.toString();
            this.id = id;
        }

        public void ignore() {
            action = Action.IGNORE;
        }

        public void warn() {
            action = Action.WARN;
        }

        public void fail() {
            action = Action.FAIL;
        }

        public void skipItemBlock() {
            action = Action.BLOCKONLY;
        }

        public void remap(Block target) {
            action = Action.REMAP;
            this.target = target;
        }

        public void remap(Item target) {
            action = Action.REMAP;
            this.target = target;
        }

        public Action getAction() {
            return action;
        }

        public Object getTarget() {
            return target;
        }
    }
}

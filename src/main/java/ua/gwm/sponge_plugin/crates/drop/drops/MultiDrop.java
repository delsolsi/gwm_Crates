package ua.gwm.sponge_plugin.crates.drop.drops;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.*;

public class MultiDrop extends Drop {

    private List<Drop> drops;
    private boolean give_all = true;

    public MultiDrop(ConfigurationNode node) {
        super(node);
        try {
            ConfigurationNode drops_node = node.getNode("DROPS");
            ConfigurationNode give_all_node = node.getNode("GIVE_ALL");
            if (drops_node.isVirtual()) {
                throw new RuntimeException("DROPS node does not exist");
            }
            drops = new ArrayList<Drop>();
            for (ConfigurationNode drop_node : drops_node.getChildrenList()) {
                ConfigurationNode drop_type_node = drop_node.getNode("TYPE");
                if (drop_type_node.isVirtual()) {
                    throw new RuntimeException("TYPE node does not exist!");
                }
                String drop_type = drop_type_node.getString();
                if (!GWMCrates.getInstance().getDrops().containsKey(drop_type)) {
                    throw new RuntimeException("Drop type \"" + drop_type + "\" not found!");
                }
                try {
                    Class<? extends Drop> drop_class = GWMCrates.getInstance().getDrops().get(drop_type);
                    Constructor<? extends Drop> drop_constructor = drop_class.getConstructor(ConfigurationNode.class);
                    Drop drop = drop_constructor.newInstance(drop_node);
                    drops.add(drop);
                } catch (Exception e) {
                    throw new RuntimeException("Exception creating Drop (part of Multi Drop)!", e);
                }
            }
            give_all = give_all_node.getBoolean(true);
        } catch (Exception e) {
            throw new RuntimeException("Exception creating Multi Drop!", e);
        }
    }

    public MultiDrop(Optional<String> id, Optional<BigDecimal> price, Optional<ItemStack> drop_item, int level,
                     List<Drop> drops, boolean give_all) {
        super(id, price, drop_item, level);
        this.drops = drops;
        this.give_all = give_all;
    }

    @Override
    public void apply(Player player) {
        if (give_all) {
            drops.forEach(drop -> drop.apply(player));
        } else {
            GWMCratesUtils.chooseDropByLevel(drops, player, false).apply(player);
        }
    }

    public List<Drop> getDrops() {
        return drops;
    }

    public void setDrops(List<Drop> drops) {
        this.drops = drops;
    }
}

package mekanism.common.content.transmitter;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.IChunk;

public class InventoryNetwork extends DynamicNetwork<TileEntity, InventoryNetwork, TileEntityLogisticalTransporterBase> {

    public InventoryNetwork() {
    }

    public InventoryNetwork(UUID networkID) {
        super(networkID);
    }

    public InventoryNetwork(Collection<InventoryNetwork> networks) {
        for (InventoryNetwork net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }
        register();
    }

    public List<AcceptorData> calculateAcceptors(TransitRequest request, TransporterStack stack, Long2ObjectMap<IChunk> chunkMap) {
        List<AcceptorData> toReturn = new ArrayList<>();
        for (BlockPos pos : possibleAcceptors) {
            if (pos == null || pos.equals(stack.homeLocation)) {
                continue;
            }
            Set<Direction> sides = acceptorDirections.get(pos);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity acceptor = MekanismUtils.getTileEntity(getWorld(), chunkMap, pos);
            if (acceptor == null) {
                continue;
            }

            AcceptorData data = null;
            for (Direction side : sides) {
                Direction opposite = side.getOpposite();
                TransitResponse response = TransporterManager.getPredictedInsert(acceptor, stack.color, request, opposite);
                if (!response.isEmpty()) {
                    if (data == null) {
                        toReturn.add(data = new AcceptorData(pos, response, opposite));
                    } else {
                        data.sides.add(opposite);
                    }
                }
            }
        }
        return toReturn;
    }

    @Override
    public void commit() {
        super.commit();
        // update the cache when the network has been changed (called when transmitters are added)
        PathfinderCache.onChanged(this);
    }

    @Override
    public void deregister() {
        super.deregister();
        // update the cache when the network has been removed (when transmitters are removed)
        PathfinderCache.onChanged(this);
    }

    @Override
    public String toString() {
        return "[InventoryNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public ITextComponent getNeededInfo() {
        return null;
    }

    @Override
    public ITextComponent getStoredInfo() {
        return null;
    }

    @Override
    public ITextComponent getFlowInfo() {
        return null;
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.INVENTORY_NETWORK, transmitters.size(), possibleAcceptors.size());
    }

    public static class AcceptorData {

        private final BlockPos location;
        private final TransitResponse response;
        private final Set<Direction> sides;

        protected AcceptorData(BlockPos pos, TransitResponse ret, Direction side) {
            location = pos;
            response = ret;
            sides = EnumSet.of(side);
        }

        public TransitResponse getResponse() {
            return response;
        }

        public BlockPos getLocation() {
            return location;
        }

        public Set<Direction> getSides() {
            return sides;
        }
    }
}
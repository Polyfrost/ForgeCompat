package org.polyfrost.forgecompat;

import net.minecraftforge.client.IRenderHandler;

public interface ForgeDimension {
    IRenderHandler getSkyRenderer();

    IRenderHandler getCloudRenderer();

    IRenderHandler getWeatherRenderer();
}

package com.ashkiano.customizablesilktouch.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class SilkTouchData {

    private final String id;
    private final List<Material> toolItems;
    private final List<Material> silkTouchBlocks;
    private final HashMap<Material, Material> blockedBlocks;

}

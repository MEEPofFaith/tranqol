package tranqol.world.blocks.distribution;

import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

public class MultiSorter extends Block{
    public float speed = 5f;

    public MultiSorter(String name){
        super(name);
    }

    public class MultiSorterBuild extends Building{
        public @Nullable Item leftSort, frontSort, rightSort;
    }
}

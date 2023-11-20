package net.rubyhorizon.campfires.configuration.campfire;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder(access = AccessLevel.PACKAGE)
@ToString
@Getter
public class TorchConfiguration {
    private boolean torch;
    private boolean soulTorch;
    private boolean redStoneTorch;
}

package com.github.apace100.apoli.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import java.util.ArrayList;
import java.util.HashMap;

public class ApoliAttachmentTypes {
    public static final AttachmentType<PowerHolderAttachment> POWER_HOLDER_ATTACHMENT = AttachmentRegistry.<PowerHolderAttachment>builder()
            .persistent(PowerHolderAttachment.CODEC)
            .initializer(() -> new PowerHolderAttachment(new ArrayList<>(), new HashMap<>(), new HashMap<>()))
            .copyOnDeath()
            .buildAndRegister(PowerHolderAttachment.ID);

    public static void register() {
    }

}

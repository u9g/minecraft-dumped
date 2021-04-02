package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.util.LevelType;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

public class RealmsResetNormalWorldScreen extends RealmsScreen {
   private static final Component SEED_LABEL = new TranslatableComponent("mco.reset.world.seed");
   private final Consumer<WorldGenerationInfo> callback;
   private RealmsLabel titleLabel;
   private EditBox seedEdit;
   private LevelType levelType;
   private boolean generateStructures;
   private final Component buttonTitle;

   public RealmsResetNormalWorldScreen(Consumer<WorldGenerationInfo> var1, Component var2) {
      super();
      this.levelType = LevelType.DEFAULT;
      this.generateStructures = true;
      this.callback = var1;
      this.buttonTitle = var2;
   }

   public void tick() {
      this.seedEdit.tick();
      super.tick();
   }

   public void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.titleLabel = new RealmsLabel(new TranslatableComponent("mco.reset.world.generate"), this.width / 2, 17, 16777215);
      this.addWidget(this.titleLabel);
      this.seedEdit = new EditBox(this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, (EditBox)null, new TranslatableComponent("mco.reset.world.seed"));
      this.seedEdit.setMaxLength(32);
      this.addWidget(this.seedEdit);
      this.setInitialFocus(this.seedEdit);
      this.addButton(CycleButton.builder(LevelType::getName).withValues((Object[])LevelType.values()).withInitialValue(this.levelType).create(this.width / 2 - 102, row(4), 205, 20, new TranslatableComponent("selectWorld.mapType"), (var1, var2) -> {
         this.levelType = var2;
      }));
      this.addButton(CycleButton.onOffBuilder(this.generateStructures).create(this.width / 2 - 102, row(6) - 2, 205, 20, new TranslatableComponent("selectWorld.mapFeatures"), (var1, var2) -> {
         this.generateStructures = var2;
      }));
      this.addButton(new Button(this.width / 2 - 102, row(12), 97, 20, this.buttonTitle, (var1) -> {
         this.callback.accept(new WorldGenerationInfo(this.seedEdit.getValue(), this.levelType, this.generateStructures));
      }));
      this.addButton(new Button(this.width / 2 + 8, row(12), 97, 20, CommonComponents.GUI_BACK, (var1) -> {
         this.onClose();
      }));
      this.narrateLabels();
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public void onClose() {
      this.callback.accept((Object)null);
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      this.renderBackground(var1);
      this.titleLabel.render(this, var1);
      this.font.draw(var1, SEED_LABEL, (float)(this.width / 2 - 100), (float)row(1), 10526880);
      this.seedEdit.render(var1, var2, var3, var4);
      super.render(var1, var2, var3, var4);
   }
}

package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsMainScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
   private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
   private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
   private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
   private static final ResourceLocation LEAVE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/leave_icon.png");
   private static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
   private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
   private static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
   private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
   private static final ResourceLocation CONFIGURE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/configure_icon.png");
   private static final ResourceLocation QUESTIONMARK_LOCATION = new ResourceLocation("realms", "textures/gui/realms/questionmark.png");
   private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
   private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
   private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
   private static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
   private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
   private static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("minecraft", "textures/gui/widgets.png");
   private static final Component NO_PENDING_INVITES_TEXT = new TranslatableComponent("mco.invites.nopending");
   private static final Component PENDING_INVITES_TEXT = new TranslatableComponent("mco.invites.pending");
   private static final List<Component> TRIAL_MESSAGE_LINES = ImmutableList.of(new TranslatableComponent("mco.trial.message.line1"), new TranslatableComponent("mco.trial.message.line2"));
   private static final Component SERVER_UNITIALIZED_TEXT = new TranslatableComponent("mco.selectServer.uninitialized");
   private static final Component SUBSCRIPTION_EXPIRED_TEXT = new TranslatableComponent("mco.selectServer.expiredList");
   private static final Component SUBSCRIPTION_RENEW_TEXT = new TranslatableComponent("mco.selectServer.expiredRenew");
   private static final Component TRIAL_EXPIRED_TEXT = new TranslatableComponent("mco.selectServer.expiredTrial");
   private static final Component SUBSCRIPTION_CREATE_TEXT = new TranslatableComponent("mco.selectServer.expiredSubscribe");
   private static final Component SELECT_MINIGAME_PREFIX = (new TranslatableComponent("mco.selectServer.minigame")).append(" ");
   private static final Component POPUP_TEXT = new TranslatableComponent("mco.selectServer.popup");
   private static final Component SERVER_EXPIRED_TOOLTIP = new TranslatableComponent("mco.selectServer.expired");
   private static final Component SERVER_EXPIRES_SOON_TOOLTIP = new TranslatableComponent("mco.selectServer.expires.soon");
   private static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = new TranslatableComponent("mco.selectServer.expires.day");
   private static final Component SERVER_OPEN_TOOLTIP = new TranslatableComponent("mco.selectServer.open");
   private static final Component SERVER_CLOSED_TOOLTIP = new TranslatableComponent("mco.selectServer.closed");
   private static final Component LEAVE_SERVER_TOOLTIP = new TranslatableComponent("mco.selectServer.leave");
   private static final Component CONFIGURE_SERVER_TOOLTIP = new TranslatableComponent("mco.selectServer.configure");
   private static final Component SERVER_INFO_TOOLTIP = new TranslatableComponent("mco.selectServer.info");
   private static final Component NEWS_TOOLTIP = new TranslatableComponent("mco.news");
   private static List<ResourceLocation> teaserImages = ImmutableList.of();
   private static final RealmsDataFetcher REALMS_DATA_FETCHER = new RealmsDataFetcher(Minecraft.getInstance(), RealmsClient.create());
   private static boolean overrideConfigure;
   private static int lastScrollYPosition = -1;
   private static volatile boolean hasParentalConsent;
   private static volatile boolean checkedParentalConsent;
   private static volatile boolean checkedClientCompatability;
   private static Screen realmsGenericErrorScreen;
   private static boolean regionsPinged;
   private final RateLimiter inviteNarrationLimiter;
   private boolean dontSetConnectedToRealms;
   private final Screen lastScreen;
   private volatile RealmsMainScreen.RealmSelectionList realmSelectionList;
   private long selectedServerId = -1L;
   private Button playButton;
   private Button backButton;
   private Button renewButton;
   private Button configureButton;
   private Button leaveButton;
   private List<Component> toolTip;
   private List<RealmsServer> realmsServers = Lists.newArrayList();
   private volatile int numberOfPendingInvites;
   private int animTick;
   private boolean hasFetchedServers;
   private boolean popupOpenedByUser;
   private boolean justClosedPopup;
   private volatile boolean trialsAvailable;
   private volatile boolean createdTrial;
   private volatile boolean showingPopup;
   private volatile boolean hasUnreadNews;
   private volatile String newsLink;
   private int carouselIndex;
   private int carouselTick;
   private boolean hasSwitchedCarouselImage;
   private List<KeyCombo> keyCombos;
   private int clicks;
   private ReentrantLock connectLock = new ReentrantLock();
   private MultiLineLabel formattedPopup;
   private RealmsMainScreen.HoveredElement hoveredElement;
   private Button showPopupButton;
   private Button pendingInvitesButton;
   private Button newsButton;
   private Button createTrialButton;
   private Button buyARealmButton;
   private Button closeButton;

   public RealmsMainScreen(Screen var1) {
      super();
      this.formattedPopup = MultiLineLabel.EMPTY;
      this.lastScreen = var1;
      this.inviteNarrationLimiter = RateLimiter.create(0.01666666753590107D);
   }

   private boolean shouldShowMessageInList() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         if (this.trialsAvailable && !this.createdTrial) {
            return true;
         } else {
            Iterator var1 = this.realmsServers.iterator();

            RealmsServer var2;
            do {
               if (!var1.hasNext()) {
                  return true;
               }

               var2 = (RealmsServer)var1.next();
            } while(!var2.ownerUUID.equals(this.minecraft.getUser().getUuid()));

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean shouldShowPopup() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         if (this.popupOpenedByUser) {
            return true;
         } else {
            return this.trialsAvailable && !this.createdTrial && this.realmsServers.isEmpty() ? true : this.realmsServers.isEmpty();
         }
      } else {
         return false;
      }
   }

   public void init() {
      this.keyCombos = Lists.newArrayList(new KeyCombo[]{new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> {
         overrideConfigure = !overrideConfigure;
      }), new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
            this.switchToProd();
         } else {
            this.switchToStage();
         }

      }), new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
            this.switchToProd();
         } else {
            this.switchToLocal();
         }

      })});
      if (realmsGenericErrorScreen != null) {
         this.minecraft.setScreen(realmsGenericErrorScreen);
      } else {
         this.connectLock = new ReentrantLock();
         if (checkedClientCompatability && !hasParentalConsent()) {
            this.checkParentalConsent();
         }

         this.checkClientCompatability();
         this.checkUnreadNews();
         if (!this.dontSetConnectedToRealms) {
            this.minecraft.setConnectedToRealms(false);
         }

         this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
         if (hasParentalConsent()) {
            REALMS_DATA_FETCHER.forceUpdate();
         }

         this.showingPopup = false;
         if (hasParentalConsent() && this.hasFetchedServers) {
            this.addButtons();
         }

         this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
         if (lastScrollYPosition != -1) {
            this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
         }

         this.addWidget(this.realmSelectionList);
         this.magicalSpecialHackyFocus(this.realmSelectionList);
         this.formattedPopup = MultiLineLabel.create(this.font, POPUP_TEXT, 100);
      }
   }

   private static boolean hasParentalConsent() {
      return checkedParentalConsent && hasParentalConsent;
   }

   public void addButtons() {
      this.leaveButton = (Button)this.addButton(new Button(this.width / 2 - 202, this.height - 32, 90, 20, new TranslatableComponent("mco.selectServer.leave"), (var1x) -> {
         this.leaveClicked(this.findServer(this.selectedServerId));
      }));
      this.configureButton = (Button)this.addButton(new Button(this.width / 2 - 190, this.height - 32, 90, 20, new TranslatableComponent("mco.selectServer.configure"), (var1x) -> {
         this.configureClicked(this.findServer(this.selectedServerId));
      }));
      this.playButton = (Button)this.addButton(new Button(this.width / 2 - 93, this.height - 32, 90, 20, new TranslatableComponent("mco.selectServer.play"), (var1x) -> {
         RealmsServer var2 = this.findServer(this.selectedServerId);
         if (var2 != null) {
            this.play(var2, this);
         }
      }));
      this.backButton = (Button)this.addButton(new Button(this.width / 2 + 4, this.height - 32, 90, 20, CommonComponents.GUI_BACK, (var1x) -> {
         if (!this.justClosedPopup) {
            this.minecraft.setScreen(this.lastScreen);
         }

      }));
      this.renewButton = (Button)this.addButton(new Button(this.width / 2 + 100, this.height - 32, 90, 20, new TranslatableComponent("mco.selectServer.expiredRenew"), (var1x) -> {
         this.onRenew();
      }));
      this.pendingInvitesButton = (Button)this.addButton(new RealmsMainScreen.PendingInvitesButton());
      this.newsButton = (Button)this.addButton(new RealmsMainScreen.NewsButton());
      this.showPopupButton = (Button)this.addButton(new RealmsMainScreen.ShowPopupButton());
      this.closeButton = (Button)this.addButton(new RealmsMainScreen.CloseButton());
      this.createTrialButton = (Button)this.addButton(new Button(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20, new TranslatableComponent("mco.selectServer.trial"), (var1x) -> {
         if (this.trialsAvailable && !this.createdTrial) {
            Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
            this.minecraft.setScreen(this.lastScreen);
         }
      }));
      this.buyARealmButton = (Button)this.addButton(new Button(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20, new TranslatableComponent("mco.selectServer.buy"), (var0) -> {
         Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms");
      }));
      RealmsServer var1 = this.findServer(this.selectedServerId);
      this.updateButtonStates(var1);
   }

   private void updateButtonStates(@Nullable RealmsServer var1) {
      this.playButton.active = this.shouldPlayButtonBeActive(var1) && !this.shouldShowPopup();
      this.renewButton.visible = this.shouldRenewButtonBeActive(var1);
      this.configureButton.visible = this.shouldConfigureButtonBeVisible(var1);
      this.leaveButton.visible = this.shouldLeaveButtonBeVisible(var1);
      boolean var2 = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
      this.createTrialButton.visible = var2;
      this.createTrialButton.active = var2;
      this.buyARealmButton.visible = this.shouldShowPopup();
      this.closeButton.visible = this.shouldShowPopup() && this.popupOpenedByUser;
      this.renewButton.active = !this.shouldShowPopup();
      this.configureButton.active = !this.shouldShowPopup();
      this.leaveButton.active = !this.shouldShowPopup();
      this.newsButton.active = true;
      this.pendingInvitesButton.active = true;
      this.backButton.active = true;
      this.showPopupButton.active = !this.shouldShowPopup();
   }

   private boolean shouldShowPopupButton() {
      return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
   }

   private boolean shouldPlayButtonBeActive(@Nullable RealmsServer var1) {
      return var1 != null && !var1.expired && var1.state == RealmsServer.State.OPEN;
   }

   private boolean shouldRenewButtonBeActive(@Nullable RealmsServer var1) {
      return var1 != null && var1.expired && this.isSelfOwnedServer(var1);
   }

   private boolean shouldConfigureButtonBeVisible(@Nullable RealmsServer var1) {
      return var1 != null && this.isSelfOwnedServer(var1);
   }

   private boolean shouldLeaveButtonBeVisible(@Nullable RealmsServer var1) {
      return var1 != null && !this.isSelfOwnedServer(var1);
   }

   public void tick() {
      super.tick();
      this.justClosedPopup = false;
      ++this.animTick;
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

      if (hasParentalConsent()) {
         REALMS_DATA_FETCHER.init();
         Iterator var4;
         RealmsServer var5;
         if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
            List var1 = REALMS_DATA_FETCHER.getServers();
            this.realmSelectionList.clear();
            boolean var2 = !this.hasFetchedServers;
            if (var2) {
               this.hasFetchedServers = true;
            }

            if (var1 != null) {
               boolean var3 = false;
               var4 = var1.iterator();

               while(var4.hasNext()) {
                  var5 = (RealmsServer)var4.next();
                  if (this.isSelfOwnedNonExpiredServer(var5)) {
                     var3 = true;
                  }
               }

               this.realmsServers = var1;
               if (this.shouldShowMessageInList()) {
                  this.realmSelectionList.addMessageEntry(new RealmsMainScreen.TrialEntry());
               }

               var4 = this.realmsServers.iterator();

               while(var4.hasNext()) {
                  var5 = (RealmsServer)var4.next();
                  this.realmSelectionList.addEntry(new RealmsMainScreen.ServerEntry(var5));
               }

               if (!regionsPinged && var3) {
                  regionsPinged = true;
                  this.pingRegions();
               }
            }

            if (var2) {
               this.addButtons();
            }
         }

         if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
            this.numberOfPendingInvites = REALMS_DATA_FETCHER.getPendingInvitesCount();
            if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
               NarrationHelper.now(I18n.get("mco.configure.world.invite.narration", this.numberOfPendingInvites));
            }
         }

         if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
            boolean var6 = REALMS_DATA_FETCHER.isTrialAvailable();
            if (var6 != this.trialsAvailable && this.shouldShowPopup()) {
               this.trialsAvailable = var6;
               this.showingPopup = false;
            } else {
               this.trialsAvailable = var6;
            }
         }

         if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
            RealmsServerPlayerLists var7 = REALMS_DATA_FETCHER.getLivestats();
            Iterator var8 = var7.servers.iterator();

            label87:
            while(true) {
               while(true) {
                  if (!var8.hasNext()) {
                     break label87;
                  }

                  RealmsServerPlayerList var9 = (RealmsServerPlayerList)var8.next();
                  var4 = this.realmsServers.iterator();

                  while(var4.hasNext()) {
                     var5 = (RealmsServer)var4.next();
                     if (var5.id == var9.serverId) {
                        var5.updateServerPing(var9);
                        break;
                     }
                  }
               }
            }
         }

         if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
            this.hasUnreadNews = REALMS_DATA_FETCHER.hasUnreadNews();
            this.newsLink = REALMS_DATA_FETCHER.newsLink();
         }

         REALMS_DATA_FETCHER.markClean();
         if (this.shouldShowPopup()) {
            ++this.carouselTick;
         }

         if (this.showPopupButton != null) {
            this.showPopupButton.visible = this.shouldShowPopupButton();
         }

      }
   }

   private void pingRegions() {
      (new Thread(() -> {
         List var1 = Ping.pingAllRegions();
         RealmsClient var2 = RealmsClient.create();
         PingResult var3 = new PingResult();
         var3.pingResults = var1;
         var3.worldIds = this.getOwnedNonExpiredWorldIds();

         try {
            var2.sendPingResults(var3);
         } catch (Throwable var5) {
            LOGGER.warn("Could not send ping result to Realms: ", var5);
         }

      })).start();
   }

   private List<Long> getOwnedNonExpiredWorldIds() {
      ArrayList var1 = Lists.newArrayList();
      Iterator var2 = this.realmsServers.iterator();

      while(var2.hasNext()) {
         RealmsServer var3 = (RealmsServer)var2.next();
         if (this.isSelfOwnedNonExpiredServer(var3)) {
            var1.add(var3.id);
         }
      }

      return var1;
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
      this.stopRealmsFetcher();
   }

   private void onRenew() {
      RealmsServer var1 = this.findServer(this.selectedServerId);
      if (var1 != null) {
         String var2 = "https://aka.ms/ExtendJavaRealms?subscriptionId=" + var1.remoteSubscriptionId + "&profileId=" + this.minecraft.getUser().getUuid() + "&ref=" + (var1.expiredTrial ? "expiredTrial" : "expiredRealm");
         this.minecraft.keyboardHandler.setClipboard(var2);
         Util.getPlatform().openUri(var2);
      }
   }

   private void checkClientCompatability() {
      if (!checkedClientCompatability) {
         checkedClientCompatability = true;
         (new Thread("MCO Compatability Checker #1") {
            public void run() {
               RealmsClient var1 = RealmsClient.create();

               try {
                  RealmsClient.CompatibleVersionResponse var2 = var1.clientCompatible();
                  if (var2 == RealmsClient.CompatibleVersionResponse.OUTDATED) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                     return;
                  }

                  if (var2 == RealmsClient.CompatibleVersionResponse.OTHER) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                     return;
                  }

                  RealmsMainScreen.this.checkParentalConsent();
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.checkedClientCompatability = false;
                  RealmsMainScreen.LOGGER.error("Couldn't connect to realms", var3);
                  if (var3.httpResultCode == 401) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(new TranslatableComponent("mco.error.invalid.session.title"), new TranslatableComponent("mco.error.invalid.session.message"), RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                  } else {
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
                     });
                  }
               }

            }
         }).start();
      }

   }

   private void checkUnreadNews() {
   }

   private void checkParentalConsent() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            RealmsClient var1 = RealmsClient.create();

            try {
               Boolean var2 = var1.mcoEnabled();
               if (var2) {
                  RealmsMainScreen.LOGGER.info("Realms is available for this user");
                  RealmsMainScreen.hasParentalConsent = true;
               } else {
                  RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                  RealmsMainScreen.hasParentalConsent = false;
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
                  });
               }

               RealmsMainScreen.checkedParentalConsent = true;
            } catch (RealmsServiceException var3) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms", var3);
               RealmsMainScreen.this.minecraft.execute(() -> {
                  RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
               });
            }

         }
      }).start();
   }

   private void switchToStage() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               RealmsClient var1 = RealmsClient.create();

               try {
                  Boolean var2 = var1.stageAvailable();
                  if (var2) {
                     RealmsClient.switchToStage();
                     RealmsMainScreen.LOGGER.info("Switched to stage");
                     RealmsMainScreen.REALMS_DATA_FETCHER.forceUpdate();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
               }

            }
         }).start();
      }

   }

   private void switchToLocal() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               RealmsClient var1 = RealmsClient.create();

               try {
                  Boolean var2 = var1.stageAvailable();
                  if (var2) {
                     RealmsClient.switchToLocal();
                     RealmsMainScreen.LOGGER.info("Switched to local");
                     RealmsMainScreen.REALMS_DATA_FETCHER.forceUpdate();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
               }

            }
         }).start();
      }

   }

   private void switchToProd() {
      RealmsClient.switchToProd();
      REALMS_DATA_FETCHER.forceUpdate();
   }

   private void stopRealmsFetcher() {
      REALMS_DATA_FETCHER.stop();
   }

   private void configureClicked(RealmsServer var1) {
      if (this.minecraft.getUser().getUuid().equals(var1.ownerUUID) || overrideConfigure) {
         this.saveListScrollPosition();
         this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, var1.id));
      }

   }

   private void leaveClicked(@Nullable RealmsServer var1) {
      if (var1 != null && !this.minecraft.getUser().getUuid().equals(var1.ownerUUID)) {
         this.saveListScrollPosition();
         TranslatableComponent var2 = new TranslatableComponent("mco.configure.world.leave.question.line1");
         TranslatableComponent var3 = new TranslatableComponent("mco.configure.world.leave.question.line2");
         this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::leaveServer, RealmsLongConfirmationScreen.Type.Info, var2, var3, true));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
   }

   @Nullable
   private RealmsServer findServer(long var1) {
      Iterator var3 = this.realmsServers.iterator();

      RealmsServer var4;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         var4 = (RealmsServer)var3.next();
      } while(var4.id != var1);

      return var4;
   }

   private void leaveServer(boolean var1) {
      if (var1) {
         (new Thread("Realms-leave-server") {
            public void run() {
               try {
                  RealmsServer var1 = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
                  if (var1 != null) {
                     RealmsClient var2 = RealmsClient.create();
                     var2.uninviteMyselfFrom(var1.id);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.removeServer(var1);
                     });
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't configure world");
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this));
                  });
               }

            }
         }).start();
      }

      this.minecraft.setScreen(this);
   }

   private void removeServer(RealmsServer var1) {
      REALMS_DATA_FETCHER.removeItem(var1);
      this.realmsServers.remove(var1);
      this.realmSelectionList.children().removeIf((var1x) -> {
         return var1x instanceof RealmsMainScreen.ServerEntry && ((RealmsMainScreen.ServerEntry)var1x).serverData.id == this.selectedServerId;
      });
      this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
      this.updateButtonStates((RealmsServer)null);
      this.selectedServerId = -1L;
      this.playButton.active = false;
   }

   public void removeSelection() {
      this.selectedServerId = -1L;
   }

   public boolean keyPressed(int var1, int var2, int var3) {
      if (var1 == 256) {
         this.keyCombos.forEach(KeyCombo::reset);
         this.onClosePopup();
         return true;
      } else {
         return super.keyPressed(var1, var2, var3);
      }
   }

   private void onClosePopup() {
      if (this.shouldShowPopup() && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
      } else {
         this.minecraft.setScreen(this.lastScreen);
      }

   }

   public boolean charTyped(char var1, int var2) {
      this.keyCombos.forEach((var1x) -> {
         var1x.keyPressed(var1);
      });
      return true;
   }

   public void render(PoseStack var1, int var2, int var3, float var4) {
      this.hoveredElement = RealmsMainScreen.HoveredElement.NONE;
      this.toolTip = null;
      this.renderBackground(var1);
      this.realmSelectionList.render(var1, var2, var3, var4);
      this.drawRealmsLogo(var1, this.width / 2 - 50, 7);
      if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
         this.renderStage(var1);
      }

      if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
         this.renderLocal(var1);
      }

      if (this.shouldShowPopup()) {
         this.drawPopup(var1, var2, var3);
      } else {
         if (this.showingPopup) {
            this.updateButtonStates((RealmsServer)null);
            if (!this.children.contains(this.realmSelectionList)) {
               this.children.add(this.realmSelectionList);
            }

            RealmsServer var5 = this.findServer(this.selectedServerId);
            this.playButton.active = this.shouldPlayButtonBeActive(var5);
         }

         this.showingPopup = false;
      }

      super.render(var1, var2, var3, var4);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(var1, this.toolTip, var2, var3);
      }

      if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
         this.minecraft.getTextureManager().bind(TRIAL_ICON_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         boolean var8 = true;
         boolean var6 = true;
         byte var7 = 0;
         if ((Util.getMillis() / 800L & 1L) == 1L) {
            var7 = 8;
         }

         GuiComponent.blit(var1, this.createTrialButton.x + this.createTrialButton.getWidth() - 8 - 4, this.createTrialButton.y + this.createTrialButton.getHeight() / 2 - 4, 0.0F, (float)var7, 8, 8, 8, 16);
      }

   }

   private void drawRealmsLogo(PoseStack var1, int var2, int var3) {
      this.minecraft.getTextureManager().bind(LOGO_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.pushMatrix();
      RenderSystem.scalef(0.5F, 0.5F, 0.5F);
      GuiComponent.blit(var1, var2 * 2, var3 * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
      RenderSystem.popMatrix();
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      if (this.isOutsidePopup(var1, var3) && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
         this.justClosedPopup = true;
         return true;
      } else {
         return super.mouseClicked(var1, var3, var5);
      }
   }

   private boolean isOutsidePopup(double var1, double var3) {
      int var5 = this.popupX0();
      int var6 = this.popupY0();
      return var1 < (double)(var5 - 5) || var1 > (double)(var5 + 315) || var3 < (double)(var6 - 5) || var3 > (double)(var6 + 171);
   }

   private void drawPopup(PoseStack var1, int var2, int var3) {
      int var4 = this.popupX0();
      int var5 = this.popupY0();
      if (!this.showingPopup) {
         this.carouselIndex = 0;
         this.carouselTick = 0;
         this.hasSwitchedCarouselImage = true;
         this.updateButtonStates((RealmsServer)null);
         if (this.children.contains(this.realmSelectionList)) {
            RealmsMainScreen.RealmSelectionList var6 = this.realmSelectionList;
            if (!this.children.remove(var6)) {
               LOGGER.error("Unable to remove widget: {}", var6);
            }
         }

         NarrationHelper.now(POPUP_TEXT.getString());
      }

      if (this.hasFetchedServers) {
         this.showingPopup = true;
      }

      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.7F);
      RenderSystem.enableBlend();
      this.minecraft.getTextureManager().bind(DARKEN_LOCATION);
      boolean var8 = false;
      boolean var7 = true;
      GuiComponent.blit(var1, 0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
      RenderSystem.disableBlend();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(POPUP_LOCATION);
      GuiComponent.blit(var1, var4, var5, 0.0F, 0.0F, 310, 166, 310, 166);
      if (!teaserImages.isEmpty()) {
         this.minecraft.getTextureManager().bind((ResourceLocation)teaserImages.get(this.carouselIndex));
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GuiComponent.blit(var1, var4 + 7, var5 + 7, 0.0F, 0.0F, 195, 152, 195, 152);
         if (this.carouselTick % 95 < 5) {
            if (!this.hasSwitchedCarouselImage) {
               this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
               this.hasSwitchedCarouselImage = true;
            }
         } else {
            this.hasSwitchedCarouselImage = false;
         }
      }

      this.formattedPopup.renderLeftAlignedNoShadow(var1, this.width / 2 + 52, var5 + 7, 10, 5000268);
   }

   private int popupX0() {
      return (this.width - 310) / 2;
   }

   private int popupY0() {
      return this.height / 2 - 80;
   }

   private void drawInvitationPendingIcon(PoseStack var1, int var2, int var3, int var4, int var5, boolean var6, boolean var7) {
      int var8 = this.numberOfPendingInvites;
      boolean var9 = this.inPendingInvitationArea((double)var2, (double)var3);
      boolean var10 = var7 && var6;
      if (var10) {
         float var11 = 0.25F + (1.0F + Mth.sin((float)this.animTick * 0.5F)) * 0.25F;
         int var12 = -16777216 | (int)(var11 * 64.0F) << 16 | (int)(var11 * 64.0F) << 8 | (int)(var11 * 64.0F) << 0;
         this.fillGradient(var1, var4 - 2, var5 - 2, var4 + 18, var5 + 18, var12, var12);
         var12 = -16777216 | (int)(var11 * 255.0F) << 16 | (int)(var11 * 255.0F) << 8 | (int)(var11 * 255.0F) << 0;
         this.fillGradient(var1, var4 - 2, var5 - 2, var4 + 18, var5 - 1, var12, var12);
         this.fillGradient(var1, var4 - 2, var5 - 2, var4 - 1, var5 + 18, var12, var12);
         this.fillGradient(var1, var4 + 17, var5 - 2, var4 + 18, var5 + 18, var12, var12);
         this.fillGradient(var1, var4 - 2, var5 + 17, var4 + 18, var5 + 18, var12, var12);
      }

      this.minecraft.getTextureManager().bind(INVITE_ICON_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      boolean var19 = var7 && var6;
      float var20 = var19 ? 16.0F : 0.0F;
      GuiComponent.blit(var1, var4, var5 - 6, var20, 0.0F, 15, 25, 31, 25);
      boolean var13 = var7 && var8 != 0;
      int var14;
      if (var13) {
         var14 = (Math.min(var8, 6) - 1) * 8;
         int var15 = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
         this.minecraft.getTextureManager().bind(INVITATION_ICONS_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         float var16 = var9 ? 8.0F : 0.0F;
         GuiComponent.blit(var1, var4 + 4, var5 + 4 + var15, (float)var14, var16, 8, 8, 48, 16);
      }

      var14 = var2 + 12;
      boolean var21 = var7 && var9;
      if (var21) {
         Component var17 = var8 == 0 ? NO_PENDING_INVITES_TEXT : PENDING_INVITES_TEXT;
         int var18 = this.font.width((FormattedText)var17);
         this.fillGradient(var1, var14 - 3, var3 - 3, var14 + var18 + 3, var3 + 8 + 3, -1073741824, -1073741824);
         this.font.drawShadow(var1, (Component)var17, (float)var14, (float)var3, -1);
      }

   }

   private boolean inPendingInvitationArea(double var1, double var3) {
      int var5 = this.width / 2 + 50;
      int var6 = this.width / 2 + 66;
      int var7 = 11;
      int var8 = 23;
      if (this.numberOfPendingInvites != 0) {
         var5 -= 3;
         var6 += 3;
         var7 -= 5;
         var8 += 5;
      }

      return (double)var5 <= var1 && var1 <= (double)var6 && (double)var7 <= var3 && var3 <= (double)var8;
   }

   public void play(RealmsServer var1, Screen var2) {
      if (var1 != null) {
         try {
            if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.connectLock.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException var4) {
            return;
         }

         this.dontSetConnectedToRealms = true;
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(var2, new GetServerDetailsTask(this, var2, var1, this.connectLock)));
      }

   }

   private boolean isSelfOwnedServer(RealmsServer var1) {
      return var1.ownerUUID != null && var1.ownerUUID.equals(this.minecraft.getUser().getUuid());
   }

   private boolean isSelfOwnedNonExpiredServer(RealmsServer var1) {
      return this.isSelfOwnedServer(var1) && !var1.expired;
   }

   private void drawExpired(PoseStack var1, int var2, int var3, int var4, int var5) {
      this.minecraft.getTextureManager().bind(EXPIRED_ICON_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 10, 28);
      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         this.setTooltip(SERVER_EXPIRED_TOOLTIP);
      }

   }

   private void drawExpiring(PoseStack var1, int var2, int var3, int var4, int var5, int var6) {
      this.minecraft.getTextureManager().bind(EXPIRES_SOON_ICON_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.animTick % 20 < 10) {
         GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         GuiComponent.blit(var1, var2, var3, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         if (var6 <= 0) {
            this.setTooltip(SERVER_EXPIRES_SOON_TOOLTIP);
         } else if (var6 == 1) {
            this.setTooltip(SERVER_EXPIRES_IN_DAY_TOOLTIP);
         } else {
            this.setTooltip(new TranslatableComponent("mco.selectServer.expires.days", new Object[]{var6}));
         }
      }

   }

   private void drawOpen(PoseStack var1, int var2, int var3, int var4, int var5) {
      this.minecraft.getTextureManager().bind(ON_ICON_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 10, 28);
      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         this.setTooltip(SERVER_OPEN_TOOLTIP);
      }

   }

   private void drawClose(PoseStack var1, int var2, int var3, int var4, int var5) {
      this.minecraft.getTextureManager().bind(OFF_ICON_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      GuiComponent.blit(var1, var2, var3, 0.0F, 0.0F, 10, 28, 10, 28);
      if (var4 >= var2 && var4 <= var2 + 9 && var5 >= var3 && var5 <= var3 + 27 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         this.setTooltip(SERVER_CLOSED_TOOLTIP);
      }

   }

   private void drawLeave(PoseStack var1, int var2, int var3, int var4, int var5) {
      boolean var6 = false;
      if (var4 >= var2 && var4 <= var2 + 28 && var5 >= var3 && var5 <= var3 + 28 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         var6 = true;
      }

      this.minecraft.getTextureManager().bind(LEAVE_ICON_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float var7 = var6 ? 28.0F : 0.0F;
      GuiComponent.blit(var1, var2, var3, var7, 0.0F, 28, 28, 56, 28);
      if (var6) {
         this.setTooltip(LEAVE_SERVER_TOOLTIP);
         this.hoveredElement = RealmsMainScreen.HoveredElement.LEAVE;
      }

   }

   private void drawConfigure(PoseStack var1, int var2, int var3, int var4, int var5) {
      boolean var6 = false;
      if (var4 >= var2 && var4 <= var2 + 28 && var5 >= var3 && var5 <= var3 + 28 && var5 < this.height - 40 && var5 > 32 && !this.shouldShowPopup()) {
         var6 = true;
      }

      this.minecraft.getTextureManager().bind(CONFIGURE_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float var7 = var6 ? 28.0F : 0.0F;
      GuiComponent.blit(var1, var2, var3, var7, 0.0F, 28, 28, 56, 28);
      if (var6) {
         this.setTooltip(CONFIGURE_SERVER_TOOLTIP);
         this.hoveredElement = RealmsMainScreen.HoveredElement.CONFIGURE;
      }

   }

   protected void renderMousehoverTooltip(PoseStack var1, List<Component> var2, int var3, int var4) {
      if (!var2.isEmpty()) {
         int var5 = 0;
         int var6 = 0;
         Iterator var7 = var2.iterator();

         while(var7.hasNext()) {
            Component var8 = (Component)var7.next();
            int var9 = this.font.width((FormattedText)var8);
            if (var9 > var6) {
               var6 = var9;
            }
         }

         int var12 = var3 - var6 - 5;
         int var13 = var4;
         if (var12 < 0) {
            var12 = var3 + 12;
         }

         for(Iterator var14 = var2.iterator(); var14.hasNext(); var5 += 10) {
            Component var10 = (Component)var14.next();
            int var11 = var13 - (var5 == 0 ? 3 : 0) + var5;
            this.fillGradient(var1, var12 - 3, var11, var12 + var6 + 3, var13 + 8 + 3 + var5, -1073741824, -1073741824);
            this.font.drawShadow(var1, var10, (float)var12, (float)(var13 + var5), 16777215);
         }

      }
   }

   private void renderMoreInfo(PoseStack var1, int var2, int var3, int var4, int var5, boolean var6) {
      boolean var7 = false;
      if (var2 >= var4 && var2 <= var4 + 20 && var3 >= var5 && var3 <= var5 + 20) {
         var7 = true;
      }

      this.minecraft.getTextureManager().bind(QUESTIONMARK_LOCATION);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float var8 = var6 ? 20.0F : 0.0F;
      GuiComponent.blit(var1, var4, var5, var8, 0.0F, 20, 20, 40, 20);
      if (var7) {
         this.setTooltip(SERVER_INFO_TOOLTIP);
      }

   }

   private void renderNews(PoseStack var1, int var2, int var3, boolean var4, int var5, int var6, boolean var7, boolean var8) {
      boolean var9 = false;
      if (var2 >= var5 && var2 <= var5 + 20 && var3 >= var6 && var3 <= var6 + 20) {
         var9 = true;
      }

      this.minecraft.getTextureManager().bind(NEWS_LOCATION);
      if (var8) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
      }

      boolean var10 = var8 && var7;
      float var11 = var10 ? 20.0F : 0.0F;
      GuiComponent.blit(var1, var5, var6, var11, 0.0F, 20, 20, 40, 20);
      if (var9 && var8) {
         this.setTooltip(NEWS_TOOLTIP);
      }

      if (var4 && var8) {
         int var12 = var9 ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
         this.minecraft.getTextureManager().bind(INVITATION_ICONS_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         GuiComponent.blit(var1, var5 + 10, var6 + 2 + var12, 40.0F, 0.0F, 8, 8, 48, 16);
      }

   }

   private void renderLocal(PoseStack var1) {
      String var2 = "LOCAL!";
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.pushMatrix();
      RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
      RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      RenderSystem.scalef(1.5F, 1.5F, 1.5F);
      this.font.draw(var1, "LOCAL!", 0.0F, 0.0F, 8388479);
      RenderSystem.popMatrix();
   }

   private void renderStage(PoseStack var1) {
      String var2 = "STAGE!";
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.pushMatrix();
      RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
      RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      RenderSystem.scalef(1.5F, 1.5F, 1.5F);
      this.font.draw(var1, (String)"STAGE!", 0.0F, 0.0F, -256);
      RenderSystem.popMatrix();
   }

   public RealmsMainScreen newScreen() {
      RealmsMainScreen var1 = new RealmsMainScreen(this.lastScreen);
      var1.init(this.minecraft, this.width, this.height);
      return var1;
   }

   public static void updateTeaserImages(ResourceManager var0) {
      Collection var1 = var0.listResources("textures/gui/images", (var0x) -> {
         return var0x.endsWith(".png");
      });
      teaserImages = (List)var1.stream().filter((var0x) -> {
         return var0x.getNamespace().equals("realms");
      }).collect(ImmutableList.toImmutableList());
   }

   private void setTooltip(Component... var1) {
      this.toolTip = Arrays.asList(var1);
   }

   private void pendingButtonPress(Button var1) {
      this.minecraft.setScreen(new RealmsPendingInvitesScreen(this.lastScreen));
   }

   class CloseButton extends Button {
      public CloseButton() {
         super(RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, 12, 12, new TranslatableComponent("mco.selectServer.close"), (var1x) -> {
            RealmsMainScreen.this.onClosePopup();
         });
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.CROSS_ICON_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         float var5 = this.isHovered() ? 12.0F : 0.0F;
         blit(var1, this.x, this.y, 0.0F, var5, 12, 12, 12, 24);
         if (this.isMouseOver((double)var2, (double)var3)) {
            RealmsMainScreen.this.setTooltip(this.getMessage());
         }

      }
   }

   class ShowPopupButton extends Button {
      public ShowPopupButton() {
         super(RealmsMainScreen.this.width - 37, 6, 20, 20, new TranslatableComponent("mco.selectServer.info"), (var1x) -> {
            RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser;
         });
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         RealmsMainScreen.this.renderMoreInfo(var1, var2, var3, this.x, this.y, this.isHovered());
      }
   }

   class NewsButton extends Button {
      public NewsButton() {
         super(RealmsMainScreen.this.width - 62, 6, 20, 20, new TranslatableComponent("mco.news"), (var1x) -> {
            if (RealmsMainScreen.this.newsLink != null) {
               Util.getPlatform().openUri(RealmsMainScreen.this.newsLink);
               if (RealmsMainScreen.this.hasUnreadNews) {
                  RealmsPersistence.RealmsPersistenceData var2 = RealmsPersistence.readFile();
                  var2.hasUnreadNews = false;
                  RealmsMainScreen.this.hasUnreadNews = false;
                  RealmsPersistence.writeFile(var2);
               }

            }
         });
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         RealmsMainScreen.this.renderNews(var1, var2, var3, RealmsMainScreen.this.hasUnreadNews, this.x, this.y, this.isHovered(), this.active);
      }
   }

   class PendingInvitesButton extends Button implements TickableWidget {
      public PendingInvitesButton() {
         super(RealmsMainScreen.this.width / 2 + 47, 6, 22, 22, TextComponent.EMPTY, (var1x) -> {
            RealmsMainScreen.this.pendingButtonPress(var1x);
         });
      }

      public void tick() {
         this.setMessage(RealmsMainScreen.this.numberOfPendingInvites == 0 ? RealmsMainScreen.NO_PENDING_INVITES_TEXT : RealmsMainScreen.PENDING_INVITES_TEXT);
      }

      public void renderButton(PoseStack var1, int var2, int var3, float var4) {
         RealmsMainScreen.this.drawInvitationPendingIcon(var1, var2, var3, this.x, this.y, this.isHovered(), this.active);
      }
   }

   class ServerEntry extends RealmsMainScreen.Entry {
      private final RealmsServer serverData;

      public ServerEntry(RealmsServer var2) {
         super(null);
         this.serverData = var2;
      }

      public void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10) {
         this.renderMcoServerItem(this.serverData, var1, var4, var3, var7, var8);
      }

      public boolean mouseClicked(double var1, double var3, int var5) {
         if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.selectedServerId = -1L;
            RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
         } else {
            RealmsMainScreen.this.selectedServerId = this.serverData.id;
         }

         return true;
      }

      private void renderMcoServerItem(RealmsServer var1, PoseStack var2, int var3, int var4, int var5, int var6) {
         this.renderLegacy(var1, var2, var3 + 36, var4, var5, var6);
      }

      private void renderLegacy(RealmsServer var1, PoseStack var2, int var3, int var4, int var5, int var6) {
         if (var1.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.WORLDICON_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableAlphaTest();
            GuiComponent.blit(var2, var3 + 10, var4 + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            float var19 = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
            int var20 = -16777216 | (int)(127.0F * var19) << 16 | (int)(255.0F * var19) << 8 | (int)(127.0F * var19);
            GuiComponent.drawCenteredString(var2, RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, var3 + 10 + 40 + 75, var4 + 12, var20);
         } else {
            boolean var7 = true;
            boolean var8 = true;
            if (var1.expired) {
               RealmsMainScreen.this.drawExpired(var2, var3 + 225 - 14, var4 + 2, var5, var6);
            } else if (var1.state == RealmsServer.State.CLOSED) {
               RealmsMainScreen.this.drawClose(var2, var3 + 225 - 14, var4 + 2, var5, var6);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(var1) && var1.daysLeft < 7) {
               RealmsMainScreen.this.drawExpiring(var2, var3 + 225 - 14, var4 + 2, var5, var6, var1.daysLeft);
            } else if (var1.state == RealmsServer.State.OPEN) {
               RealmsMainScreen.this.drawOpen(var2, var3 + 225 - 14, var4 + 2, var5, var6);
            }

            if (!RealmsMainScreen.this.isSelfOwnedServer(var1) && !RealmsMainScreen.overrideConfigure) {
               RealmsMainScreen.this.drawLeave(var2, var3 + 225, var4 + 2, var5, var6);
            } else {
               RealmsMainScreen.this.drawConfigure(var2, var3 + 225, var4 + 2, var5, var6);
            }

            if (!"0".equals(var1.serverPing.nrOfPlayers)) {
               String var9 = ChatFormatting.GRAY + "" + var1.serverPing.nrOfPlayers;
               RealmsMainScreen.this.font.draw(var2, var9, (float)(var3 + 207 - RealmsMainScreen.this.font.width(var9)), (float)(var4 + 3), 8421504);
               if (var5 >= var3 + 207 - RealmsMainScreen.this.font.width(var9) && var5 <= var3 + 207 && var6 >= var4 + 1 && var6 <= var4 + 10 && var6 < RealmsMainScreen.this.height - 40 && var6 > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  RealmsMainScreen.this.setTooltip(new TextComponent(var1.serverPing.playerList));
               }
            }

            if (RealmsMainScreen.this.isSelfOwnedServer(var1) && var1.expired) {
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.enableBlend();
               RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.BUTTON_LOCATION);
               RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
               Component var22;
               Component var23;
               if (var1.expiredTrial) {
                  var22 = RealmsMainScreen.TRIAL_EXPIRED_TEXT;
                  var23 = RealmsMainScreen.SUBSCRIPTION_CREATE_TEXT;
               } else {
                  var22 = RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
                  var23 = RealmsMainScreen.SUBSCRIPTION_RENEW_TEXT;
               }

               int var11 = RealmsMainScreen.this.font.width((FormattedText)var23) + 17;
               boolean var12 = true;
               int var13 = var3 + RealmsMainScreen.this.font.width((FormattedText)var22) + 8;
               int var14 = var4 + 13;
               boolean var15 = false;
               if (var5 >= var13 && var5 < var13 + var11 && var6 > var14 && var6 <= var14 + 16 & var6 < RealmsMainScreen.this.height - 40 && var6 > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  var15 = true;
                  RealmsMainScreen.this.hoveredElement = RealmsMainScreen.HoveredElement.EXPIRED;
               }

               int var16 = var15 ? 2 : 1;
               GuiComponent.blit(var2, var13, var14, 0.0F, (float)(46 + var16 * 20), var11 / 2, 8, 256, 256);
               GuiComponent.blit(var2, var13 + var11 / 2, var14, (float)(200 - var11 / 2), (float)(46 + var16 * 20), var11 / 2, 8, 256, 256);
               GuiComponent.blit(var2, var13, var14 + 8, 0.0F, (float)(46 + var16 * 20 + 12), var11 / 2, 8, 256, 256);
               GuiComponent.blit(var2, var13 + var11 / 2, var14 + 8, (float)(200 - var11 / 2), (float)(46 + var16 * 20 + 12), var11 / 2, 8, 256, 256);
               RenderSystem.disableBlend();
               int var17 = var4 + 11 + 5;
               int var18 = var15 ? 16777120 : 16777215;
               RealmsMainScreen.this.font.draw(var2, var22, (float)(var3 + 2), (float)(var17 + 1), 15553363);
               GuiComponent.drawCenteredString(var2, RealmsMainScreen.this.font, var23, var13 + var11 / 2, var17 + 1, var18);
            } else {
               if (var1.worldType == RealmsServer.WorldType.MINIGAME) {
                  int var21 = 13413468;
                  int var10 = RealmsMainScreen.this.font.width((FormattedText)RealmsMainScreen.SELECT_MINIGAME_PREFIX);
                  RealmsMainScreen.this.font.draw(var2, RealmsMainScreen.SELECT_MINIGAME_PREFIX, (float)(var3 + 2), (float)(var4 + 12), 13413468);
                  RealmsMainScreen.this.font.draw(var2, var1.getMinigameName(), (float)(var3 + 2 + var10), (float)(var4 + 12), 7105644);
               } else {
                  RealmsMainScreen.this.font.draw(var2, var1.getDescription(), (float)(var3 + 2), (float)(var4 + 12), 7105644);
               }

               if (!RealmsMainScreen.this.isSelfOwnedServer(var1)) {
                  RealmsMainScreen.this.font.draw(var2, var1.owner, (float)(var3 + 2), (float)(var4 + 12 + 11), 5000268);
               }
            }

            RealmsMainScreen.this.font.draw(var2, var1.getName(), (float)(var3 + 2), (float)(var4 + 1), 16777215);
            RealmsTextureManager.withBoundFace(var1.ownerUUID, () -> {
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               GuiComponent.blit(var2, var3 - 36, var4, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
               GuiComponent.blit(var2, var3 - 36, var4, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);
            });
         }
      }
   }

   class TrialEntry extends RealmsMainScreen.Entry {
      private TrialEntry() {
         super(null);
      }

      public void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10) {
         this.renderTrialItem(var1, var2, var4, var3, var7, var8);
      }

      public boolean mouseClicked(double var1, double var3, int var5) {
         RealmsMainScreen.this.popupOpenedByUser = true;
         return true;
      }

      private void renderTrialItem(PoseStack var1, int var2, int var3, int var4, int var5, int var6) {
         int var7 = var4 + 8;
         int var8 = 0;
         boolean var9 = false;
         if (var3 <= var5 && var5 <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && var4 <= var6 && var6 <= var4 + 32) {
            var9 = true;
         }

         int var10 = 8388479;
         if (var9 && !RealmsMainScreen.this.shouldShowPopup()) {
            var10 = 6077788;
         }

         for(Iterator var11 = RealmsMainScreen.TRIAL_MESSAGE_LINES.iterator(); var11.hasNext(); var8 += 10) {
            Component var12 = (Component)var11.next();
            GuiComponent.drawCenteredString(var1, RealmsMainScreen.this.font, var12, RealmsMainScreen.this.width / 2, var7 + var8, var10);
         }

      }

      // $FF: synthetic method
      TrialEntry(Object var2) {
         this();
      }
   }

   abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
      private Entry() {
         super();
      }

      // $FF: synthetic method
      Entry(Object var2) {
         this();
      }
   }

   class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
      private boolean showingMessage;

      public RealmSelectionList() {
         super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 32, RealmsMainScreen.this.height - 40, 36);
      }

      public void clear() {
         super.clear();
         this.showingMessage = false;
      }

      public int addMessageEntry(RealmsMainScreen.Entry var1) {
         this.showingMessage = true;
         return this.addEntry(var1);
      }

      public boolean isFocused() {
         return RealmsMainScreen.this.getFocused() == this;
      }

      public boolean keyPressed(int var1, int var2, int var3) {
         if (var1 != 257 && var1 != 32 && var1 != 335) {
            return super.keyPressed(var1, var2, var3);
         } else {
            RealmsMainScreen.Entry var4 = (RealmsMainScreen.Entry)this.getSelected();
            return var4 == null ? super.keyPressed(var1, var2, var3) : var4.mouseClicked(0.0D, 0.0D, 0);
         }
      }

      public boolean mouseClicked(double var1, double var3, int var5) {
         if (var5 == 0 && var1 < (double)this.getScrollbarPosition() && var3 >= (double)this.y0 && var3 <= (double)this.y1) {
            int var6 = RealmsMainScreen.this.realmSelectionList.getRowLeft();
            int var7 = this.getScrollbarPosition();
            int var8 = (int)Math.floor(var3 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
            int var9 = var8 / this.itemHeight;
            if (var1 >= (double)var6 && var1 <= (double)var7 && var9 >= 0 && var8 >= 0 && var9 < this.getItemCount()) {
               this.itemClicked(var8, var9, var1, var3, this.width);
               RealmsMainScreen.this.clicks = RealmsMainScreen.this.clicks + 7;
               this.selectItem(var9);
            }

            return true;
         } else {
            return super.mouseClicked(var1, var3, var5);
         }
      }

      public void selectItem(int var1) {
         this.setSelectedItem(var1);
         if (var1 != -1) {
            RealmsServer var2;
            if (this.showingMessage) {
               if (var1 == 0) {
                  var2 = null;
               } else {
                  if (var1 - 1 >= RealmsMainScreen.this.realmsServers.size()) {
                     RealmsMainScreen.this.selectedServerId = -1L;
                     return;
                  }

                  var2 = (RealmsServer)RealmsMainScreen.this.realmsServers.get(var1 - 1);
               }
            } else {
               if (var1 >= RealmsMainScreen.this.realmsServers.size()) {
                  RealmsMainScreen.this.selectedServerId = -1L;
                  return;
               }

               var2 = (RealmsServer)RealmsMainScreen.this.realmsServers.get(var1);
            }

            RealmsMainScreen.this.updateButtonStates(var2);
            if (var2 == null) {
               RealmsMainScreen.this.selectedServerId = -1L;
            } else if (var2.state == RealmsServer.State.UNINITIALIZED) {
               RealmsMainScreen.this.selectedServerId = -1L;
            } else {
               RealmsMainScreen.this.selectedServerId = var2.id;
               if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.playButton.active) {
                  RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId), RealmsMainScreen.this);
               }

            }
         }
      }

      public void setSelected(@Nullable RealmsMainScreen.Entry var1) {
         super.setSelected(var1);
         int var2 = this.children().indexOf(var1);
         if (this.showingMessage && var2 == 0) {
            NarrationHelper.now(I18n.get("mco.trial.message.line1"), I18n.get("mco.trial.message.line2"));
         } else if (!this.showingMessage || var2 > 0) {
            RealmsServer var3 = (RealmsServer)RealmsMainScreen.this.realmsServers.get(var2 - (this.showingMessage ? 1 : 0));
            RealmsMainScreen.this.selectedServerId = var3.id;
            RealmsMainScreen.this.updateButtonStates(var3);
            if (var3.state == RealmsServer.State.UNINITIALIZED) {
               NarrationHelper.now(I18n.get("mco.selectServer.uninitialized") + I18n.get("mco.gui.button"));
            } else {
               NarrationHelper.now(I18n.get("narrator.select", var3.name));
            }
         }

      }

      public void itemClicked(int var1, int var2, double var3, double var5, int var7) {
         if (this.showingMessage) {
            if (var2 == 0) {
               RealmsMainScreen.this.popupOpenedByUser = true;
               return;
            }

            --var2;
         }

         if (var2 < RealmsMainScreen.this.realmsServers.size()) {
            RealmsServer var8 = (RealmsServer)RealmsMainScreen.this.realmsServers.get(var2);
            if (var8 != null) {
               if (var8.state == RealmsServer.State.UNINITIALIZED) {
                  RealmsMainScreen.this.selectedServerId = -1L;
                  Minecraft.getInstance().setScreen(new RealmsCreateRealmScreen(var8, RealmsMainScreen.this));
               } else {
                  RealmsMainScreen.this.selectedServerId = var8.id;
               }

               if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.CONFIGURE) {
                  RealmsMainScreen.this.selectedServerId = var8.id;
                  RealmsMainScreen.this.configureClicked(var8);
               } else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.LEAVE) {
                  RealmsMainScreen.this.selectedServerId = var8.id;
                  RealmsMainScreen.this.leaveClicked(var8);
               } else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.EXPIRED) {
                  RealmsMainScreen.this.onRenew();
               }

            }
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getRowWidth() {
         return 300;
      }

      // $FF: synthetic method
      public void setSelected(@Nullable AbstractSelectionList.Entry var1) {
         this.setSelected((RealmsMainScreen.Entry)var1);
      }
   }

   static enum HoveredElement {
      NONE,
      EXPIRED,
      LEAVE,
      CONFIGURE;

      private HoveredElement() {
      }
   }
}

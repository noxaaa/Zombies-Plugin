package io.lama06.zombies;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.lama06.zombies.event.player.PlayerCancelCommandEvent;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import io.lama06.zombies.perk.GlobalPerk;
import io.lama06.zombies.perk.PerkMachine;
import io.lama06.zombies.system.perk.global.PerkItem;
import io.lama06.zombies.util.PositionUtil;
import io.lama06.zombies.weapon.WeaponType;
import io.lama06.zombies.zombie.Zombie;
import io.lama06.zombies.zombie.ZombieType;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class ZombiesCommandExecutor implements TabExecutor {
    private static final String TEMPLATES_DIR = "templates";
    private static final List<String> BUILTIN_TEMPLATES = List.of("dead_end");

    @Override
    public boolean onCommand(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args
    ) {
        if (args.length == 0) {
            root(sender);
            return true;
        }

        final String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "config" -> config(sender);
            case "saveConfig" -> saveConfig(sender);
            case "checkConfig" -> checkConfig(sender);
            case "loadTemplate" -> loadTemplate(sender, remainingArgs);
            case "start" -> start(sender);
            case "stop" -> stop(sender);
            case "giveGold" -> giveGold(sender, remainingArgs);
            case "giveWeapon" -> giveWeapon(sender, remainingArgs);
            case "spawnZombie" -> spawnZombie(sender, remainingArgs);
            case "cancel" -> cancel(sender);
            case "placeSigns" -> placeSigns(sender, remainingArgs);
            case "dumpWorldConfig" -> dumpWorldConfig(sender);
            case "nextround" -> nextRound(sender);
            case "summonBuff" -> summonBuff(sender, remainingArgs);
            default -> sender.sendMessage(Component.text("unknown command").color(NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args
    ) {
        if (args.length == 1) {
            return List.of(
                    "config",
                    "saveConfig",
                    "checkConfig",
                    "loadTemplate",
                    "start",
                    "stop",
                    "giveGold",
                    "giveWeapon",
                    "spawnZombie",
                    "summonBuff",
                    "nextround"
            );
        }
        if (args.length == 2 && args[0].equals("loadTemplate")) {
            return getAvailableTemplates();
        }
        return List.of();
    }

    private List<String> getAvailableTemplates() {
        final List<String> templates = new ArrayList<>(BUILTIN_TEMPLATES);
        // 扫描配置目录中的模板
        final File templatesDir = new File(ZombiesPlugin.INSTANCE.getDataFolder(), TEMPLATES_DIR);
        if (templatesDir.exists() && templatesDir.isDirectory()) {
            final File[] files = templatesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (final File file : files) {
                    final String name = file.getName().replace(".json", "");
                    if (!templates.contains(name)) {
                        templates.add(name);
                    }
                }
            }
        }
        return templates;
    }

    private Reader getTemplateReader(final String templateName) {
        // 优先从配置目录加载
        final File templatesDir = new File(ZombiesPlugin.INSTANCE.getDataFolder(), TEMPLATES_DIR);
        final File templateFile = new File(templatesDir, templateName + ".json");
        if (templateFile.exists()) {
            try {
                return new FileReader(templateFile);
            } catch (final FileNotFoundException e) {
                // 继续尝试从内置资源加载
            }
        }
        // 从内置资源加载
        final InputStream resource = ZombiesPlugin.INSTANCE.getResource(TEMPLATES_DIR + "/" + templateName + ".json");
        if (resource != null) {
            return new InputStreamReader(resource);
        }
        return null;
    }

    private void root(final CommandSender sender) {
        final Component equalSigns = Component.text("=".repeat(10)).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        final Component obfuscatedChar = Component.text("_").decorate(TextDecoration.OBFUSCATED);
        final TextComponent.Builder builder = Component.text();

        builder.append(equalSigns).append(obfuscatedChar).appendSpace();
        builder.append(Component.text("Zombies-Plugin").color(NamedTextColor.LIGHT_PURPLE));
        builder.appendSpace().append(Component.text("(Version %s)".formatted(ZombiesPlugin.INSTANCE.getPluginMeta().getVersion()))
                                             .color(NamedTextColor.GREEN));
        builder.appendSpace().append(obfuscatedChar).append(equalSigns);

        builder.appendNewline();
        builder.append(Component.text("Creator: "));
        builder.append(Component.text("Lama06").color(NamedTextColor.GOLD));

        builder.appendNewline();
        builder.append(Component.text("Website: "));
        builder.append(Component.text("github.com/Lama06/Zombies-Plugin")
                               .clickEvent(ClickEvent.openUrl("https://github.com/Lama06/Zombies-Plugin/")));

        sender.sendMessage(builder);
    }

    private void config(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (world.isGameRunning()) {
            final String warning = "It isn't supported to edit a running game's config. " +
                    "Only continue if you know what you're doing.";
            sender.sendMessage(Component.text(warning).color(NamedTextColor.RED));
        }
        WorldConfig config = world.getConfig();
        if (config == null) {
            final ZombiesConfig globalConfig = ZombiesPlugin.INSTANCE.getGlobalConfig();
            config = new WorldConfig();
            globalConfig.worlds.put(world.getBukkit().getName(), config);
        }
        config.openMenu(player, () -> {});
    }

    private void saveConfig(final CommandSender sender) {
        try {
            ZombiesPlugin.INSTANCE.getConfigManager().saveConfig();
        } catch (final IOException e) {
            sender.sendMessage(Component.text("error: " + e.getMessage()));
            ZombiesPlugin.INSTANCE.getSLF4JLogger().error("failed to save config", e);
        }
        sender.sendMessage(Component.text("Saved").color(NamedTextColor.GREEN));
    }

    private void checkConfig(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final WorldConfig config = ZombiesPlugin.INSTANCE.getWorldConfig(new ZombiesWorld(player.getWorld()));
        if (config == null) {
            sender.sendMessage(Component.text("This world isn't configured"));
            return;
        }
        try {
            config.check();
        } catch (final InvalidConfigException e) {
            sender.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("No issues found").color(NamedTextColor.GREEN));
    }

    private void loadTemplate(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final List<String> availableTemplates = getAvailableTemplates();
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /zombies loadTemplate <template>").color(NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Available templates: " + String.join(", ", availableTemplates)).color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Put custom templates in: plugins/zombies/templates/").color(NamedTextColor.GRAY));
            return;
        }
        final String templateName = args[0];
        final Reader templateReader = getTemplateReader(templateName);
        if (templateReader == null) {
            sender.sendMessage(Component.text("Template not found: " + templateName).color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Available templates: " + String.join(", ", availableTemplates)).color(NamedTextColor.GRAY));
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        final ZombiesConfig globalConfig = ZombiesPlugin.INSTANCE.getGlobalConfig();
        final WorldConfig config;
        try {
            config = ConfigManager.createGson().fromJson(templateReader, WorldConfig.class);
        } catch (final JsonParseException e) {
            sender.sendMessage(Component.text("Template malformed: " + e.getMessage()).color(NamedTextColor.RED));
            return;
        } finally {
            try {
                templateReader.close();
            } catch (final IOException ignored) {}
        }
        globalConfig.worlds.put(world.getBukkit().getName(), config);
        sender.sendMessage(Component.text("Template '" + templateName + "' loaded. Start the game: Click me!").color(NamedTextColor.GREEN)
                                   .clickEvent(ClickEvent.suggestCommand("/zombies start")));
    }

    private void start(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld()) {
            final Component msg = Component.text("You muse first configure this world.").color(NamedTextColor.RED)
                    .appendNewline()
                    .append(Component.text("> Configure manually <")
                                    .clickEvent(ClickEvent.runCommand("/zombies config"))
                                    .color(NamedTextColor.BLUE))
                    .appendNewline()
                    .append(Component.text("> Load Dead End config (recommended) <")
                                    .clickEvent(ClickEvent.runCommand("/zombies loadTemplate dead_end"))
                                    .color(NamedTextColor.GREEN));
            sender.sendMessage(msg);
            return;
        }
        if (world.isGameRunning()) {
            sender.sendMessage(Component.text("The game is already running").color(NamedTextColor.RED));
            return;
        }
        try {
            world.getConfig().check();
        } catch (final InvalidConfigException e) {
            player.sendMessage(Component.text("The config is invalid: " + e.getMessage()).color(NamedTextColor.RED));
            return;
        }
        world.startGame();
    }

    private void stop(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld() || !world.isGameRunning()) {
            sender.sendMessage(Component.text("The game isn't running").color(NamedTextColor.RED));
            return;
        }
        world.endGame();
    }

    private void giveWeapon(final CommandSender sender, final String[] args) {
        if (args.length == 0) {
            return;
        }
        final WeaponType weaponType;
        try {
            weaponType = WeaponType.valueOf(args[0].toUpperCase());
        } catch (final IllegalArgumentException e) {
            return;
        }
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
        zombiesPlayer.giveWeapon(player.getInventory().getHeldItemSlot(), weaponType);
    }

    private void giveGold(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
        if (args.length == 0) {
            return;
        }
        final int goldAdd;
        try {
            goldAdd = Integer.parseInt(args[0]);
        } catch (final NumberFormatException e) {
            return;
        }
        final int goldPrevious = zombiesPlayer.get(ZombiesPlayer.GOLD);
        final int golfAfter = goldPrevious + goldAdd;
        zombiesPlayer.set(ZombiesPlayer.GOLD, golfAfter);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(zombiesPlayer, goldPrevious, golfAfter));
    }

    private void spawnZombie(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isGameRunning()) {
            return;
        }
        if (args.length == 0) {
            return;
        }
        final ZombieType zombieType;
        try {
            zombieType = ZombieType.valueOf(args[0].toUpperCase());
        } catch (final IllegalArgumentException e) {
            return;
        }
        world.spawnZombie(player.getLocation(), zombieType);
    }

    private void cancel(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new PlayerCancelCommandEvent(player));
    }

    private record SignPosition(Block block, BlockFace direction) { }

    @FunctionalInterface
    private interface SignPositionFetcher {
        Optional<SignPosition> getSignPosition(final World world, final BlockPosition position);
    }

    private Optional<SignPosition> getShopSignPosition(final World world, final BlockPosition position) {
        final Block signBlock = position.toLocation(world).getBlock();
        Block neighbour = null;
        int modX, modZ = 0;
        searchNeighbour:
        for (modX = -1; modX <= 1; modX++) {
            for (modZ = -1 ; modZ <= 1; modZ++) {
                if ((modX == 0) == (modZ == 0)) {
                    continue;
                }
                final Block neighbourCandidate = signBlock.getRelative(modX, 0, modZ);
                if (neighbourCandidate.getType().isEmpty()) {
                    continue;
                }
                neighbour = neighbourCandidate;
                break searchNeighbour;
            }
        }
        if (neighbour == null) {
            return Optional.empty();
        }
        final int finalModX = modX;
        final int finalModZ = modZ;
        final BlockFace directionToNeighbour = Arrays.stream(BlockFace.values())
                .filter(face -> face.getModX() == finalModX && face.getModZ() == finalModZ)
                .findAny().orElseThrow();
        final BlockFace signDirection = directionToNeighbour.getOppositeFace();
        return Optional.of(new SignPosition(signBlock, signDirection));
    }

    private Optional<SignPosition> getPerkSignPosition(final World world, final BlockPosition position) {
        final Block buttonBlock = position.toLocation(world).getBlock();
        if (!(buttonBlock.getBlockData() instanceof final Switch buttonData)) {
            return Optional.empty();
        }
        final BlockFace signDirection = buttonData.getFacing();
        final Block signBlock = buttonBlock.getRelative(BlockFace.UP);
        return Optional.of(new SignPosition(signBlock, signDirection));
    }

    private boolean placeSign(
            final SignPositionFetcher fetcher,
            final World world,
            final BlockPosition position,
            final List<? extends Component> lines
    ) {
        final Optional<SignPosition> signPosition = fetcher.getSignPosition(world, position);
        if (signPosition.isEmpty()) {
            return false;
        }
        final Block signBlock = signPosition.get().block();
        signBlock.setType(Material.OAK_WALL_SIGN);
        final WallSign signData = (WallSign) signBlock.getBlockData();
        signData.setFacing(signPosition.get().direction());
        signBlock.setBlockData(signData);
        final Sign signState = (Sign) signBlock.getState();
        final SignSide signFront = signState.getSide(Side.FRONT);
        signFront.setGlowingText(true);
        for (int i = 0; i < lines.size(); i++) {
            final Component line = lines.get(i);
            signFront.line(i, line);
        }
        signState.update();
        return true;
    }

    private void placeSigns(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("ok")) {
            final TextComponent.Builder builder = Component.text();
            builder.append(Component.text("Executing this command will place signs at every armor and weapon shop."));
            builder.appendNewline();
            builder.append(Component.text("Existing blocks will be removed. This cannot be reverted").color(NamedTextColor.RED));
            builder.appendNewline();
            builder.append(Component.text("> Click here to confirm <")
                                   .clickEvent(ClickEvent.runCommand("/zombies placeSigns ok"))
                                   .color(NamedTextColor.BLUE));
            sender.sendMessage(builder);
            return;
        }

        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld()) {
            return;
        }
        final WorldConfig config = world.getConfig();
        final List<BlockPosition> errors = new ArrayList<>();
        for (final ArmorShop armorShop : config.armorShops) {
            if (armorShop.position == null || armorShop.quality == null || armorShop.part == null) {
                continue;
            }
            final boolean ok = placeSign(this::getShopSignPosition, world.getBukkit(), armorShop.position, List.of(
                    armorShop.quality.getDisplayName().append(Component.text(" Armor")),
                    armorShop.part.getDisplayName(),
                    Component.text(armorShop.price + " Gold").color(NamedTextColor.GOLD)
            ));
            if (!ok) {
                errors.add(armorShop.position);
            }
        }
        for (final WeaponShop weaponShop : config.weaponShops) {
            if (weaponShop.position == null || weaponShop.weaponType == null) {
                continue;
            }
            final boolean ok = placeSign(this::getShopSignPosition, world.getBukkit(), weaponShop.position, List.of(
                    weaponShop.weaponType.getDisplayName(),
                    Component.text(weaponShop.purchasePrice + " Gold").color(NamedTextColor.GOLD),
                    Component.text("Refill: ").append(Component.text(weaponShop.refillPrice + " Gold").color(NamedTextColor.GOLD))
            ));
            if (!ok) {
                errors.add(weaponShop.position);
            }
        }
        for (final PerkMachine perkMachine : config.perkMachines) {
            if (perkMachine.position == null || perkMachine.perk == null) {
                continue;
            }
            final boolean ok = placeSign(this::getPerkSignPosition, world.getBukkit(), perkMachine.position, List.of(
                    perkMachine.perk.getDisplayName(),
                    Component.text(perkMachine.gold + " Gold").color(NamedTextColor.GOLD)
            ));
            if (!ok) {
                errors.add(perkMachine.position);
            }
        }
        for (final BlockPosition error : errors) {
            sender.sendMessage(Component.text("Failed to place sign at " + PositionUtil.format(error)).color(NamedTextColor.RED));
        }
        sender.sendMessage(Component.text("Done").color(NamedTextColor.GREEN));
    }

    private void dumpWorldConfig(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld()) {
            return;
        }
        final WorldConfig config = world.getConfig();
        final Gson gson = ConfigManager.createGson();
        final String json = gson.toJson(config);
        sender.sendMessage(Component.text("> Copy <").clickEvent(ClickEvent.copyToClipboard(json)).color(NamedTextColor.GREEN));
    }

    private void nextRound(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isGameRunning()) {
            sender.sendMessage(Component.text("游戏未运行").color(NamedTextColor.RED));
            return;
        }
        final WorldConfig config = world.getConfig();
        if (config == null || config.rounds.isEmpty()) {
            sender.sendMessage(Component.text("配置无效").color(NamedTextColor.RED));
            return;
        }
        final Integer currentRound = world.get(ZombiesWorld.ROUND);
        if (currentRound == null) {
            return;
        }
        // 杀死所有僵尸
        final List<Zombie> zombies = world.getZombies();
        for (final Zombie zombie : zombies) {
            zombie.getEntity().remove();
        }
        // 标记当前回合所有波次已触发
        final RoundConfig roundConfig = config.rounds.get(currentRound - 1);
        world.set(ZombiesWorld.TRIGGERED_WAVES, roundConfig.waves.size());
        sender.sendMessage(Component.text("已跳过当前回合").color(NamedTextColor.GREEN));
    }

    private static final int PERK_ITEM_TIME = 60 * 20;

    private void summonBuff(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("用法: /zombies summonBuff <BUFF名称>").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("可用: INSTANT_KILL, MAX_AMMO, DOUBLE_GOLD").color(NamedTextColor.GRAY));
            return;
        }
        final GlobalPerk perk;
        try {
            perk = GlobalPerk.valueOf(args[0].toUpperCase());
        } catch (final IllegalArgumentException e) {
            sender.sendMessage(Component.text("无效的Buff名称: " + args[0]).color(NamedTextColor.RED));
            sender.sendMessage(Component.text("可用: INSTANT_KILL, MAX_AMMO, DOUBLE_GOLD").color(NamedTextColor.GRAY));
            return;
        }

        // 记录玩家当前位置
        final org.bukkit.Location spawnLocation = player.getLocation().clone().add(0, 2, 0);
        final World world = player.getWorld();

        sender.sendMessage(Component.text("3秒后将在此位置生成 ").color(NamedTextColor.GREEN)
                .append(perk.getDisplayName()));

        // 3秒后生成buff
        Bukkit.getScheduler().runTaskLater(ZombiesPlugin.INSTANCE, () -> {
            final ItemDisplay display = world.spawn(spawnLocation, ItemDisplay.class);
            display.setCustomNameVisible(true);
            display.customName(perk.getDisplayName());
            display.setItemStack(new ItemStack(perk.getMaterial()));
            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(0.6f, 0.6f, 0.6f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));
            final PersistentDataContainer pdc = display.getPersistentDataContainer();
            pdc.set(PerkItem.getPerkNameKey(), PersistentDataType.STRING, perk.name());
            pdc.set(PerkItem.getRemainingTimeKey(), PersistentDataType.INTEGER, PERK_ITEM_TIME);
        }, 3 * 20L); // 3秒 = 60 ticks
    }
}

# BuildMode

Created with ❤️ by Axther

BuildMode is a Minecraft Spigot plugin for version 1.21.4 that allows players to temporarily enter Creative mode for building purposes while maintaining server economy balance.

## Features

- Temporary Creative mode sessions (60 minutes by default)
- Inventory preservation between survival and creative modes
- Item restrictions to prevent economy abuse
- Container and drop protections
- Boss bar timer display
- PlaceholderAPI integration
- No permissions needed for basic usage

## Commands

- `/buildmode start` - Start a build mode session
- `/buildmode end` - End your current build mode session
- `/buildmode reload` - Reload the plugin configuration (requires `buildmode.admin` permission)
- `/buildmode list` - List active build mode sessions (requires `buildmode.admin` permission)

Alias: `/bm`

## Permissions

- `buildmode.admin` - Allows bypassing time limits, viewing active sessions, and reloading the plugin

## Configuration

The plugin configuration is stored in `config.yml`:

```yaml
# BuildMode Configuration

# Duration of build mode session in minutes
build-duration-minutes: 60

# Cooldown between sessions in minutes
cooldown-minutes: 1

# Display options
bossbar: true
scoreboard: false

# Allow redstone components
allowed-redstone: true

# Item restriction mode (blacklist or whitelist)
restriction-mode: blacklist

# Blacklist of items not allowed in build mode
blacklist:
  # Ores & Raw Blocks
  - COAL_ORE
  - IRON_ORE
  # ... (and many more items)
```

## PlaceholderAPI Integration

The plugin provides the following placeholders when PlaceholderAPI is installed:

- `%buildmode_active%` - Returns "true" if the player is in build mode, "false" otherwise
- `%buildmode_timeleft%` - Returns the remaining time in the format "mm:ss"
- `%buildmode_timeleft_seconds%` - Returns the remaining time in seconds
- `%buildmode_cooldown%` - Returns the cooldown time in minutes

## How It Works

1. When a player starts a build mode session, their inventory is saved and they are given a wooden axe.
2. The player is switched to Creative mode and can build freely with allowed blocks.
3. Restricted items (valuable resources, combat gear, etc.) are blocked to prevent economy abuse.
4. When the session ends (timeout, command, or logout), the player's original inventory is restored.

## Item Restrictions

The plugin follows a philosophy of allowing building blocks and redstone components while restricting valuable items, combat gear, and utility items. By default, it uses a blacklist approach, but you can switch to a whitelist in the configuration.

## Edge Cases

- Player logs out mid-session: Timer keeps running offline; if expired, inventory is restored on next login.
- Player dies: Falls back to survival rules, still restores inventory.
- Server reload: Sessions & timers persist via scheduler re-registration.
- Two sessions back-to-back: 1-minute cooldown (configurable) to discourage spam toggling.

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/BuildMode/config.yml`

## Building from Source

1. Clone the repository
2. Build using Maven: `mvn clean package`
3. The JAR file will be in the `target` directory

## Author

Axther

## Dependencies

- Spigot/Paper 1.21.4
- PlaceholderAPI (optional)

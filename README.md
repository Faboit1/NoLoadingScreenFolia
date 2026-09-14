# NoLoadingScreenFolia

A Folia port of [LoadingScreenRemover](https://modrinth.com/plugin/loadingscreenremover) by Unnm3d.

When a player teleports from one world to another, the server sends a respawn
packet and the client puts up the **"Loading terrain"** screen. If both worlds
share an environment — overworld to overworld, say — the client's dimension
state is already correct, so that packet can simply be dropped and the teleport
looks like an ordinary move. Teleports that cross into a *different*
environment are left alone, because the client genuinely has to rebuild its
dimension state for those.

## Requirements

| | |
|---|---|
| Server | Folia **26.2** |
| Java | **25** |

PacketEvents is shaded in, so this is a single drop-in jar with nothing else to
install. It is relocated, so it will not conflict with a standalone PacketEvents
install or with another plugin that shades its own copy.

## Installing

Grab `NoLoadingScreenFolia-<version>.jar` from the
[latest release](../../releases/latest), or from the **NoLoadingScreen**
artifact on any [Build run](../../actions/workflows/build.yml). Drop it in
`plugins/` and restart.

## Configuration

`plugins/NoLoadingScreen/config.yml`:

```yaml
# Apply potion effects to cover the moment before the destination chunks render.
mask-chunk-load: true

# Blindness duration in ticks (20 ticks = 1 second). 0 disables it.
blind-ticks: 20

# Duration in ticks of the speed/slowness pair applied during the transfer.
speed-ticks: 10
```

Without the loading screen the client keeps rendering while the destination
chunks stream in, so it would otherwise show the old world's terrain, or empty
void, for a moment. The potion effects hide that. Set `mask-chunk-load: false`
to drop the packet without touching the player.

## Command

`/noloadingscreen reload` — reload the config. Aliases: `/nls`, `/lsr`,
`/loadingscreenremover`. Permission: `noloadingscreen.use` (op by default).

## Building

```sh
./gradlew build
```

The jar lands in `build/libs/`. Gradle will fetch a Java 25 toolchain itself if
the machine does not have one. GitHub Actions builds every push and attaches the
jar; pushing a `v*` tag publishes a release.

## What changed from the original

The upstream jar declares `folia-supported: true`, but several things kept it
from working on a current Folia:

- **PacketEvents was never declared as a dependency.** The plugin called
  `PacketEvents.getAPI()` in `onEnable` with no `depend` entry in `plugin.yml`,
  so unless PacketEvents happened to load first it failed immediately. It is now
  shaded and relocated, and initialised in `onLoad` where it belongs.
- **`PotionEffectType.SLOW` no longer exists.** It was renamed `SLOWNESS`, so
  every qualifying teleport threw.
- **The "changing worlds" flag was never cleared on success.** It was only
  cleared by a *later* teleport that did not qualify, so after one world change
  a player kept swallowing every respawn packet — including the one sent after
  they died. The flag is now a one-shot consumed by the respawn packet it was
  armed for, and it expires on its own if that packet never arrives.
- **Players were tracked by `Player` instance**, which pins a disconnected
  player's object graph and is unreliable across Folia's region threads. Now
  keyed by UUID and cleared on quit.
- **The packet listener resolved a Bukkit `Player` on the Netty thread.** It
  reads the UUID off the connection instead, so no entity is touched off-region.
- `api-version` was `1.18`; it is now `1.21`.
- The shaded config library and its runtime snakeyaml-engine download were
  replaced with Bukkit's built-in config for what amounts to two integers.
- The reload message sent the literal text `&bLoadingScreenRemover ...`, since
  legacy colour codes are not translated on the way out.
- bStats reporting under the original author's plugin ID was removed, and the
  shaded PacketEvents has its own update check and metrics turned off.
- The bundled UniversalScheduler was dropped — the scheduler it created was
  never used.

## Credit

Original plugin and approach by **Unnm3d**.

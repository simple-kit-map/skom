# skom (**s**imple **k**it **m**ap on minest**om**)

simple kit map is a [minestom](https://minestom.net/) mini-game/sandbox PvP server, come have a peek at the progress by joining `skm.ctt.cx` (1.7.10-1.21.11)

### mini-game and their rules

1. Create a new minigame: `/mi new skywars`
2. Add a map to play on `/mi set skywars islands2`
2. Add and configure your [gamerules](https://github.com/simple-kit-map/skom/tree/main/src/main/java/cx/ctt/skom/gamerules): `/mi set skywars SpawnWithKit frogger`
3. Start a game and include your friends' usernames: `/play skywars couleur marc jeb`

### setup:
1. [make a github account](<https://github.com/signup>) and fork [simple-kit-map/skom](<https://github.com/simple-kit-map/skom/fork>)
2. install [IntelliJ IDEA](<https://www.jetbrains.com/idea/download>)
3. `New` → `Project...` → `From Version Control...`
4. Log in with GitHub and select your skom fork
5. Initialize project by clicking that little button in [`build.gradle.kts`](./build.gradke.kts): <img width="75" height="35" alt="image" src="https://gist.github.com/user-attachments/assets/63b77112-ef9f-4093-a7a1-1a192e986a87" />
6. run redis-server with redis-json plugin (depends on your os & package manager)
7. run grade's `run` task and connect to `localhost:45565` in 1.21.11 (1.7 proxy guide soon)
8. Check out some plugins e.g.:
   * [Database Tools](https://plugins.jetbrains.com/plugin/10925)
   * [IdeaVim](https://plugins.jetbrains.com/plugin/164)
   
### serving
1. minestom backend server runs on loopback (127.0.0.1 / localhost), defaults to on port 45565
   * when running in a dev environment you can skip proxy setup (step 3) and just connect to `localhost:45565` on 1.21.11
2. redis also runs on loopback, port 6379
3. if protocol-translating through [ViaProxy](https://github.com/ViaVersion/ViaProxy) or [Velocity](https://github.com/PaperMC/Velocity/)+[VV](https://github.com/ViaVersion/ViaVersion/)+[VR](https://github.com/ViaVersion/ViaRewind)+[VB](https://github.com/ViaVersion/viabackwards/), runs on all interfaces (0.0.0.0) on port 25565 (minecraft default)
   * Velocity: use `SKM_AUTH=bungee`, and configure Velocity to use [bungeecord/legacy forwarding](https://docs.papermc.io/velocity/player-information-forwarding/)
   * ViaProxy:, the [`run` task](https://github.com/simple-kit-map/skom/blob/1d04c4aa49081728dd2567470ebd152048917f29/build.gradle.kts#L102-L116) downloads and hooks [ViaProxyAuthHook](https://github.com/ViaVersionAddons/ViaProxyAuthHook)


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
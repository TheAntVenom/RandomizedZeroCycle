<img width="865" height="195" alt="rzc-menu" src="https://github.com/user-attachments/assets/f15ea028-caab-485e-b633-9fe158ebf299" />

# RandomizedZeroCycle

Gives the user basic control over the first "Ender Dragon Flight Node" with 5 "game-mode" settings. Requires Minecraft 1.16.1 & Fabric Loader.

Tested with Fabric Loader 0.16.14, within an instance where many / of the most common RSG mods were enabled.

This mod was especially designed to work with both "MiniPracticeKit", and Mescht's Zero Practice Map. I recommend using those to handle your inventory / getting into the end.

---

This mod affects the first X/Z node that dragon chooses when it first spawns in, and it lets you modify the Y-axis of that and all future nodes for the entire fight.

Think "dragon is always at ground level / or always out of reach", if you feel like getting silly with the idea.

I made use out of this to make "low to the ground zero cycle" scenarios, and I imagine there are a lot of odd scenarios that could be cooked up.

---

Config file is stored in the instance `/config` folder. Use that if you want to edit the coordinates for "Expanded Zero Cycle" mode.

Use `/rzc` + tab completion to navigate. Type `?` on most entries to get a description of that item, and see what values settings are set to.

---

# Modes:

* **Vanilla** - Should behave like Vanilla 1.16.1
* **FullyRandom** - The first node will spawn anywhere within a customizable ring surrounding the end fountain.
* **ExpandedZeroCycle** - Think about how modern Zero Cycles work, but instead of only two towers being affected by the 7/8 and 1/8 pool, all 10 of them have fixed coordinates, for both 7/8 and 1/8 arrangements.
* **TwelveVanillaNodes** - I saw the opportunity to implement a mode that pulled from the Ender Dragon's normal in-flight nodes, and I implemented it.

---

# Settings:

* **`spawnMsg`** - Shows which X/Z node the dragon will first target. Off by default.
* **`deathMsg`** - Shows a message after the dragon goes down, with which game-mode you played, the first X/Y/Z coordinates of the chosen node, and what height the dragon was set to spawn at.
* **`nodeMarker`** - A color customizable vertical beacon of particles that show over the first X/Z node. Idea directly ripped from Mescht's Practice Map.
* **`yOffset`** - The Y-axis nodes are chosen with Vanilla 1.16.1's code, but this option will let you offset which coordinate is chosen by a fixed amount.
* **`spawnHeight`** - Change the height the dragon spawns at, from 0-255.

---

# Building from Source

Clone the repository and build using Gradle:

```bash
git clone [https://github.com/your-username/RandomizedZeroCycle.git](https://github.com/your-username/RandomizedZeroCycle.git)
cd RandomizedZeroCycle
./gradlew build

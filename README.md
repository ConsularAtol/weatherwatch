[![](https://jitpack.io/v/ConsularAtol/weatherwatch.svg)](https://jitpack.io/#ConsularAtol/weatherwatch)

# Weather Watch
Weather Watch is a mod that focuses on matching the in-game weather to the real life weather in your area. If it's stormy outside, it'll match that in game!

# Features
## Weather Sync
Uses WeatherAPI to find the weather data at the location specified by the server's IP, or just your IP if you're playing singleplayer. (Wonder what cool things you could find with a VPN?)
![The weather in my area is sunny, therefore the in game weather is clear](https://cdn.modrinth.com/data/cached_images/8e01ca31a449e1efb90c6ad7d33020d394f9fbdf.png)
![The weather in my friends area in rainy, therefore it's raining in her game](https://cdn.modrinth.com/data/cached_images/edaf924546eb7caddf402e81f9aaa35597d5841d_0.webp)
*First screenshot from my area, second screenshot from a friends area*
## Season Sync
Weather Watch supports [Serene Seasons](https://modrinth.com/mod/serene-seasons/versions), and will set the season depending on the month your system clock is set to. It works in a 3 month per season system, with December-February* being winter and so on.
![The season is late winter because it is February](https://cdn.modrinth.com/data/cached_images/59d4940aa3d0d8234cee92c1949fdff4f485fee1.png)
![The season is late spring because it is May](https://cdn.modrinth.com/data/cached_images/f326f53044d8d1a536d09efd909e3ebc5f0d6cd9.png)

The months corresponding to each season depends on the hemisphere you're located in*
*In multiplayer, it'll simply refer to the server clock*
## Time Sync (off by default)
Optionally, Weather Watch can also sync the day time to your system clock as well! This isn't recommended for traditional survival, as it does remove the ability to skip to day with a bed. However, this is great for peaceful or otherwise roleplay experiences!
![Minecraft equivalent to 4:25 PM](https://cdn.modrinth.com/data/cached_images/ad015bb105b5d7961aabe77f84b268877bf82609_0.webp)
![Replace this with a description](https://cdn.modrinth.com/data/cached_images/378bf8b8bacb8b7896bd6821adccb9de445801d4_0.webp)
*In multiplayer, it'll simply refer to the server clock*

*I did realize while writing this that time will literally just fast forward/rewind for American Daylight Savings, but that's really funny actually*

# Config
## `syncWeather` (default: true)
Toggles the weather syncing capabilities

## `syncSeasons` (default: true)
Toggles the season syncing capabilities, does nothing if [Serene Seasons](https://modrinth.com/mod/serene-seasons/versions) is not installed

## `syncTime` (default: false)
Toggles the time syncing capabilities

## `syncMoonPhase` (default: true)
Toggles syncing moon phase with the moon phase in your area

## `ip-override` (default: "server")
Overrides the IP fed into the location fetcher. When set to `server`, it will simply use the server's public IP. 

# FAQ
## "Doesn't this cause privacy concerns?"
I thought of this while making it and made sure that no info is public to anyone that wouldn't already know it. All features that rely on location tracking do so using the already public server IP, the only info connected clients recieve is if it should be raining, what season it should be, and the time of day.
## "Can I use this in singleplayer?"
As long as you're connected to the internet, yes. This mod requires an internet connection to access WeatherAPI and to track the server location. Time and season sync should work regardless though.
## "Why isn't this mod open source?"
As much as I would love to make this one open source, I cannot do so until I find a way to hide my WeatherAPI key from plain sight. Keeping it closed source is the easiest way I can think of to both hide my API key and not require users to provide their own for now. While the cost is small, excessive calls to the API does cost me some money, and that can be easily manipulated if someone wants to.

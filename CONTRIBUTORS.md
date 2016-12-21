**This list may not be fully accurate at the current time, but we listed the major/first contributions.**

## Developer
* @austinv11

## Co-developers
* @GrandPanda
* @chrislo27

## Contributors
* @iabarca - For too many things to list
* @grandmind - Added example bots, helped with documentation, added RoleBuilder, made QoL adjustments
* @phantamanta44 - Added some Java 8 goodness
* @lclc98 - Added audio support, initial sharding
* @sedmelluq - Realized how UTF-8 was ruining audio
* @TheFjonG - Created the awesome (now outdated) Discord4J ReadTheDocs page
* @Martacus - Created an example repo for Discord4J modules
* @davue - Added user fields to VoiceUser events
* @dec - Wrote the regex for invite parsing
* @langerhans - Helped with the AudioChannel object
* @Kaioru - Fixed the module-requires manifest key
* @theIgloo - Fixed voice state logic, added self mute and deaf support, made IVoiceChannel#join() check the user's 
 permissions, added voice channel bitrate support, and fixed various permission caching problems
* @Techtony96 - Made IVoiceChannel#mention() throw an UnsupportedOperationException
* @chrislo27 - Fixed message pin event logic and clarified cloudflare errors, MessageTokenizer
* @poncethecat - Added .addRole and .removeRole for the User object
* @oopsjpeg - Added ignore case for getXByName for many methods
* @ArsenArsen - Added CloudFlare requests to retry
* @ChrisVega - Originally fixed reconnects pre-websocket rewrite
* @robot-rover - Implemented embed support, D4J logo, module loader fixes
* @nija123098 - Added incomplete request count tracking
* @Masterzach32 - Added IGuild utilities
* @UnderMybrella - Added webhook support
* @BloodShura - AudioPlayer::queue(IAudioProvider) now calls the correct constructor for AudioInputStreamProvider instances; Track(AudioInputStream) constructor no longer declares IOException as being throwable

## Pre-Fork Contributors
* @nerd -Project creator and original maintainer
* @Klazen108 -Added UTF-8 support for messages
* @invalid-email-address -Fixed a websocket endpoint
* @matias49 -Updated README and added a .gitignore file

## Misc. Credits
* All the other library devs on the [Unofficial Discord API Server](https://discord.gg/0SBTUU1wZTU7PCok) -You guys helped so 
much with demystifying the discord "api"
* All the dedicated users on #java_discord4j and our official Discord server who reported bugs and helped new users, it's made maintaining this lib so much easier

### WARNING: The following module is highly experimental and prone to drastic API changes between versions. It is not recommended for general use!

# Discord4J Command
The `command` module provides a set of low-level tools for dealing with bot commands. It can be used on its own or
easily serve as a basis of interoperability for higher-level command libraries. 

This module is extremely extensible while still being relatively lightweight. This allows for a very large degree of
freedom, meaning that your cool idea for a command api can be easily implemented as a layer atop this base api.
Additionally, this api allows for commands to interoperate very easily. Interoperation allows for commands to be easily
distributed without an actual bot, meaning that developers can create bot-less "command packs" which can be incorporated
into other users' bots quite simply.

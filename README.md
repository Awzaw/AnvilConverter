# AnvilConverter

A tool to convert .mcr mcregion worlds to .mca anvil or .mcapm pmanvil format

PMAnvil has the best performance with PocketMine; if you use Anvil format, this PHP extension reduces lag massively: https://github.com/dktapps/PocketMine-C-ChunkUtils

Usage:

```java -jar "/PathTo/AnvilConverter.jar" /PathTo/PocketMine/worlds worldtoconvert pmanvil```


The original .mcr files will be untouched, and the original level.dat renamed to level.dat_mcr

A new anvil/pmanvil level.dat is automatically generated to replace the mcregion version, so the server is ready to run after conversion. You can remove all the old .mcr files and level.dat_mcr if successful, or revert by renaming level.dat_mcr to level.dat and deleting the .mca or .mcapm files

Code from https://www.mojang.com/2012/02/new-minecraft-map-format-anvil/
Don't do evil


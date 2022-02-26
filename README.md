# LockLoginPremium
This module allows <b>only at BungeeCord and Velocity</b> to authenticate premium users as soon as they join.

As LockLogin does not use any method to verify if a player joins with a premium account, the process is manual and requires the player to run $premium.

### Why $premium and not /premium
Acording to LockLogin default configuration, modules command prefix is $ instead of /.

This is because LockLogin wants to avoid any command overwrite causing errors. For example, setting the module command prefix to / and creating a msg command, would replace the /msg command with LockLogin one.

Anyway, the module command prefix can be changed [in configuration file](https://github.com/KarmaConfigs/LockLoginReborn/wiki/Configuration)

```yaml
#Module commands prefix
ModulePrefix: "$"
```

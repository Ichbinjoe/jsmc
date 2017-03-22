# JSMC - Bukkit Plugin for Javascript Code

This project is dedicated to providing a solid bootstrap for developers, server
administrators, and anyone to easily, quickly, and efficiently modify the
behavior of their Minecraft servers using Javascript. Javascript can allow
developers to write correct and succinct code using a wide variety of
programming paradigms which are much harder or require far more code to
replicate in pure Java. The bulk of code in Jsmc is the packaging and dependency
system. Jsmc exposes a mostly compatible `require()` mechanism similar to
[Node.js](https://nodejs.org/api/modules.html) (however, it is important to note
that the require is not completely compatible, but is close enough to suit most
non-node npm modules).

## Prerequisites / Special Notes

Jsmc requires Java 8, due to the heavy dependence of [Project
Nashorn](http://openjdk.java.net/projects/nashorn/) which is only available on
Java 8 and above. While other Javascript interpreters are available, it is the
decision of this project to enforce use of Nashorn to leverage the near-runtime
performance of the Nashorn execution environment.

Jsmc currently only has a Bukkit plugin variety. In the future, more modules may
be created to accommodate other servers in the Minecraft ecosystem, such as
BungeeCord or Sponge (PRs welcome!!).

## Installation

Installation of Jsmc is as easy as placing the Jsmc plugin within the `plugins/`
folder of your Bukkit compatible server (Original Craftbukkit, Spigot, Paper,
Taco, etc...). When the server starts for the first time, Jsmc will create a
default `node_modules` folder within your server's world container (most times,
this is the 'root' of your server, the folder which contains your server jar,
worlds, plugins, etc.) along with filling it with the default module loader,
`mc-bukkit-default-loader`.

Additional modules can be installed using `npm` if the module is on npm, or
simply can be placed within the `node_modules/` folder to be recognized by the
module loader. More information about the default loader can be viewed on it's
README.md page.

## Development

Modules can be developed for Jsmc iff the execution code of the module is
compatible with the Nashorn JS execution engine. This means:

+ Code to be compiled by Nashorn must be [ECMAScript
  5.1](http://ecma-international.org/ecma-262/5.1/) compatible. ECMAScript 6 and
  above scripts must be transpiled down to ECMAScript 5.1. This can be achieved
  by using a transpiler such as [Babel](https://babeljs.io/).
+ Code may not use Node.js specific modules. Jsmc does not provide any
  'built-in' modules by default - plugins may hook in and provide modules,
  however, these may not be compatible with node.js modules.
+ Code may not use browser specific apis. This plugin is for Minecraft servers -
  I'm not sure why we ever would expose browser APIs, but I'm sure if this isn't
  mentioned, someone will ask.
+ Code may use Nashorn specific functions. The keyword `Java` can be used for
  conversion back and forth between Javascript and Java types.

More detail will be filled in on this project's wiki pages.

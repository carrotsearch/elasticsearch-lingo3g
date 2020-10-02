Lingo3G search results clustering extension for Elasticsearch
=============================================================

This plugin adds on-the-fly text clustering capability
to an Elasticsearch instance.

Note this is an *extension* plugin, it requires 
[elasticsearch-carrot2](https://github.com/carrot2/elasticsearch-carrot2)
plugin to be installed independently and just adds Carrot Search 
Lingo3G algorithm support.


Installation
------------

In order to install a stable version of the plugin, 
run ElasticSearch's `plugin` utility (remember to pick the
ES-compatible version of the plugin from the table below!).

    bin/elasticsearch-plugin install com.carrotsearch:elasticsearch-lingo3g:(es-version)

To install from sources (master branch), install Lingo3G POM in
your local Maven repository first, then run:

    ./gradlew clean build

and install with:

    Linux:
    bin/elasticsearch-plugin install file:/.../(plugin)/build/distributions/*.zip

    Windows:
    bin\elasticsearch-plugin install file:///c:/.../(plugin)/build/distributions/*.zip

The plugin will display a popup to request additional permissions - you have 
to accept such a prompt.


Usage
-----

Instructions are identical to that of [elasticsearch-carrot2](https://github.com/carrot2/elasticsearch-carrot2), please refer
to that plugin's documentation.


License
-------

This software is licensed under the Apache 2 license. Full text
of the license is in the repository (`LICENSE.txt`).

Lingo3G is commercial, proprietary software and is not included in 
this bundle.

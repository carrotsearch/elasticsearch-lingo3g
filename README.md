Lingo3G search results clustering extension for Elasticsearch
=============================================================

This plugin adds on-the-fly text clustering capability
to an Elasticsearch instance.

Note this is an *extension* plugin, it requires 
[elasticsearch-carrot2](https://github.com/carrot2/elasticsearch-carrot2)
plugin to be installed independently and just adds Carrot Search 
Lingo3G algorithm support.

You must own Lingo3G license and have access to its binary
distribution to build and run the plugin. 


Installation
------------

The plugin can be built and installed from sources (preferable)
or by adding Lingo3G JAR to prebuilt plugin binaries and installing
it afterward.

To install from sources, install Lingo3G POM in
your local Maven repository first, according to the instructions 
available with Lingo3G distribution.

Elasticsearch will only run plugins that have been built *exactly*
against the given version of its API. Open `build.gradle` and adjust
the version of Elasticsearch, elasticsearch-carrot2 plugin and 
Lingo3G the extension plugin will be compiled against:

    localversions = [
        es       : "7.11.2",
        c2plugin : "7.11.2",
        l3g      : "2.0.0-beta1"
    ]

Then build the plugin:

    ./gradlew clean build

and install in Elasticsearch with:

    Linux:
    bin/elasticsearch-plugin install file:/.../(plugin)/build/distributions/elasticsearch-lingo3g-full-*.zip

    Windows:
    bin\elasticsearch-plugin install file:///c:/.../(plugin)/build/distributions/elasticsearch-lingo3g-full-*.zip

The installer will display a popup to request additional permissions - you have 
to accept this request, otherwise the plugin will not be installed.

Alternatively, you can also fetch and modify plugin binaries from 
[Maven Central](https://repo1.maven.org/maven2/com/carrotsearch/elasticsearch-lingo3g/).

Once the ZIP distribution of the plugin is downloaded, you will have to add Lingo3G JAR
to the archive, then proceed with `elasticsearch-plugin` installation above.

Each plugin binary matches the corresponding Elasticsearch version it was compiled against.
We do *not* publish binaries for each and every version of ES so you may have to recompile
from sources from time to time.


Lingo3G license
---------------

The plugin will require a valid license file in one of the following locations:

* `${es-home}/config/`
* `${es-home}/elasticsearch-lingo3g/`
* Elasticsearch user's home folder


Compatibility
-------------

| plugin                | elasticsearch         | Lingo3G       |
| ---                   |                   --- | ---           |
| 7.7.1                 | 7.7.1                 |  2.0.0-beta1  |


Usage
-----

Instructions are identical to that of [elasticsearch-carrot2](https://github.com/carrot2/elasticsearch-carrot2), 
please refer to that plugin's documentation.


License
-------

This plugin is licensed under the Apache 2 license. Full text
of the license is in the repository (`LICENSE.txt`).

Lingo3G is commercial, proprietary software and is not included in 
this bundle. Send an e-mail request to info@carrotsearch.com for
an evaluation license. 

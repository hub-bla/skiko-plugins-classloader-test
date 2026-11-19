# Two IntelliJ plugins, two Skiko versions

## Prerequisite: publish Skiko to mavenLocal from this branch

The plugins resolve Skiko only from `~/.m2` (mavenLocal), so Skiko must be published from the
**`hub.bla/skiko-rendering-module`** branch of the Skiko repository, from its `skiko/` directory:

    git checkout hub.bla/skiko-rendering-module
    ./gradlew publishToMavenLocal

Publish from this particular branch and not from `master` or a release: the check relies on the Skiko test helpers
`CoreInstance.cc` / `CoreInstanceProbe.kt` and on the `@loader_path` / `$ORIGIN` / delay-load isolation of native
libraries, and these exist only on this branch, in non-release (SNAPSHOT) builds. If the branch changes, publish again
(and rebuild the other versions below), otherwise the plugins run against a stale build.

`plugin-a` and `plugin-b` each bundle their own Skiko (`skikoA` / `skikoB` in `gradle.properties`, resolved from
mavenLocal). `plugin-a` registers the `skikoCheck` starter, which runs each plugin's `SkikoProbe` in that plugin's
class loader: it creates a `SkiaLayer` and checks that
- the Skiko classes come from the plugin's own class loader,
- a Ganesh call that runs through Skia code in core works,
- the core library instance the plugin's classes use (`_nCoreInstance`) is the one the platform library is linked to
  (`_nCoreInstanceSeenByPlatform`), and the two plugins have different core instances.

The last check uses the test helpers of the Skiko branch (`CoreInstance.cc`, `CoreInstanceProbe.kt`), which are
compiled only into non-release builds.

    ./gradlew :plugin-a:runCheck -Porder=ab   # or ba; report in plugin-a/build/check-<order>.txt

Skiko versions published from the Skiko checkout (`./gradlew publishToMavenLocal -Pdeploy.version=<v>`):

| version        | contents                                                                   |
|----------------|----------------------------------------------------------------------------|
| 0.0.0-SNAPSHOT | current source (loads core via `@loader_path`)                             |
| 0.0.1-SNAPSHOT | same, but core exports `skikoCoreInstanceV2` instead of `skikoCoreInstance` (ABI differs from A) |
| 0.0.2-SNAPSHOT | negative control: install name `./lib...` (before the fix) + V2 symbol    |
| 0.0.3-SNAPSHOT | negative control: install name `./lib...`, original symbol                 |

Negative control: `-PskikoA=0.0.2-SNAPSHOT -PskikoB=0.0.3-SNAPSHOT`.
The native probe only exists in non-release Skiko builds (test helpers).

Rebuild all four after the Skiko branch changes (temporary edits, reverted afterwards):

    ./gradlew publishToMavenLocal                                 # 0.0.0: unchanged source
    # rename skikoCoreInstance -> skikoCoreInstanceV2 in src/jvmTest/cpp/CoreInstance.cc and
    # skiko-platform/src/jvmTest/cpp/CoreInstance.cc, then:
    ./gradlew publishToMavenLocal -Pdeploy.version=0.0.1
    # also change -install_name "@loader_path/..." to "./..." in JvmTasksConfiguration.kt, then 0.0.2;
    # restore the original symbol name (keep the install name edit), then 0.0.3

workspace(name = "io_bazel")

# Protobuf expects an //external:python_headers label which would contain the
# Python headers if fast Python protos is enabled. Since we are not using fast
# Python protos, bind python_headers to a dummy target.
bind(
    name = "python_headers",
    actual = "//:dummy",
)

# Protobuf code generation for GRPC requires three external labels:
# //external:grpc-java_plugin
# //external:grpc-jar
# //external:guava
bind(
    name = "grpc-java-plugin",
    actual = "//third_party/grpc:grpc-java-plugin",
)

bind(
    name = "grpc-jar",
    actual = "//third_party/grpc:grpc-jar",
)

bind(
    name = "guava",
    actual = "//third_party:guava",
)

# Used by //third_party/protobuf:protobuf_python
bind(
    name = "six",
    actual = "//third_party/py/six",
)

http_archive(
    name = "bazel_j2objc",
    url = "https://github.com/google/j2objc/releases/download/2.0.3/j2objc-2.0.3.zip",
    # Computed using "shasum -a 256 j2objc-2.0.3.zip"
    sha256 = "a36bac432d0dbd8c98249e484b2b69dd5720afa4abb58711a3c3def1c0bfa21d",
    strip_prefix = "j2objc-2.0.3",
)

# For src/test/shell/bazel:test_srcs
load("//src/test/shell/bazel:list_source_repository.bzl", "list_source_repository")
list_source_repository(name = "local_bazel_source_list")

# To run the Android integration tests in //src/test/shell/bazel/android:all or
# build the Android sample app in //examples/android/java/bazel:hello_world
#
#   1. Install an Android SDK and NDK from https://developer.android.com
#   2. Set the $ANDROID_HOME and $ANDROID_NDK_HOME environment variables
#   3. Uncomment the two lines below
#
# android_sdk_repository(name = "androidsdk")
# android_ndk_repository(name = "androidndk")

# In order to run //src/test/shell/bazel:maven_skylark_test, follow the
# instructions above for the Android integration tests and uncomment the
# following lines:
# load("//tools/build_defs/repo:maven_rules.bzl", "maven_dependency_plugin")
# maven_dependency_plugin()

# This allows rules written in skylark to locate apple build tools.
bind(
    name = "xcrunwrapper",
    actual = "@bazel_tools//tools/objc:xcrunwrapper",
)

new_local_repository(
    name = "com_google_protobuf",
    build_file = "./third_party/protobuf/3.6.0/BUILD",
    path = "./third_party/protobuf/3.6.0/",
)

new_local_repository(
    name = "com_google_protobuf_cc",
    build_file = "./third_party/protobuf/3.6.0/BUILD",
    path = "./third_party/protobuf/3.6.0/",
)

new_local_repository(
    name = "com_google_protobuf_java",
    build_file = "./third_party/protobuf/3.6.0/com_google_protobuf_java.BUILD",
    path = "./third_party/protobuf/3.6.0/",
)

new_local_repository(
    name = "googleapis",
    path = "./third_party/googleapis/",
    build_file = "./third_party/googleapis/BUILD",
)

http_archive(
    name = "desugar_jdk_libs",
    url = "https://github.com/google/desugar_jdk_libs/archive/fd937f4180c1b557805219af4482f1a27eb0ff2b.zip",
    # Computed using "shasum -a 256 <zip>"
    sha256 = "43b8fcc56a180e178d498f375fbeb95e8b65b9bf6c2da91ae3ae0332521a1a12",
    strip_prefix = "desugar_jdk_libs-fd937f4180c1b557805219af4482f1a27eb0ff2b",
)

load("//:distdir.bzl", "distdir_tar")
distdir_tar(
    name = "additional_distfiles",
    dirname = "derived/distdir",
    archives = ["fd937f4180c1b557805219af4482f1a27eb0ff2b.zip"],
    urls = {
        "fd937f4180c1b557805219af4482f1a27eb0ff2b.zip" :
        ["https://github.com/google/desugar_jdk_libs/archive/fd937f4180c1b557805219af4482f1a27eb0ff2b.zip"],
    },
    sha256 = {
        "fd937f4180c1b557805219af4482f1a27eb0ff2b.zip" :
        "43b8fcc56a180e178d498f375fbeb95e8b65b9bf6c2da91ae3ae0332521a1a12",
  },
)

# OpenJDK distributions used to create a version of Bazel bundled with the OpenJDK.
http_file(
    name = "openjdk_linux",
    sha256 = "57fad3602e74c79587901d6966d3b54ef32cb811829a2552163185d5064fe9b5",
    urls = [
        "https://mirror.bazel.build/openjdk/azul-zulu10.2%2B3-jdk10.0.1/zulu10.2%2B3-jdk10.0.1-linux_x64-allmodules.tar.gz",
    ],
)

http_file(
    name = "openjdk_macos",
    sha256 = "e669c9a897413d855b550b4e39d79614392e6fb96f494e8ef99a34297d9d85d3",
    urls = [
        "https://mirror.bazel.build/openjdk/azul-zulu10.2%2B3-jdk10.0.1/zulu10.2%2B3-jdk10.0.1-macosx_x64-allmodules.tar.gz",
    ],
)

http_file(
    name = "openjdk_win",
    sha256 = "c39e7700a8d41794d60985df5a20352435196e78ecbc6a2b30df7be8637bffd5",
    urls = [
        "https://mirror.bazel.build/openjdk/azul-zulu10.2%2B3-jdk10.0.1/zulu10.2%2B3-jdk10.0.1-win_x64-allmodules.zip",
    ],
)

# The source-code for this OpenJDK can be found at:
# https://openjdk.linaro.org/releases/jdk9-src-1708.tar.xz
http_file(
    name = "openjdk_linux_aarch64",
    sha256 = "72e7843902b0395e2d30e1e9ad2a5f05f36a4bc62529828bcbc698d54aec6022",
    urls = [
        # When you update this, also update the link to the source-code above.
        "http://openjdk.linaro.org/releases/jdk9-server-release-1708.tar.xz",
    ],
)

http_archive(
  name = "bazel_toolchains",
  urls = [
    "https://mirror.bazel.build/github.com/bazelbuild/bazel-toolchains/archive/5124557861ebf4c0b67f98180bff1f8551e0b421.tar.gz",
    "https://github.com/bazelbuild/bazel-toolchains/archive/5124557861ebf4c0b67f98180bff1f8551e0b421.tar.gz",
  ],
  strip_prefix = "bazel-toolchains-5124557861ebf4c0b67f98180bff1f8551e0b421",
  sha256 = "c3b08805602cd1d2b67ebe96407c1e8c6ed3d4ce55236ae2efe2f1948f38168d",
)

# We're pinning to a commit because this project does not have a recent release.
# Nothing special about this commit, though.
http_archive(
    name = "com_google_googletest",
    urls = [
        "https://github.com/google/googletest/archive/dfa853b63d17c787914b663b50c2095a0c5b706e.tar.gz",
    ],
    strip_prefix = "googletest-dfa853b63d17c787914b663b50c2095a0c5b706e",
    sha256 = "313a16fba8f0be8ee20ba9883e044556044cbd1ae6cea532473d163a843ef991",
)

# For src/test/shell/bazel:bazel_sandboxing_test
http_file(
  name = 'mount_path_toolchain',
  url = 'https://asci-toolchain.appspot.com.storage.googleapis.com/toolchain-testing/mount_path_toolchain.tar.gz',
  sha256 = "dd8088d3543a86fd91a9ccde6e40102aff6eaf3d048aa73cc18eff05cc2053d5",
)

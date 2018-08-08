// Copyright 2018 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.vfs;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.devtools.common.options.Converter;
import com.google.devtools.common.options.OptionsParsingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/** Type of hash function to use for digesting files. */
// The underlying HashFunctions are immutable and thread safe.
public class DigestHashFunction {
  // This map must be declared first to make sure that calls to register() have it ready.
  private static final HashMap<String, DigestHashFunction> hashFunctionRegistry = new HashMap<>();

  public static final DigestHashFunction MD5 = register(Hashing.md5(), "MD5");
  public static final DigestHashFunction SHA1 = register(Hashing.sha1(), "SHA-1", "SHA1");
  public static final DigestHashFunction SHA256 = register(Hashing.sha256(), "SHA-256", "SHA256");

  private static DigestHashFunction defaultHash;
  private static boolean defaultHasBeenSet = false;

  private final HashFunction hash;
  private final String name;

  private DigestHashFunction(HashFunction hash, String name) {
    this.hash = hash;
    this.name = name;
  }

  public HashFunction getHash() {
    return hash;
  }

  public boolean isValidDigest(byte[] digest) {
    // TODO(b/109764197): Remove this check to accept variable-length hashes.
    return digest != null && digest.length * 8 == hash.bits();
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Creates a new DigestHashFunction that is registered to be recognized by its name in {@link
   * DigestFunctionConverter}.
   *
   * @param hashName the canonical name for this hash function.
   * @param altNames alternative names that will be mapped to this function by the converter but
   *     will not serve as the canonical name for the DigestHashFunction.
   * @param hash The {@link HashFunction} to register.
   * @throws IllegalArgumentException if the name is already registered.
   */
  public static DigestHashFunction register(
      HashFunction hash, String hashName, String... altNames) {
    DigestHashFunction hashFunction = new DigestHashFunction(hash, hashName);
    List<String> names = ImmutableList.<String>builder().add(hashName).add(altNames).build();
    synchronized (hashFunctionRegistry) {
      for (String name : names) {
        if (hashFunctionRegistry.containsKey(name)) {
          throw new IllegalArgumentException("Hash function " + name + " is already registered.");
        }
        hashFunctionRegistry.put(name, hashFunction);
      }
    }
    return hashFunction;
  }

  /**
   * Returns the default DigestHashFunction for this instance of Bazel.
   *
   * <p>Note: This is a synchronized function, to make sure it does not occur concurrently with
   * {@link #setDefault(DigestHashFunction)}. Once this value is set, it's a constant, so to prevent
   * blocking calls, users should cache this value if needed.
   *
   * @throws DefaultNotSetException if the default has not yet been set by a previous call to {@link
   *     #setDefault}.
   */
  public static synchronized DigestHashFunction getDefault() throws DefaultNotSetException {
    if (!defaultHasBeenSet) {
      throw new DefaultNotSetException("DigestHashFunction default has not been set");
    }
    return defaultHash;
  }

  /** Indicates that the default has not been initialized. */
  public static final class DefaultNotSetException extends Exception {
    DefaultNotSetException(String message) {
      super(message);
    }
  }

  /**
   * Sets the default DigestHashFunction for this instance of Bazel - can only be set once to
   * prevent incongruities.
   *
   * @throws DefaultAlreadySetException if it was already set.
   */
  public static synchronized void setDefault(DigestHashFunction hash)
      throws DefaultAlreadySetException {
    if (defaultHasBeenSet) {
      throw new DefaultAlreadySetException(
          String.format(
              "setDefault(%s) failed. The default has already been set to %s, you cannot reset it.",
              hash.name, defaultHash.name));
    }
    defaultHash = hash;
    defaultHasBeenSet = true;
  }

  /** Failure to set the default if the default already being set. */
  public static final class DefaultAlreadySetException extends Exception {
    DefaultAlreadySetException(String message) {
      super(message);
    }
  }

  /** Converts a string to its registered {@link DigestHashFunction}. */
  public static class DigestFunctionConverter implements Converter<DigestHashFunction> {
    @Override
    public DigestHashFunction convert(String input) throws OptionsParsingException {
      for (Entry<String, DigestHashFunction> possibleFunctions : hashFunctionRegistry.entrySet()) {
        if (possibleFunctions.getKey().equalsIgnoreCase(input)) {
          return possibleFunctions.getValue();
        }
      }
      throw new OptionsParsingException("Not a valid hash function.");
    }

    @Override
    public String getTypeDescription() {
      return "hash function";
    }
  }
}
